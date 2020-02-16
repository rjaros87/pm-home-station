/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.plantower;

public class PlanTowerDevice {
    
    public enum PLANTOWER_MODEL {
        PMS7003(32),
        PMS5003ST(40),
        UNKNOWN(0);
        
        private int dataLength;
        
        PLANTOWER_MODEL(int dataLength) {
            this.dataLength = dataLength;
        }
        
        public int dataLength() {
            return dataLength;
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
        int headIndex = indexOfArray(readBuffer, START_CHARACTERS);
        ParticulateMatterSample result = null;

        if (modelIdentified() && headIndex >= 0 && readBuffer.length >= headIndex + 16) {

//            if (readBuffer.length == DATA_LENGTH && readBuffer[0] == START_CHARACTERS[0] && readBuffer[1] == START_CHARACTERS[1]) {
            // remark #1: << 8 is ~2 times faster than *0x100 - compiler does not optimize that, not even JIT in runtime
            // remark #2: it's necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)
            int pm1_0 = ((readBuffer[10 + headIndex] & 0xFF) << 8) + (readBuffer[11 + headIndex] & 0xFF);
            int pm2_5 = ((readBuffer[12 + headIndex] & 0xFF) << 8) + (readBuffer[13 + headIndex] & 0xFF);
            int pm10 = ((readBuffer[14 + headIndex] & 0xFF) << 8) + (readBuffer[15 + headIndex] & 0xFF);

            int hcho = -1;
            int temperature = -1;
            int humidity = -1;

            if (model == PLANTOWER_MODEL.PMS5003ST && readBuffer.length >= headIndex + 34) {
                hcho = ((readBuffer[28 + headIndex] & 0xFF) << 8) + (readBuffer[29 + headIndex] & 0xFF);
                humidity = (short)((readBuffer[32 + headIndex] & 0xFF) << 8) | (readBuffer[33 + headIndex] & 0xFF);
                // value is signed (can be negative):
                temperature = (readBuffer[30 + headIndex] << 8) + (readBuffer[31 + headIndex] & 0xFF);
            } else {    // left for debug pruposes... 
                if (model == PLANTOWER_MODEL.PMS5003ST) {
                    System.out.println("**** buffer too small as head idx found != 0? - data length: " + readBuffer.length + ", headidx: " + headIndex);
                }
            }

            result = new ParticulateMatterSample(pm1_0, pm2_5, pm10, hcho, humidity, temperature);
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
