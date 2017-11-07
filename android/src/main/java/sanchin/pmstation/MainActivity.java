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
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.TextView;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import pl.radoslawjaros.plantower.ParticulateMatterSample;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss",
                                                                            Locale.getDefault());
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "sanchin.pmstation.USB_PERMISSION";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    static final private byte START_CHARACTERS = 0x42;
    private boolean serialPortConnected;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private CardView pm1Card;
    private CardView pm25Card;
    private CardView pm10Card;
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
                        status.setText(R.string.status_connected);
                        connection = usbManager.openDevice(device);
                        new ConnectionThread().start();
                    } else { // User not accepted our USB connection.
                        status.setText(R.string.status_permission_denied);
                    }
                    break;
                case ACTION_USB_ATTACHED:
                    if (!serialPortConnected) {
                        findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
                    }
                    break;
                case ACTION_USB_DETACHED:
                    // Usb device was disconnected.
                    status.setText(R.string.status_disconnected);
                    if (serialPortConnected) {
                        serialPort.close();
                    }
                    serialPortConnected = false;
                    break;
            }
        }
    };
    private UsbSerialInterface.UsbReadCallback readCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            final ParticulateMatterSample sample = read(bytes);
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
        pm1Card = findViewById(R.id.pm1_card);
        pm25Card = findViewById(R.id.pm25_card);
        pm10Card = findViewById(R.id.pm10_card);
        pm1 = pm1Card.findViewById(R.id.pm_value);
        pm25 = pm25Card.findViewById(R.id.pm_value);
        pm10 = pm10Card.findViewById(R.id.pm_value);
        ((TextView) pm1Card.findViewById(R.id.pm_label)).setText(R.string.pm1);
        ((TextView) pm25Card.findViewById(R.id.pm_label)).setText(R.string.pm25);
        ((TextView) pm10Card.findViewById(R.id.pm_label)).setText(R.string.pm10);

        time = findViewById(R.id.time);
        status = findViewById(R.id.status);

        serialPortConnected = false;
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    void updateValues(final ParticulateMatterSample sample) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pm1.setText(String.format(Locale.getDefault(), "%d", sample.getPm1_0()));
                pm25.setText(String.format(Locale.getDefault(), "%d", sample.getPm2_5()));
                pm25Card.setCardBackgroundColor(
                        ColorUtils.setAlphaComponent(AQIColor.fromPM25Level(sample.getPm2_5()).getColor(), 136));
                pm10.setText(String.format(Locale.getDefault(), "%d", sample.getPm10()));
                pm10Card.setCardBackgroundColor(
                        ColorUtils.setAlphaComponent(AQIColor.fromPM10Level(sample.getPm10()).getColor(), 136));
                time.setText(dateFormat.format(sample.getDate()));
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
