/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.plantower;

import pmstation.observers.PlanTowerObserver;

import java.util.ArrayList;
import java.util.List;

public class PlanTowerSensor {
    
    private static final long DEFAULT_INTERVAL = 3000L;

    private PlanTowerDevice planTowerDevice;
    private List<PlanTowerObserver> planTowerObserver;
    private Thread measurementsThread;

    public PlanTowerSensor() {
        planTowerDevice = new PlanTowerDevice();
        planTowerObserver = new ArrayList<PlanTowerObserver>();
    }

    public boolean connectDevice() {
        boolean openPort = planTowerDevice.openPort();
        planTowerDevice.runCommand(PlanTowerDevice.MODE_WAKEUP);
        return openPort;
    }

    public void disconnectDevice() {
        if (measurementsThread != null && !measurementsThread.isInterrupted()) {
            measurementsThread.interrupt();
        }
        planTowerDevice.runCommand(PlanTowerDevice.MODE_SLEEP);
        planTowerDevice.closePort();
    }

    public void startMeasurements() {
        startMeasurements(DEFAULT_INTERVAL);
    }
    
    public void startMeasurements(long interval) {
        measurementsThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    notifyAllObservers(planTowerDevice.read());
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        });

        measurementsThread.start();
    }

    public void addObserver(PlanTowerObserver observer) {
        planTowerObserver.add(observer);
    }

    public void notifyAllObservers(ParticulateMatterSample particulateMatterSample) {
        for (PlanTowerObserver observer : planTowerObserver) {
            observer.notify(particulateMatterSample);
        }
    }
}
