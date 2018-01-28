/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
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

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.plantower.Sensor;

public class MainActivity extends AppCompatActivity implements IPlanTowerObserver {
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_PERMISSION = "pmstation.USB_PERMISSION";
    public static final String VALUES_FRAGMENT = "VALUES_FRAGMENT";
    public static final String CHART_FRAGMENT = "CHART_FRAGMENT";
    public static final String SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    public static final String ABOUT_FRAGMENT = "ABOUT_FRAGMENT";
    private static final String TAG = "MainActivity";
    public static final String LAST_SINGLE_PANE_FRAGMENT = "lastSinglePaneFragment";
    private Menu menu;
    private ImageView smog;
    private List<ParticulateMatterSample> values = Collections.synchronizedList(new ArrayList<>());
    private boolean running = false;
    private String lastSinglePaneFragment;
    private Sensor sensor;
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
                    sensor.clearPermissionReqestFlag();
                    Bundle extras = intent.getExtras();
                    if (extras == null) {
                        return;
                    }
                    boolean granted = extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) { // User accepted our USB connection. Try to open the device as a serial port
                        if (sensor.connectDevice()) {
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            setStatus(true);
                        }
                    } else { // User not accepted our USB connection.
                        setStatus(false);
                    }
                    break;
                case ACTION_USB_ATTACHED:
                    if (!sensor.isConnected()) {
                        sensor.findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
                    }
                    break;
                case ACTION_USB_DETACHED:
                    // Usb device was disconnected.
                    setStatus(false);
                    if (sensor.isConnected()) {
                        sensor.disconnectDevice();
                    }
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
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

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        smog = findViewById(R.id.smog);
        smog.setAlpha(0f);

        sensor = new Sensor(this);
        sensor.addValueObserver(this);

        boolean dualPane = findViewById(R.id.dual_pane) != null;

        if (savedInstanceState != null) {
            lastSinglePaneFragment = savedInstanceState.getString(LAST_SINGLE_PANE_FRAGMENT);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isEmulator()) {
            sensor.startFakeDataThread();
        }
    }

    @Override
    protected void onStop() {
        if (isEmulator()) {
            sensor.stopFakeDataThread();
        }
        if (!isChangingConfigurations()) {
            sensor.sleep();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Registering receiver");
        registerReceiver();
        sensor.findSerialPortDevice();
        if (isRunning()) {
            sensor.wakeUp();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Unregistering receiver");
        unregisterReceiver(usbReceiver);
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
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
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

    public Sensor getSensor() {
        return sensor;
    }
}
