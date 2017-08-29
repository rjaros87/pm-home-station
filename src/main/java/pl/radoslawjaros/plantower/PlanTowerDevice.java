package pl.radoslawjaros.plantower;

import com.fazecast.jSerialComm.*;

public class PlanTowerDevice {
    //TODO: read values from config file
    static final private int TIMEOUT_READ = 2000; //[ms]
    static final private int TIMEOUT_WRITE = 2000; //[ms]
    static final private int DATA_LENGTH = 32; //[ms]
    static final private byte START_CHARACTERS = 0x42;
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
        if (SerialPort.getCommPorts().length == 0) {
            return false;
        }
        comPort = SerialPort.getCommPorts()[0];
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


    public ParticulateMatterSample read() {
        try {
            byte[] readBuffer = new byte[DATA_LENGTH];
            int numRead = this.comPort.readBytes(readBuffer, readBuffer.length);
            int headIndex = indexOfArray(readBuffer, START_CHARACTERS);

            if (headIndex > 0) {
                this.comPort.readBytes(readBuffer, headIndex);
                numRead = this.comPort.readBytes(readBuffer, readBuffer.length);
            }

            if (numRead == DATA_LENGTH && readBuffer[0] == START_CHARACTERS) {
                int pm1_0 = readBuffer[10] * 0x100 + readBuffer[11];
                int pm2_5 = readBuffer[12] * 0x100 + readBuffer[13];
                int pm10 = readBuffer[14] * 0x100 + readBuffer[15];

                return new ParticulateMatterSample(pm1_0, pm2_5, pm10);
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
