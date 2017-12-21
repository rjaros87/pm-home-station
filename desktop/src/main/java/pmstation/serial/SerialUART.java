package pmstation.serial;

import com.fazecast.jSerialComm.SerialPort;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            logger.warn("No serial ports available!");
            return false;
        }
        logger.debug("Got {} serial ports available", ports.length);
        int portToUse = -1;

        for (int i = 0; i < ports.length; i++) {
            SerialPort sp = ports[i];
            logger.debug("\t- {}, {}", sp.getSystemPortName(), sp.getDescriptivePortName());
            if (isSerialPort(sp)) {
                portToUse = i;
            }
        }
        if (portToUse < 0) {
            logger.warn("No relevant serial usb found on this system!");
            return false;
        }
        comPort = ports[portToUse];
        logger.info("Going to use the following port: {}", comPort.getSystemPortName());

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
        while ((availBytes = comPort.bytesAvailable()) > 0) {
            byte[] ignoredBuffer = new byte[PlanTowerDevice.DATA_LENGTH]; 
            comPort.readBytes(ignoredBuffer, Math.min(availBytes, ignoredBuffer.length));
        }
        logger.trace("Going to read bytes, bytes avail now: {}", comPort.bytesAvailable());

        // now, when avail bytes is 0 we can start waiting for fresh data
        comPort.readBytes(readBuffer, readBuffer.length);
        logger.trace("ReadBytes:\n{}, bytes available after read: {}", SerialUARTUtils.bytesToHexString(readBuffer), comPort.bytesAvailable());

        return readBuffer;
    }

    public void writeBytes(byte[] writeBuffer) {
        logger.trace("WriteBytes:\n{}", SerialUARTUtils.bytesToHexString(writeBuffer));
        comPort.writeBytes(writeBuffer, writeBuffer.length);
    }

    public boolean isConnected() {
        return comPort != null && comPort.isOpen();
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
                portDesc.contains("pmsensor")   // TODO make the name configurable (custom name for BT HC-05/HC-06 or even normal serial)
        );
    }
}
