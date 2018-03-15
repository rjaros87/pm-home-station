/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.plantower;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.core.plantower.PlanTowerDevice;
import pmstation.serial.SerialUART;

public class PlanTowerSensor {
    
    private static final Logger logger = LoggerFactory.getLogger(PlanTowerSensor.class);
    
    private static final long DEFAULT_INTERVAL = 3000L;

    private final List<IPlanTowerObserver> planTowerObserver;
    private final SerialUART serialUART;
    private final ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> scheduledMeasurements = null;
    private long interval = -1;

    public PlanTowerSensor() {
        serialUART = new SerialUART();
        planTowerObserver = new ArrayList<>();
        scheduledExecutor = Executors.newScheduledThreadPool(2);
    }

    public boolean connectDevice() {
        notifyAboutConnecting();
        boolean openPort = serialUART.openPort();
        if (openPort) {
            logger.debug("Waking up the device...");
            serialUART.writeBytes(PlanTowerDevice.MODE_WAKEUP);
            delay();
            serialUART.writeBytes(PlanTowerDevice.MODE_ACTIVE);
            delay();
            notifyAboutConnection();
        } else {
            disconnectDevice();
        }
        return openPort;
    }

    public void disconnectDevice() {
        notifyAboutDisconnecting();
        if (scheduledMeasurements != null) {
            scheduledMeasurements.cancel(true);
        }
        if (serialUART.isConnected()) {
            logger.debug("Sleeping the device...");
            serialUART.writeBytes(PlanTowerDevice.MODE_PASSIVE);
            delay();    // seems to be required... otherwise it won't go to sleep
            serialUART.writeBytes(PlanTowerDevice.MODE_SLEEP);
            delay();
            serialUART.closePort();
        }
        notifyAboutDisconnection();
    }

    public void startMeasurements() {
        startMeasurements(DEFAULT_INTERVAL);
    }
    
    public synchronized void startMeasurements(long interval) {
        if (scheduledMeasurements != null && !scheduledMeasurements.isDone()) {
            if (this.interval == interval) {
                logger.info("Ignoring re-scheduling since the interval is the same as previous one");
                return;
            } else {
                logger.info("Going to cancel scheduled measurement task as interval change is requested");
                scheduledMeasurements.cancel(true);
            }
        }
        logger.info("Scheduling measurements at interval: {}ms...", interval);
        scheduledMeasurements = scheduledExecutor.scheduleAtFixedRate(getMeasurementsRunnable(), 2000, interval, TimeUnit.MILLISECONDS);
        this.interval = interval;
    }

    public void addObserver(IPlanTowerObserver observer) {
        planTowerObserver.add(observer);
    }
    
    public boolean isConnected() {
        return serialUART.isConnected();
    }
    
    public String portDetails() {
        return serialUART.portDetails();
    }
    
    public void waitUntilDisconnected() {
        Runnable runnable = () -> {
            disconnectDevice();
        };
        Thread shutdownHook = new Thread(runnable);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        while (true) {
            try {
                if (scheduledMeasurements.isCancelled() || scheduledMeasurements.isDone()) {
                    logger.info("Measurements finished for whatever reason, bailing out");
                    return;
                }
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
        }
    }
    
    private Runnable getMeasurementsRunnable() {
        return () -> {
            try {
                if (serialUART.isConnected()) {
                    byte[] readBuffer = serialUART.readBytes(PlanTowerDevice.DATA_LENGTH);
                    if (readBuffer != null) {
                        notify(PlanTowerDevice.parse(readBuffer));
                    } else {
                        logger.info("Unable to read bytes from the sensor (a sudden device disconnection?)");
                        scheduledMeasurements.cancel(false);
                        notifyAboutDisconnection();
                    }
                } else {
                    logger.info("Sensor is disconnected");
                    scheduledMeasurements.cancel(false);
                    notifyAboutDisconnection();
                }
            } catch (Exception e) {
                logger.warn("Caught Exception in scheduled executor - assuming a sudden disconnection", e);
                notifyAboutDisconnection();
                scheduledMeasurements.cancel(true);
            } catch (Throwable t) {
                logger.warn("Caught Throwable in scheduled executor - no further measurements will take place unless manual reconnect", t);
                notifyAboutDisconnection();
                scheduledMeasurements.cancel(true);
                throw t;
            }
        };
    }

    private void notify(ParticulateMatterSample particulateMatterSample) {
        if (particulateMatterSample != null) {
            for (IPlanTowerObserver observer : planTowerObserver) {
                observer.update(particulateMatterSample);
            }
        }
    }

    private void notifyAboutConnecting() {
        for (IPlanTowerObserver observer : planTowerObserver) {
            observer.connecting();
        }
    }

    private void notifyAboutConnection() {
        for (IPlanTowerObserver observer : planTowerObserver) {
            observer.connected();
        }
    }
    
    private void notifyAboutDisconnecting() {
        for (IPlanTowerObserver observer : planTowerObserver) {
            observer.disconnecting();
        }
    }

    private void notifyAboutDisconnection() {
        for (IPlanTowerObserver observer : planTowerObserver) {
            observer.disconnected();
        }
    }
    
    private void delay() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
