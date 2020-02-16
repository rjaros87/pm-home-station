/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.plantower;

public class PlanTowerDevice {
    
    public enum PLANTOWER_MODEL {
        PMS7003(32, 2),
        PMS5003ST(40, 3),
        UNKNOWN(0, -1);
        
        private int dataLength;
        private int lastBytesToSkipForChecksum;
        
        PLANTOWER_MODEL(int dataLength, int lastBytesToSkipForChecksum) {
            this.dataLength = dataLength;
            this.lastBytesToSkipForChecksum = lastBytesToSkipForChecksum;
        }
        
        public int dataLength() {
            return dataLength;
        }
        
        public int lastBytesToSkipForChecksum() {
            return lastBytesToSkipForChecksum;
        }
        
        public static PLANTOWER_MODEL identify(int dataLength) {
            PLANTOWER_MODEL result = UNKNOWN;
            for (PLANTOWER_MODEL model : PLANTOWER_MODEL.values()) {
                if (model.dataLength() == dataLength) {
                    result = model;
                    break;
                }
            }
            return result;
        }
        
        @Override
        public String toString() {
            return this.name() + ", data size: " + this.dataLength;
        }
    }
    
    //TODO: read values from config file
    public static final byte[] MODE_WAKEUP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x01, 0x01, 0x74};
    public static final byte[] MODE_SLEEP = {0x42, 0x4d, (byte) 0xe4, 0x00, 0x00, 0x01, 0x73};
    public static final byte[] MODE_ACTIVE = {0x42, 0x4d, (byte) 0xe1, 0x00, 0x01, 0x01, 0x71};
    public static final byte[] MODE_PASSIVE = {0x42, 0x4d, (byte) 0xe1, 0x00, 0x00, 0x01, 0x70};
    public static final byte[] START_CHARACTERS = {0x42, 0x4d};
    public static final byte[] CMD_READ_REQ = {0x42, 0x4d, (byte) 0xe2, 0x00, 0x00, 0x01, 0x71};

    /**
     * Identified PlanTower model.
     */
    private PLANTOWER_MODEL model = null;
    
    /**
     * Initializes PlanTower parser (based on sample data to distinguish the PM model).
     * @param sampleArray a sample data to work out the model
     */
    public PlanTowerDevice(byte[] sampleArray) {
        model = identifyModel(sampleArray, START_CHARACTERS);
    }
    
    public PLANTOWER_MODEL model() {
        return model;
    }
    
    public boolean modelIdentified() {
        return model() != PLANTOWER_MODEL.UNKNOWN;
    }

    public ParticulateMatterSample parse(byte[] readBuffer) {
        ParticulateMatterSample result = null;
        if (!modelIdentified()) {
            return result;
        }
        if (!frameVerified(readBuffer)) {
            return result;
        }

        // if (readBuffer.length == DATA_LENGTH && readBuffer[0] == START_CHARACTERS[0] && readBuffer[1] == START_CHARACTERS[1]) {
        // remark #1: << 8 is ~2 times faster than *0x100 - compiler does not optimize that, not even JIT in runtime
        // remark #2: it's necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)

        int pm1_0 = ((readBuffer[10] & 0xFF) << 8) + (readBuffer[11] & 0xFF);
        int pm2_5 = ((readBuffer[12] & 0xFF) << 8) + (readBuffer[13] & 0xFF);
        int pm10 = ((readBuffer[14] & 0xFF) << 8) + (readBuffer[15] & 0xFF);

        int hcho = -1;
        int temperature = -1;
        int humidity = -1;
        byte modelVersion = 0;
        byte errCode = 0;
        
        if (model == PLANTOWER_MODEL.PMS5003ST) {
            hcho = ((readBuffer[28] & 0xFF) << 8) + (readBuffer[29] & 0xFF);
            humidity = ((readBuffer[32] & 0xFF) << 8) | (readBuffer[33] & 0xFF);
            // value is signed (can be negative):
            temperature = (short) (readBuffer[30] << 8) + (readBuffer[31] & 0xFF);
            modelVersion = readBuffer[36];
            errCode = readBuffer[37];
        }
        result = new ParticulateMatterSample(pm1_0, pm2_5, pm10, hcho, humidity, temperature, modelVersion, errCode);

        return result;
    }
    
    private boolean frameVerified(byte[] data) {
        boolean result = false;
        if (indexOfArray(data, START_CHARACTERS) != 0 || data.length != model.dataLength()) {
            System.err.println("------------- wrong start or data length"); // TODO temporary debug
            return result;
        }

        int checkSum = ((data[data.length - 2] & 0xFF) << 8) | (data[data.length - 1] & 0xFF);
        int calcdCheckSum = checksum(data, model.lastBytesToSkipForChecksum);
        
        if (data[37] != 0) {
            System.err.println("------------- errcode reported by device " + data[37]); // TODO temporary debug
        }
        if (checkSum == calcdCheckSum) {
            result = true;
        } else {
            System.err.println("------------- checksum mismatch! " + checkSum + "vs" + calcdCheckSum); // TODO temporary debug
        }
        return result;
    }
    
    private int checksum(byte[] data, int lastBytesToSkip) {
        int result = 0;
        result += data[0] & 0xFF;
        result += data[1] & 0xFF;
        
        int dataLength = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        for (int i = 0; i <= dataLength - lastBytesToSkip; i++) {
            result += data[3 + i] & 0xFF;
        }
        return result;
    }
    
    private PLANTOWER_MODEL identifyModel(byte[] sampleArray, byte[] needle) {
        PLANTOWER_MODEL result = PLANTOWER_MODEL.UNKNOWN;
        
        int firstIdx = indexOfArray(sampleArray, needle);
        if (firstIdx >= 0 && sampleArray.length > firstIdx) {
            int secondIdx = indexOfArray(sampleArray, firstIdx + 1, needle);
            if (secondIdx >= 0) {
                result = PLANTOWER_MODEL.identify(secondIdx - firstIdx);
            }
        }
        return result;
    }
    
    private int indexOfArray(byte[] sampleArray, byte[] needle) {
        return indexOfArray(sampleArray, 0, needle);
    }
    
    private int indexOfArray(byte[] sampleArray, int startAt, byte[] needle) {
        int result = -1;
        boolean found;
        for (int i = startAt; i < sampleArray.length - needle.length + 1; ++i) {
            found = true;
            for (int j = 0; j < needle.length; ++j) {
                if (sampleArray[i + j] != needle[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                result = i;
                break;
            }
        }
        return result;
    }
}
