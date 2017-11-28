package pmstation.serial;

import com.fazecast.jSerialComm.*;
import pmstation.core.serial.SerialUARTInterface;
import pmstation.core.serial.SerialUARTUtils;

public class SerialUART implements SerialUARTInterface {
    private static final int TIMEOUT_READ = 2000; //[ms]
    private static final int TIMEOUT_WRITE = 2000; //[ms]
    private static final int BAUD_RATE = 9600;

    private SerialPort serialPort;

    public boolean openPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            SerialUARTUtils.simpleLog("No serial ports available!", "WARN", SerialUART.class);
            return false;
        }

        SerialUARTUtils.simpleLog("Got " + ports.length + " serial ports available", "DEBUG", SerialUART.class);
        int portToUse = -1;

        for (int i = 0; i < ports.length; i++) {
            SerialPort sp = ports[i];
            SerialUARTUtils.simpleLog("\t- " + sp.getSystemPortName() + " " + sp.getDescriptivePortName(), "DEBUG", SerialUART.class);

            if (isSerialPort(sp)) {
                portToUse = i;
            }
        }

        if (portToUse < 0) {
            SerialUARTUtils.simpleLog("No relevant serial usb found on this system!", "WARN", SerialUART.class);
            return false;
        }

        serialPort = SerialPort.getCommPorts()[portToUse];
        SerialUARTUtils.simpleLog("Going to use the following port: {}" + serialPort.getSystemPortName(), "INFO", SerialUART.class);

        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        serialPort.setComPortParameters(BAUD_RATE, 8,
                SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                TIMEOUT_READ,
                TIMEOUT_WRITE
        );
        SerialUARTUtils.simpleLog("Going to open the port...", "DEBUG", SerialUART.class);
        boolean result = serialPort.openPort();
        SerialUARTUtils.simpleLog("Port opened? {}" + result, "DEBUG", SerialUART.class);
        return result;
    }

    public void closePort() {
        if (serialPort != null) {
            serialPort.closePort();
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            serialPort.removeDataListener();
            SerialUARTUtils.simpleLog("Going to close the port...", "DEBUG", SerialUART.class);
            boolean result = serialPort.closePort();
            SerialUARTUtils.simpleLog("Port closed? " + result, "DEBUG", SerialUART.class);
        }
    }

    public byte[] readBytes(int dataLenght) {
        byte[] readBuffer = new byte[dataLenght];
        serialPort.readBytes(readBuffer, readBuffer.length);
        SerialUARTUtils.simpleLog("ReadBuffer:\n " + SerialUARTUtils.bytesToHexString(readBuffer), "DEBUG", SerialUART.class);

        return readBuffer;
    }

    public void writeBytes(byte[] writeBuffer) {
        SerialUARTUtils.simpleLog("WriteBuffer:\n {}" + SerialUARTUtils.bytesToHexString(writeBuffer), "DEBUG", SerialUART.class);
        serialPort.writeBytes(writeBuffer, writeBuffer.length);
    }

    private boolean isSerialPort(SerialPort sp) {
        if (System.getProperty("os.name").toLowerCase().contains("mac") || System.getProperty("os.name").toLowerCase().contains("darwin")) {
            return sp.getSystemPortName().startsWith("cu") && sp.getSystemPortName().toLowerCase().contains("usbserial");
        } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return sp.getDescriptivePortName().toLowerCase().contains("serial");
        }
//        else if (isLinux) {
//          return check ports
//        }

        return false;
    }
}
