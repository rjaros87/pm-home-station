package sanchin.pmstation;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.radoslawjaros.plantower.ParticulateMatterSample;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_USB_READY = "sanchin.pmstation.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "sanchin.pmstation.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "sanchin.pmstation.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "sanchin.pmstation.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "sanchin.pmstation.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "sanchin.pmstation.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING =
            "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING =
            "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "sanchin.pmstation.USB_PERMISSION";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    static final private byte START_CHARACTERS = 0x42;
    private boolean serialPortConnected;
    private Context context;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private TextView pm1;
    private TextView pm25;
    private TextView pm10;
    private TextView time;
    private TextView status;
    private boolean asked = false;

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                asked = false;
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) { // User accepted our USB connection. Try to open the device as a serial port
                    status.setText(R.string.status_connected);
                    connection = usbManager.openDevice(device);
                    new ConnectionThread().start();
                } else { // User not accepted our USB connection. Send an Intent to the Main Activity
                    status.setText(R.string.status_permission_denied);
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!serialPortConnected) {
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
                }
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // Usb device was disconnected. send an intent to the Main Activity
                status.setText(R.string.status_disconnected);
                if (serialPortConnected) {
                    serialPort.close();
                }
                serialPortConnected = false;
            }
        }
    };
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            final ParticulateMatterSample sample = read(arg0);
            if (sample != null) {
                Log.d(TAG, sample.toString());
                updateValues(sample);
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public ParticulateMatterSample read(byte[] readBuffer) {
        int headIndex = indexOfArray(readBuffer, START_CHARACTERS);
        if (headIndex >= 0 && readBuffer.length >= headIndex + 16) {
            int pm1_0 = readBuffer[10 + headIndex] * 0x100 + readBuffer[11 + headIndex];
            int pm2_5 = readBuffer[12 + headIndex] * 0x100 + readBuffer[13 + headIndex];
            int pm10 = readBuffer[14 + headIndex] * 0x100 + readBuffer[15 + headIndex];

            return new ParticulateMatterSample(pm1_0, pm2_5, pm10);
        }
        return null;
    }

    private int indexOfArray(byte[] sampleArray, byte needle) {
        for (int i = 0; (i < sampleArray.length); i++) {
            if (sampleArray[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pm1 = findViewById(R.id.pm1);
        pm25 = findViewById(R.id.pm25);
        pm10 = findViewById(R.id.pm10);
        time = findViewById(R.id.time);
        status = findViewById(R.id.status);

        this.context = this;
        serialPortConnected = false;

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    void updateValues(final ParticulateMatterSample sample) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pm1.setText(String.format(Locale.getDefault(), "%d", sample.getPm1_0()));
                pm25.setText(String.format(Locale.getDefault(), "%d", sample.getPm2_5()));
                pm10.setText(String.format(Locale.getDefault(), "%d", sample.getPm10()));
                time.setText(sample.getDateTime());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Registering receiver");
        registerReceiver();
        findSerialPortDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Unregistering receiver");
        unregisterReceiver(usbReceiver);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    private void findSerialPortDevice() {
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
            if (!keep) {
                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
        } else {
            // There is no USB devices connected. Send an intent to MainActivity
            Intent intent = new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        if (!asked) {
            asked = true;
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(device, mPendingIntent);
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
                    serialPort.read(mCallback);

                    // Everything went as expected. Send an intent to MainActivity
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (serialPort instanceof CDCSerialDevice) {
                        Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        context.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        context.sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(intent);
            }
        }
    }
}
