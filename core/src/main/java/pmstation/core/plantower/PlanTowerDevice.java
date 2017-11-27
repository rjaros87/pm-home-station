package pmstation.core.plantower;

import pmstation.core.serial.SerialUARTInterface;
import pmstation.core.serial.SerialUARTUtils;

public class PlanTowerDevice {
    //TODO: read values from config file

    private static final int DATA_LENGTH = 32; //[ms]
    private static final byte[] START_CHARACTERS = {0x42, 0x4d};
    public static final byte[] MODE_WAKEUP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x01, 0x01, 0x74};
    public static final byte[] MODE_SLEEP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x73};
    public static final byte[] MODE_ACTIVE = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x71};
    public static final byte[] MODE_PASSIVE = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x70};

    private SerialUARTInterface serialUart;

    public PlanTowerDevice(SerialUARTInterface serialUart) {
        this.serialUart = serialUart;
    }

    public void runCommand(byte[] command) {
        if (serialUart != null) {
            serialUart.writeBytes(command);
        }
    }

    public ParticulateMatterSample read() {
        try {
            byte[] readBuffer;
            readBuffer = serialUart.readBytes(DATA_LENGTH);
            int headIndex = indexOfArray(readBuffer, START_CHARACTERS[0]);

            if (headIndex > 0) {
                serialUart.readBytes(headIndex);
                readBuffer = serialUart.readBytes(readBuffer.length);
            }

            if (readBuffer.length == DATA_LENGTH && readBuffer[0] == START_CHARACTERS[0] && readBuffer[1] == START_CHARACTERS[1]) {
                // remark #1: << 8 is ~2 times faster than *0x100 - compiler does not optimize that, not even JIT in runtime
                // remark #2: it's necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)
                int pm1_0 = (Byte.toUnsignedInt(readBuffer[10]) << 8) + Byte.toUnsignedInt(readBuffer[11]);
                int pm2_5 = (Byte.toUnsignedInt(readBuffer[12]) << 8) + Byte.toUnsignedInt(readBuffer[13]);
                int pm10 = (Byte.toUnsignedInt(readBuffer[14]) << 8) + Byte.toUnsignedInt(readBuffer[15]);

                return new ParticulateMatterSample(pm1_0, pm2_5, pm10);
            } else {
                SerialUARTUtils.simpleLog("Bad start characters: " +
                        String.format("0x%02X", readBuffer[0]) +
                        ", " + String.format("0x%02X", readBuffer[1])
                        + ", should be " +
                        String.format("0x%02X", START_CHARACTERS[0]) +
                        " " + String.format("0x%02X", START_CHARACTERS[1]),
                        "DEBUG",
                        PlanTowerDevice.class);
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
