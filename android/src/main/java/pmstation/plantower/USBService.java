/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation.plantower;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

import pmstation.core.plantower.PlanTowerDevice;

public class USBService extends PlanTowerService {
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_PERMISSION = "pmstation.USB_PERMISSION";
    public static final String ACTION_USB_PERMISSION_GRANTED = "pmstation.plantower.sensor.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED =
            "pmstation.plantower.sensor.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_CONNECTED = "pmstation.plantower.sensor.USB_CONNECTED";
    public static final String ACTION_USB_DISCONNECTED = "pmstation.plantower.sensor.USB_DISCONNECTED";
    private static final String TAG = USBService.class.getSimpleName();
    private static final int BAUD_RATE = 9600;
    public static boolean SERVICE_CONNECTED = false;

    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private boolean serialPortConnected;
    private boolean requested = false;
    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    clearPermissionReqestFlag();
                    Bundle extras = intent.getExtras();
                    if (extras == null) {
                        return;
                    }
                    boolean granted = extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) { // User accepted our USB connection. Try to open the device as a serial port
                        if (connectDevice()) {
                            Intent usbIntent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                            context.sendBroadcast(usbIntent);
                        }
                    } else { // User did not accept USB connection.
                        Intent usbIntent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                        context.sendBroadcast(usbIntent);
                    }
                    break;
                case ACTION_USB_ATTACHED:
                    if (!isConnected()) {
                        findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
                        Intent usbIntent = new Intent(ACTION_USB_CONNECTED);
                        context.sendBroadcast(usbIntent);
                    }
                    break;
                case ACTION_USB_DETACHED:
                    // Usb device was disconnected.
                    Intent usbIntent = new Intent(ACTION_USB_DISCONNECTED);
                    context.sendBroadcast(usbIntent);
                    if (isConnected()) {
                        disconnectDevice();
                    }
                    break;
            }
        }
    };

    private IBinder binder = new LocalBinder();
    private UsbSerialInterface.UsbReadCallback readCallback = this::parseData;


    public void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                    // There is a device connected to our Android device. Try to open it as a Serial Port.
                    requestUserPermission();
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep) {
                    break;
                }
            }
        }
    }

    public void clearPermissionReqestFlag() {
        requested = false;
    }

    public boolean connectDevice() {
        connection = usbManager.openDevice(device);

        serialPortConnected = connection != null;
        if (serialPortConnected) {
            new ConnectionThread().start();
        }

        return serialPortConnected;
    }

    public void disconnectDevice() {
        if (serialPort != null) {
            serialPort.close();
        }
        serialPortConnected = false;
    }

    public void write(byte[] data) {
        if (serialPort != null) {
            serialPort.write(data);
        }
    }

    /**
     * Attempts to put the sensor in sleep mode.
     *
     * @return whether the sensor is connected or not
     */
    @Override
    public boolean sleep() {
        write(PlanTowerDevice.MODE_SLEEP);
        return isConnected();
    }

    /**
     * Attempts to wake the sensor from sleep mode.
     *
     * @return whether the sensor is connected or not
     */
    @Override
    public boolean wakeUp() {
        write(PlanTowerDevice.MODE_WAKEUP);
        return isConnected();
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        if (!requested) {
            requested = true;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(device, pendingIntent);
        }
    }

    public boolean isConnected() {
        return serialPortConnected;
    }

    /*
    * onCreate will be executed when service is started. It configures an IntentFilter to listen for
    * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
    */
    @Override
    public void onCreate() {
        super.onCreate();
        serialPortConnected = false;
        USBService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        serialPortConnected = false;
        findSerialPortDevice();
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        sleep();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "USB service onDestroy");
        USBService.SERVICE_CONNECTED = false;
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        public USBService getService() {
            return USBService.this;
        }
    }

    /*
        * A simple thread to open a serial port.
        * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
        */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    serialPortConnected = true;
                    serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(readCallback);
                    return;
                }
                Log.e(TAG, "USB not working");
            }
        }
    }
}
