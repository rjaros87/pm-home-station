/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.plantower.BluetoothLeService;
import pmstation.plantower.USBSensor;

public class MainActivity extends AppCompatActivity implements IPlanTowerObserver {
    public static final String VALUES_FRAGMENT = "VALUES_FRAGMENT";
    public static final String CHART_FRAGMENT = "CHART_FRAGMENT";
    public static final String SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    public static final String ABOUT_FRAGMENT = "ABOUT_FRAGMENT";
    public static final String LAST_SINGLE_PANE_FRAGMENT = "lastSinglePaneFragment";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVICE_UUID = "0000ffe0";
    private static final String CHARACTERISTIC_UUID = "0000ffe1";

    private final List<IPlanTowerObserver> valueObservers = Collections.synchronizedList(new ArrayList<>());
    private List<ParticulateMatterSample> values = Collections.synchronizedList(new ArrayList<>());
    private Menu menu;
    private ImageView smog;
    private String lastSinglePaneFragment;
    private USBSensor sensor;
    private boolean running = false;
    private boolean justConnected;
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case USBSensor.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    // if was just connected then it's not asleep
                    if (!justConnected) {
                        sensor.wakeUp();
                    }
                    setStatus(true);
                    break;
                case USBSensor.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    setStatus(false);
                    break;
                case USBSensor.ACTION_USB_CONNECTED: // NO USB CONNECTED
                    justConnected = true;
                    break;
                case USBSensor.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    setStatus(false);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
            }
        }
    };
    private USBHandler usbHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sensor = ((USBSensor.LocalBinder) service).getService();
            sensor.setHandler(usbHandler);
            if (isEmulator()) {
                sensor.startFakeDataThread();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sensor.stopFakeDataThread();
            sensor = null;
        }
    };
    private HandlerThread handlerThread;
    private BluetoothLeService bluetoothLeService;
    private String deviceAddress;
    private final ServiceConnection btConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            bluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };
    private boolean btConnected = false;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                btConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                btConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                findSerialChatacteristic(bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                sensor.parseData(data);
            }
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void findSerialChatacteristic(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        String uuid;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();

            if (!uuid.toLowerCase().startsWith(SERVICE_UUID)) {
                continue;
            }

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.toLowerCase().startsWith(CHARACTERISTIC_UUID)) {
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (notifyCharacteristic != null) {
                            bluetoothLeService.setCharacteristicNotification(notifyCharacteristic, false);
                            notifyCharacteristic = null;
                        }
                        bluetoothLeService.readCharacteristic(gattCharacteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        notifyCharacteristic = gattCharacteristic;
                        bluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    }
                    break;
                }
            }
        }
    }

    private ValuesFragment getDetatchedValuesFragment(boolean popBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        ValuesFragment valuesFragment = (ValuesFragment) getSupportFragmentManager().findFragmentByTag(VALUES_FRAGMENT);
        if (valuesFragment == null) {
            valuesFragment = new ValuesFragment();
        } else {
            if (popBackStack) {
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            fm.beginTransaction().remove(valuesFragment).commit();
            fm.executePendingTransactions();
        }
        return valuesFragment;
    }

    private ChartFragment getDetatchedChartFragment() {
        FragmentManager fm = getSupportFragmentManager();
        ChartFragment chartFragment = (ChartFragment) getSupportFragmentManager().findFragmentByTag(CHART_FRAGMENT);
        if (chartFragment == null) {
            chartFragment = new ChartFragment();
        } else {
            fm.beginTransaction().remove(chartFragment).commit();
            fm.executePendingTransactions();
        }
        return chartFragment;
    }

    private void openSinglePaneChartFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ChartFragment detailFragment = getDetatchedChartFragment();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.single_pane, detailFragment, CHART_FRAGMENT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrash.setCrashCollectionEnabled(!BuildConfig.DEBUG);
        usbHandler = new USBHandler(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        smog = findViewById(R.id.smog);
        smog.setAlpha(0f);

        addValueObserver(this);

        boolean dualPane = findViewById(R.id.dual_pane) != null;

        if (savedInstanceState != null) {
            lastSinglePaneFragment = savedInstanceState.getString(LAST_SINGLE_PANE_FRAGMENT);
            running = savedInstanceState.getBoolean("running");
            if (running) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        FragmentManager fm = getSupportFragmentManager();

        if (!dualPane && fm.findFragmentById(R.id.single_pane) == null) {
            ValuesFragment valuesFragment = getDetatchedValuesFragment(false);
            fm.beginTransaction().add(R.id.single_pane, valuesFragment, VALUES_FRAGMENT).commit();
            if (CHART_FRAGMENT.equals(lastSinglePaneFragment)) {
                openSinglePaneChartFragment();
            }
        }
        if (dualPane && fm.findFragmentById(R.id.values_dual) == null) {
            ValuesFragment valuesFragment = getDetatchedValuesFragment(true);
            fm.beginTransaction().add(R.id.values_dual, valuesFragment, VALUES_FRAGMENT).commit();
        }
        if (dualPane && fm.findFragmentById(R.id.chart_dual) == null) {
            ChartFragment chartFragment = getDetatchedChartFragment();
            fm.beginTransaction().add(R.id.chart_dual, chartFragment, CHART_FRAGMENT).commit();
        }

        handlerThread = new HandlerThread("ht");
        handlerThread.start();

        deviceAddress = PreferenceManager.getDefaultSharedPreferences(this).getString("bt_mac", "00:25:83:00:62:E7");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, btConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (!isChangingConfigurations() && sensor != null) {
            sensor.sleep();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Registering receiver");
        registerReceiver();
        startService(USBSensor.class, usbConnection,
                     null); // Start UsbService(if it was not started before) and Bind it
        if (sensor != null) {
            sensor.wakeUp();
        }

        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(), null, handler);
        if (bluetoothLeService != null) {
            final boolean result = bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Unregistering receiver");
        unregisterReceiver(usbReceiver);
        unregisterReceiver(gattUpdateReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(btConnection);
        bluetoothLeService = null;
        handlerThread.quit();
    }


    public List<ParticulateMatterSample> getValues() {
        return values;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_status, menu);
        this.menu = menu;
        setStatus(isRunning());

        int[] arr = {R.id.action_chart, R.id.action_connected, R.id.action_disconnected};
        for (int i : arr) {
            MenuItem item = menu.findItem(i);
            if (item != null) {
                MainActivity.tintMenuItem(item);
            }
        }

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
                if (sensor.sleep()) {
                    setStatus(false);
                }
                return true;
            case R.id.action_disconnected:
                Log.d(TAG, "Trying to connect");
                if (sensor.wakeUp()) {
                    setStatus(true);
                }
                return true;
            case R.id.action_settings:
                showSingleFragment(SETTINGS_FRAGMENT);
                return true;
            case R.id.action_about:
                showSingleFragment(ABOUT_FRAGMENT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChart() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CHART_FRAGMENT);
        if (fragment != null && !fragment.isDetached() && fragment.getId() != R.id.chart_dual) {
            return;
        }
        openSinglePaneChartFragment();
    }

    private void showSingleFragment(String fragmentTag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment == null) {
            switch (fragmentTag) {
                case SETTINGS_FRAGMENT:
                    fragment = new SettingsFragment();
                    break;
                case ABOUT_FRAGMENT:
                    fragment = new AboutFragment();
                    break;
            }
        } else {
            return;
        }
        getSupportFragmentManager().beginTransaction()
                                   .replace(android.R.id.content, fragment, fragmentTag).addToBackStack(null)
                                   .commit();
    }

    void setStatus(boolean connected) {
        this.running = connected;
        if (menu == null) {
            return;
        }
        menu.getItem(0).setVisible(connected);
        menu.getItem(1).setVisible(!connected);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USBSensor.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(USBSensor.ACTION_USB_DISCONNECTED);
        filter.addAction(USBSensor.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(usbReceiver, filter);
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        values.add(sample);
        AQIColor pm25Color = AQIColor.fromPM25Level(sample.getPm2_5());
        runOnUiThread(() -> smog.animate().alpha(pm25Color.getAlpha()));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("running", running);
        Fragment singleFragment = getSupportFragmentManager().findFragmentById(R.id.single_pane);
        if (singleFragment == null) {
            outState.putString(LAST_SINGLE_PANE_FRAGMENT, lastSinglePaneFragment);
        } else {
            outState.putString(LAST_SINGLE_PANE_FRAGMENT,
                               singleFragment instanceof ValuesFragment ? VALUES_FRAGMENT : CHART_FRAGMENT);
        }

        super.onSaveInstanceState(outState);
    }

    public USBSensor getSensor() {
        return sensor;
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!USBSensor.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void addValueObserver(IPlanTowerObserver observer) {
        valueObservers.add(observer);
    }

    public void removeValueObserver(IPlanTowerObserver observer) {
        valueObservers.remove(observer);
    }

    private void notifyAllObservers(final ParticulateMatterSample sample) {
        synchronized (valueObservers) {
            for (IPlanTowerObserver valueObserver : valueObservers) {
                valueObserver.update(sample);
            }
        }
    }

    public void wakeConnection() {
        if (sensor == null) {
            return;
        }
        sensor.wakeWorkerThread();
    }

    private static class USBHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public USBHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USBSensor.MESSAGE_FROM_SERIAL_PORT:
                    ParticulateMatterSample sample = (ParticulateMatterSample) msg.obj;
                    MainActivity mainActivity = mActivity.get();
                    if (mainActivity != null) {
                        mainActivity.notifyAllObservers(sample);
                    }
                    break;
            }
        }
    }
}
