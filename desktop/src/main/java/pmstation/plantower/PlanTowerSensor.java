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
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.serial.SerialUART;

public class PlanTowerSensor {
    
    private static final long DEFAULT_INTERVAL = 3000L;

    private List<IPlanTowerObserver> planTowerObserver;
    private Thread measurementsThread;
    private SerialUART serialUART;

    public PlanTowerSensor() {
        serialUART = new SerialUART();
        planTowerObserver = new ArrayList<>();
    }

    public boolean connectDevice() {
        boolean openPort = serialUART.openPort();
        if (openPort) {
            serialUART.writeBytes(PlanTowerDevice.MODE_WAKEUP);
        }
        return openPort;
    }

    public void disconnectDevice() {
        if (measurementsThread != null && !measurementsThread.isInterrupted()) {
            measurementsThread.interrupt();
        }
        if (serialUART.isConnected()) {
            serialUART.writeBytes(PlanTowerDevice.MODE_SLEEP);
            serialUART.closePort();
        }
    }

    public void startMeasurements() {
        startMeasurements(DEFAULT_INTERVAL);
    }
    
    public void startMeasurements(long interval) {
        measurementsThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] readBuffer;
                    readBuffer = serialUART.readBytes(2 * PlanTowerDevice.DATA_LENGTH);
//                    int headIndex = indexOfArray(readBuffer, START_CHARACTERS);
//
//                    if (headIndex > 0) {
//                        serialUART.readBytes(headIndex);
//                        readBuffer = serialUART.readBytes(readBuffer.length);
//                    }

                    notifyAllObservers(PlanTowerDevice.parse(readBuffer));
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        });

        measurementsThread.start();
    }

    public void addObserver(IPlanTowerObserver observer) {
        planTowerObserver.add(observer);
    }

    private void notifyAllObservers(ParticulateMatterSample particulateMatterSample) {
        for (IPlanTowerObserver observer : planTowerObserver) {
            observer.onNewValue(particulateMatterSample);
        }
    }
}
