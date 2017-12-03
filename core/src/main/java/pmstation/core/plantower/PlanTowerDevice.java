package pmstation.core.plantower;

public class PlanTowerDevice {
    //TODO: read values from config file

    public static final int DATA_LENGTH = 32;
    public static final byte[] MODE_WAKEUP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x01, 0x01, 0x74};
    public static final byte[] MODE_SLEEP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x73};
    public static final byte[] MODE_ACTIVE = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x71};
    public static final byte[] MODE_PASSIVE = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x70};
    private static final byte[] START_CHARACTERS = {0x42, 0x4d};

    private PlanTowerDevice() {
        // empty
    }

    private static int indexOfArray(byte[] sampleArray, byte[] needle) {
        for (int i = 0; i < sampleArray.length - needle.length + 1; ++i) {
            boolean found = true;
            for (int j = 0; j < needle.length; ++j) {
                if (sampleArray[i + j] != needle[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    public static ParticulateMatterSample parse(byte[] readBuffer) {
        int headIndex = indexOfArray(readBuffer, START_CHARACTERS);

        if (headIndex >= 0 && readBuffer.length >= headIndex + 16) {
//            if (readBuffer.length == DATA_LENGTH && readBuffer[0] == START_CHARACTERS[0] && readBuffer[1] == START_CHARACTERS[1]) {
            // remark #1: << 8 is ~2 times faster than *0x100 - compiler does not optimize that, not even JIT in runtime
            // remark #2: it's necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)
            int pm1_0 = ((readBuffer[10 + headIndex] & 0xFF) << 8) + (readBuffer[11 + headIndex] & 0xFF);
            int pm2_5 = ((readBuffer[12 + headIndex] & 0xFF) << 8) + (readBuffer[13 + headIndex] & 0xFF);
            int pm10 = ((readBuffer[14 + headIndex] & 0xFF) << 8) + (readBuffer[15 + headIndex] & 0xFF);

            return new ParticulateMatterSample(pm1_0, pm2_5, pm10);
        } else {
            //todo, however it might not be needed, since indexOfArray now looks for both chars
//                logger.debug(
//                        "Bad start characters: {}, {}, should be {} {}",
//                        String.format("0x%02X", readBuffer[0]),
//                        String.format("0x%02X", readBuffer[1]),
//                        String.format("0x%02X", START_CHARACTERS[0]),
//                        String.format("0x%02X", START_CHARACTERS[1])
//                            );
        }

        return null;
    }
}
