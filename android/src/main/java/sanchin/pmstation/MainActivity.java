package sanchin.pmstation;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.radoslawjaros.plantower.ParticulateMatterSample;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    static final public byte[] MODE_WAKEUP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x01, 0x01, 0x74};
    static final public byte[] MODE_SLEEP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x73};
    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "sanchin.pmstation.USB_PERMISSION";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    static final private byte START_CHARACTERS = 0x42;
    ValuesFragment valuesFragment;
    ChartFragment chartFragment;
    private boolean serialPortConnected;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private Menu menu;

    private List<ParticulateMatterSample> values = new ArrayList<>();
    private boolean asked = false;

    private boolean connected = false;
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
                    asked = false;
                    Bundle extras = intent.getExtras();
                    if (extras == null) {
                        return;
                    }
                    boolean granted = extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) { // User accepted our USB connection. Try to open the device as a serial port
                        connected = true;
                        setStatus(true);
                        connection = usbManager.openDevice(device);
                        new ConnectionThread().start();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else { // User not accepted our USB connection.
                        connected = false;
                        setStatus(false);
                    }
                    break;
                case ACTION_USB_ATTACHED:
                    if (!serialPortConnected) {
                        findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
                    }
                    break;
                case ACTION_USB_DETACHED:
                    // Usb device was disconnected.
                    connected = false;
                    setStatus(false);
                    if (serialPortConnected) {
                        serialPort.close();
                    }
                    serialPortConnected = false;
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
            }
        }
    };
    private List<ValueObserver> valueObservers = new ArrayList<>();
    private UsbSerialInterface.UsbReadCallback readCallback = bytes -> {
        final ParticulateMatterSample sample = read(bytes);
        if (sample != null) {
//            if (sample)
            updateValues(sample);
        }
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static void tintMenuItem(MenuItem item) {
        Drawable icon = item.getIcon();
        icon.mutate();
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    private boolean write(byte[] data) {
        if (serialPort != null) {
            serialPort.write(data);
        }
        return connected;
    }

    public boolean sleep() {
        return write(MODE_SLEEP);
    }

    public boolean wakeUp() {
        return write(MODE_WAKEUP);
    }

    public void addValueObserver(ValueObserver observer) {
        valueObservers.add(observer);
    }

    public void removeValueObserver(ValueObserver observer) {
        valueObservers.remove(observer);
    }

    public ParticulateMatterSample read(byte[] readBuffer) {
        int headIndex = indexOfArray(readBuffer, START_CHARACTERS);
        if (headIndex >= 0 && readBuffer.length >= headIndex + 16) {
            // remark #1: check if compiler replaces *0x100 with << 8
            // remark #2: seems to necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)
            //int pm1_0 = Byte.toUnsignedInt(readBuffer[10 + headIndex]) * 0x100 + Byte.toUnsignedInt(readBuffer[11 + headIndex]);
            //int pm2_5 = Byte.toUnsignedInt(readBuffer[12 + headIndex]) * 0x100 + Byte.toUnsignedInt(readBuffer[13 + headIndex]);
            //int pm10 = Byte.toUnsignedInt(readBuffer[14 + headIndex]) * 0x100 + Byte.toUnsignedInt(readBuffer[15 + headIndex]);

            int pm1_0 = (readBuffer[10 + headIndex]  & 0xFF) * 0x100 + readBuffer[11 + headIndex] & 0xFF;
            int pm2_5 = (readBuffer[12 + headIndex]  & 0xFF) * 0x100 + readBuffer[13 + headIndex] & 0xFF;
            int pm10 = (readBuffer[14 + headIndex]  & 0xFF) * 0x100 + readBuffer[15 + headIndex] & 0xFF;

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
        serialPortConnected = false;
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        if (savedInstanceState != null) {
            return;
        }

        // Create a new Fragment to be placed in the activity layout
        Fragment valuesFragment = new ValuesFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        valuesFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, valuesFragment).commit();
        if (isEmulator()) {
            Thread t = new Thread(() -> {
                int i = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    i = (i + 1) % 14;
                    try {
                        updateValues(new ParticulateMatterSample(20, i * 10, 100));
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted");
                        Thread.currentThread().interrupt();
                    }
                }
            });
            t.start();
        }
    }

    void updateValues(final ParticulateMatterSample sample) {
        values.add(sample);
        for (ValueObserver valueObserver : valueObservers) {
            valueObserver.onNewValue(sample);
        }
    }

    public List<ParticulateMatterSample> getValues() {
        return values;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_status, menu);
        this.menu = menu;
        setStatus(isConnected());

        MenuItem item = menu.getItem(0);
        MainActivity.tintMenuItem(item);
        item = menu.getItem(1);
        MainActivity.tintMenuItem(item);
        item = menu.getItem(2);
        MainActivity.tintMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_chart:
                showChart();
                return true;
            case R.id.action_connected:
                Log.d(TAG, "Trying to disconnect");
                if (sleep()) {
                    setStatus(false);
                }
                return true;
            case R.id.action_disconnected:
                Log.d(TAG, "Trying to connect");
                if (wakeUp()) {
                    setStatus(true);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChart() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("chartFragment");
        if (fragment != null && !fragment.isDetached()) {
            return;
        }
        ChartFragment chartFragment = new ChartFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, chartFragment, "chartFragment").addToBackStack(null)
                           .commit();
    }

    void setStatus(boolean connected) {
        if (menu == null) {
            return;
        }
        menu.getItem(0).setVisible(connected);
        menu.getItem(1).setVisible(!connected);
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
        }
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        if (!asked) {
            asked = true;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(device, pendingIntent);
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
