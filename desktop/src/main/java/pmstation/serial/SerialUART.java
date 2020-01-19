/*
 * pm-home-station
 * 2017-2018 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.serial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

import pmstation.configuration.Config;
import pmstation.core.plantower.PlanTowerDevice;
import pmstation.core.serial.ISerialUART;
import pmstation.core.serial.SerialUARTUtils;

public class SerialUART implements ISerialUART {
    private static final Logger logger = LoggerFactory.getLogger(SerialUART.class);

    //TODO: read values from config file
    private static final int TIMEOUT_READ = 2000; //[ms]
    private static final int TIMEOUT_WRITE = 2000; //[ms]
    private static final int BAUD_RATE = 9600;

    private SerialPort comPort;

    public boolean openPort() {
        Set<SerialPort> ports = new HashSet<>(Arrays.asList(SerialPort.getCommPorts()));
        if (ports.isEmpty()) {
            logger.warn("No serial ports available!");
            return false;
        }
        logger.debug("Got {} serial ports available", ports.size());
        for (SerialPort sp : ports) {
            logger.debug("\t- {}, {}", sp.getSystemPortName(), sp.getDescriptivePortName());
        }
 
        comPort = findPreferredPort(ports); // try preferred ports first
        if (comPort != null) { 
            logger.debug("Preferred serial port found!");
        } else { // try autodetection then
            comPort = autodetectPort(ports);
        }
        
        if (comPort == null) {
            logger.warn("No relevant serial usb found on this system!");
        }
        
        return comPort != null;
    }

    public void closePort() {
        if (comPort != null) {
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            comPort.removeDataListener();
            logger.debug("Going to close the port...");
            boolean result = comPort.closePort();
            logger.debug("Port closed? {}", result);
        }
    }

    public byte[] readBytes(int dataLength) {
        byte[] readBuffer = new byte[dataLength];
        int availBytes = comPort.bytesAvailable();
        if (availBytes < 0) {
            logger.info("Port is not open!");
        }
        logger.trace("Available number of bytes (old data and garbage): {}", availBytes);
        int guard = 0;
        while ((availBytes = comPort.bytesAvailable()) > 0 && guard < 100) {
            byte[] ignoredBuffer = new byte[PlanTowerDevice.DATA_LENGTH]; // TODO: sko? what is this?
            comPort.readBytes(ignoredBuffer, Math.min(availBytes, ignoredBuffer.length));
            guard++;
        }
        logger.trace("Going to read bytes, bytes avail now: {}", comPort.bytesAvailable());

        // now, when avail bytes is 0 we can start waiting for fresh data
        int bytesRead = comPort.readBytes(readBuffer, readBuffer.length);
        
        logger.trace("ReadBytes, count: {}, data: {}\n{}, bytes available after read: {}", bytesRead, SerialUARTUtils.bytesToHexString(readBuffer), comPort.bytesAvailable());

        return bytesRead > 0 ? readBuffer : null;
    }

    public void writeBytes(byte[] writeBuffer) {
        logger.trace("WriteBytes:\n{}", SerialUARTUtils.bytesToHexString(writeBuffer));
        comPort.writeBytes(writeBuffer, writeBuffer.length);
    }

    public boolean isConnected() {
        return comPort != null && comPort.isOpen();
    }

    public String portDetails() {
        String details;
        if (comPort != null && comPort.isOpen()) {
            details = String.format(
                    "<html><table><tr><td><b>%s</b></td><td>%s</td></tr>"
                    + "<tr><td><b>%s</b></td><td>%s</td></tr>"
                    + "<tr><td><b>%s</b></td><td>%d</td></tr></table></html>",
                    "Port name: ",
                    comPort.getSystemPortName(),
                    "Port description: ",
                    comPort.getDescriptivePortName(),
                    "Baud rate: ",
                    comPort.getBaudRate());
        } else {
            details = "Not connected"; 
        }
        return details;
    }
    
    public Set<String> listPorts() {
        Set<String> result = new HashSet<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            return result;
        }
        for (int i = 0; i < ports.length; i++) {
            result.add(ports[i].getSystemPortName());
        }
        return result;
    }
    
    private boolean tryToConnect(SerialPort comPort) {
        logger.info("Going to connect to the following port: {}", comPort.getSystemPortName());

        comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        comPort.setComPortParameters(BAUD_RATE, 8,
                                     SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                TIMEOUT_READ,
                TIMEOUT_WRITE
                                  );

        logger.debug("Going to open the port...");
        boolean result = comPort.openPort();
        logger.debug("Port opened? {}", result);
        return result;

    }
    
    private SerialPort findPreferredPort(Set<SerialPort> ports) {
        SerialPort result = null;
        Set<String> prefPorts = new HashSet<>(Config.instance().to().getList(String.class, Config.Entry.PREFERRED_DEVICES.key(), new ArrayList<String>()));
        
        if (!prefPorts.isEmpty()) {
            List<SerialPort> toRemove = new ArrayList<>();
            logger.debug("Trying to find preferred serial port...");
            for (String prefPort : prefPorts) {
                for (SerialPort port : ports) {
                    String portName = port.getSystemPortName();
                    String regEx = prefPort.replaceAll("\\*", ".*");
                    if (portName.equals(prefPort) || portName.matches(regEx)) {
                        toRemove.add(port);
                        logger.debug("Found matching preferred serial port: {} - trying to connect...", portName);
                        if (tryToConnect(port)) {
                            result = port;
                            break;
                        } else {
                            logger.debug("Unable to connect to preferred port: {}", portName);
                        }
                    }
                }
                if (result != null) {
                    break;
                }
            }
            ports.removeAll(toRemove);
        }
        
        return result;
    }
    
    private SerialPort autodetectPort(Set<SerialPort> ports) {
        SerialPort result = null;
        if (!ports.isEmpty()) {
            logger.debug("Trying to autodetect serial port...");
            for (SerialPort sp : ports) {
                if (isSerialPort(sp)) {
                    if (tryToConnect(sp)) {
                        result = sp;
                        break;
                    } else {
                        logger.debug("Unable to connect to autodetected serial port: {}", sp.getSystemPortName());
                    }
                }
            }
            
        } else {
            logger.debug("No unchecked ports left to try autodetect");
        }
        return result;
    }
    
    private boolean isSerialPort(SerialPort sp) {
        String portName = sp.getSystemPortName().toLowerCase();
        String portDesc = sp.getDescriptivePortName().toLowerCase();
        return (SystemUtils.IS_OS_MAC_OSX && portName.startsWith("cu") && portName.contains("usbserial") ||
                SystemUtils.IS_OS_MAC_OSX && portName.startsWith("cu.hc-0") ||  // Bluetooth uart on Mac
                SystemUtils.IS_OS_WINDOWS && portDesc.contains("serial") ||
                SystemUtils.IS_OS_WINDOWS && portDesc.contains("hc-0") || // Bluetooth uart on Win
                SystemUtils.IS_OS_LINUX && portDesc.contains("usb") && portDesc.contains("serial") || 
                SystemUtils.IS_OS_LINUX && portDesc.contains("hc-0") || // Bluetooth uart on Linux?
                portDesc.contains("pmsensor")
        );
    }
}
