/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.plantower;

import java.util.ArrayList;
import java.util.List;

import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.core.plantower.PlanTowerDevice;
import pmstation.observers.PlanTowerObserver;
import pmstation.serial.SerialUART;

public class PlanTowerSensor {
    
    private static final long DEFAULT_INTERVAL = 3000L;

    private PlanTowerDevice planTowerDevice;
    private List<PlanTowerObserver> planTowerObserver;
    private Thread measurementsThread;
    private SerialUART serialUART;

    public PlanTowerSensor() {
        serialUART = new SerialUART();
        planTowerDevice = new PlanTowerDevice(serialUART);
        planTowerObserver = new ArrayList<PlanTowerObserver>();
    }

    public boolean connectDevice() {
        boolean openPort = serialUART.openPort();
        planTowerDevice.runCommand(PlanTowerDevice.MODE_WAKEUP);
        return openPort;
    }

    public void disconnectDevice() {
        if (measurementsThread != null && !measurementsThread.isInterrupted()) {
            measurementsThread.interrupt();
        }
        planTowerDevice.runCommand(PlanTowerDevice.MODE_SLEEP);
        serialUART.closePort();
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
