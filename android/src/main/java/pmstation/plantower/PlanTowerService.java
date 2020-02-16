/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation.plantower;

import android.app.Service;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.ref.WeakReference;

import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.core.plantower.PlanTowerDevice;

// TODO Radek G, pls update this to use PlanTowerDevice in non-static way (take a look at PlanTowerSensor)
public abstract class PlanTowerService extends Service {
    public static final int DATA_AVAILABLE = 0;
    private static final String TAG = PlanTowerService.class.getSimpleName();
    private WeakReference<Thread> workerThread;
    private Thread fakeDataThread;
    private int sampleCounter = 0;
    private SharedPreferences preferences;

    private Handler handler;

    @Override
    public void onCreate() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    protected void parseData(byte[] bytes) {
        workerThread = new WeakReference<>(Thread.currentThread());
        final ParticulateMatterSample sample = PlanTowerDevice.parse(bytes);
        if (sample != null) {
            notifyActivity(sample);
        } else if (bytes.length == 12) {
            // BT notifies about the data in two turns - 20bytes (which we parse) and 12
            // skip the 12 not to sleep twice
            return;
        }

        try {
            sampleCounter = ++sampleCounter % 10;
            String intervalPref = preferences.getString("sampling_interval", "1000");
            int interval = Integer.parseInt(intervalPref);
            if (interval > 3000) {
                if (sampleCounter == 0) {
                    sleep();
                    Thread.sleep(interval);
                    wakeUp();
                } else {
                    Thread.sleep(1000);
                }
            } else {
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "ConnectionThread sleep interrupted, most likely the sampling interval changed");
            wakeUp();
        }
    }

    protected abstract boolean wakeUp();

    protected abstract boolean sleep();

    public void wakeWorkerThread() {
        if (workerThread == null) {
            return;
        }
        Thread thread = workerThread.get();
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void startFakeDataThread() {
        if (fakeDataThread != null) {
            return;
        }
        fakeDataThread = new Thread(() -> {
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                i = (i + 1) % 14;
                try {
                    notifyActivity(new ParticulateMatterSample(20, i * 10, 100));
                    String intervalPref = preferences.getString("sampling_interval", "1000");
                    int interval = Integer.parseInt(intervalPref);
                    Thread.sleep(sampleCounter == 0 ? interval : 1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Thread interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        });
        fakeDataThread.start();
    }

    private void notifyActivity(final ParticulateMatterSample sample) {
        if (handler != null && sample != null) {
            handler.obtainMessage(DATA_AVAILABLE, sample).sendToTarget();
        }
    }

    public void stopFakeDataThread() {
        fakeDataThread.interrupt();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
