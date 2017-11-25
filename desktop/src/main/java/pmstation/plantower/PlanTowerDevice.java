package pmstation.plantower;

import com.fazecast.jSerialComm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanTowerDevice {

    private static final Logger logger = LoggerFactory.getLogger(PlanTowerDevice.class);

    // TODO use 3rd party lib or move to seprate class and identify all supported OSes
    private static boolean isOSX = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0 || System.getProperty("os.name").toLowerCase().indexOf("darwin") >= 0;
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;

    //TODO: read values from config file
    static final private int TIMEOUT_READ = 2000; //[ms]
    static final private int TIMEOUT_WRITE = 2000; //[ms]
    static final private int DATA_LENGTH = 32; //[ms]
    static final private byte[] START_CHARACTERS = {0x42, 0x4d};
    static final public byte[] MODE_WAKEUP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x01, 0x01, 0x74};
    static final public byte[] MODE_SLEEP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x73};
    static final public byte[] MODE_ACTIVE = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x71};
    static final public byte[] MODE_PASSIVE = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x70};

    private SerialPort comPort;

    public void closePort() {
        if (comPort != null) {
            comPort.closePort();
        }
    }

    public boolean openPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            logger.warn("No serial ports available!");
            return false;
        }
        logger.debug("Got {} serial ports available", SerialPort.getCommPorts().length);
        int portToUse = 0;

        for (int i = 0; i < ports.length; i++) {
            SerialPort sp = ports[i];
            logger.debug("\t- {}, {}", sp.getSystemPortName(), sp.getDescriptivePortName());
            // TODO I don't have linux/windows, so somebody please check and update the names accordingly etc - don't use [0] device for god's sake
            if (isSerialPort(sp)) {
                portToUse = i;
            }
        }
        comPort = SerialPort.getCommPorts()[portToUse];
        logger.info("Going to use the following port: {}", comPort.getSystemPortName());

        comPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                TIMEOUT_READ,
                TIMEOUT_WRITE
        );
        return comPort.openPort();
    }

    public void runCommand(byte[] command) {
        if (comPort != null) {
            comPort.writeBytes(command, command.length);
        }
    }

    public boolean isSerialPort(SerialPort sp) {
        return ((isOSX && sp.getSystemPortName().startsWith("cu") && sp.getSystemPortName().toLowerCase().contains("usbserial")) ||
                (isWindows && sp.getDescriptivePortName().toLowerCase().contains("serial")) // ||
                //(isLinux)
        );
    }


    public ParticulateMatterSample read() {
        try {
            byte[] readBuffer = new byte[DATA_LENGTH];
            int numRead = this.comPort.readBytes(readBuffer, readBuffer.length);
            int headIndex = indexOfArray(readBuffer, START_CHARACTERS[0]);

            if (headIndex > 0) {
                this.comPort.readBytes(readBuffer, headIndex);
                numRead = this.comPort.readBytes(readBuffer, readBuffer.length);
            }

            if (numRead == DATA_LENGTH && readBuffer[0] == START_CHARACTERS[0] && readBuffer[1] == START_CHARACTERS[1]) {
                // remark #1: << 8 is ~2 times faster than *0x100 - compiler does not optimize that, not even JIT in runtime
                // remark #2: it's necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)
                int pm1_0 = (Byte.toUnsignedInt(readBuffer[10]) << 8) + Byte.toUnsignedInt(readBuffer[11]);
                int pm2_5 = (Byte.toUnsignedInt(readBuffer[12]) << 8) + Byte.toUnsignedInt(readBuffer[13]);
                int pm10 = (Byte.toUnsignedInt(readBuffer[14]) << 8) + Byte.toUnsignedInt(readBuffer[15]);

                return new ParticulateMatterSample(pm1_0, pm2_5, pm10);
            } else {
                logger.debug(
                        "Bad start characters: {}, {}, should be {} {}",
                        String.format("0x%02X", readBuffer[0]),
                        String.format("0x%02X", readBuffer[1]),
                        String.format("0x%02X", START_CHARACTERS[0]),
                        String.format("0x%02X", START_CHARACTERS[1])
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private int indexOfArray(byte[] sampleArray, byte needle) {
        for (int i = 0; (i < sampleArray.length); i++) {
            if (sampleArray[i] == needle) {
                return i;
            }
        }

        return -1;
    }
}
