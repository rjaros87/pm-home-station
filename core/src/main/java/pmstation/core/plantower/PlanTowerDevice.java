/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.plantower;

import java.util.Arrays;

/**
 * A class to identify supported PlanTower devices and parse their data frames.
 */
public class PlanTowerDevice {
    
    /**
     * A list of supported PlanTower devices.
     */
    public enum PLANTOWER_MODEL {
        PMS7003(32, 2),
        PMS5003ST(40, 2),
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
            return this.name() + ", data frame size: " + this.dataLength;
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
     * @param sampleData a sample data to work out the model, sample data should be at least 2 times the max of expected data frame size
     */
    public PlanTowerDevice(byte[] sampleData) {
        model = identifyModel(sampleData, START_CHARACTERS);
    }
    
    public PLANTOWER_MODEL model() {
        return model;
    }
    
    public boolean modelIdentified() {
        return model() != PLANTOWER_MODEL.UNKNOWN;
    }

    public ParticulateMatterSample parse(byte[] dataFrame) {
        ParticulateMatterSample result = null;
        if (!modelIdentified()) {
            return result;
        }
        if (!frameVerified(dataFrame)) {
            return result;
        }

        // if (readBuffer.length == DATA_LENGTH && readBuffer[0] == START_CHARACTERS[0] && readBuffer[1] == START_CHARACTERS[1]) {
        // remark #1: << 8 is ~2 times faster than *0x100 - compiler does not optimize that, not even JIT in runtime
        // remark #2: it's necessary to ensure usigned bytes stays unsigned in java - either by using & 0xFF or Byte#toUnsignedInt (java 8)

        int pm1_0 = ((dataFrame[10] & 0xFF) << 8) + (dataFrame[11] & 0xFF);
        int pm2_5 = ((dataFrame[12] & 0xFF) << 8) + (dataFrame[13] & 0xFF);
        int pm10 = ((dataFrame[14] & 0xFF) << 8) + (dataFrame[15] & 0xFF);

        int hcho = -1;
        int temperature = -1;
        int humidity = -1;
        byte modelVersion = 0;
        byte errCode = 0;
        
        if (model == PLANTOWER_MODEL.PMS5003ST) {
            hcho = ((dataFrame[28] & 0xFF) << 8) + (dataFrame[29] & 0xFF);
            humidity = ((dataFrame[32] & 0xFF) << 8) | (dataFrame[33] & 0xFF);
            // value is signed (can be negative):
            temperature = (short) (dataFrame[30] << 8) + (dataFrame[31] & 0xFF);
            modelVersion = dataFrame[36];
            errCode = dataFrame[37];
        }
        result = new ParticulateMatterSample(pm1_0, pm2_5, pm10, hcho, humidity, temperature, modelVersion, errCode);

        return result;
    }
    
    private boolean frameVerified(byte[] data) {
        boolean result = false;
        int headIndex = indexOfArray(data, START_CHARACTERS);
        if (headIndex == -1 || data.length < model.dataLength()) {
            System.err.println("------------- wrong start or data length"); // TODO temporary debug
            return result;
        }

        byte[] sanitizedData = Arrays.copyOfRange(data, headIndex, headIndex + model.dataLength());

        int checkSum = checksumProvided(sanitizedData);
        int calcdCheckSum = checksum(sanitizedData, model.lastBytesToSkipForChecksum);

        // temporary debug - data[length - 1] should contain error bits  
        if (model == PLANTOWER_MODEL.PMS5003ST) {
            if (sanitizedData[sanitizedData.length - 3] != 0) {
                System.err.println("------------- errcode reported by device " + sanitizedData[sanitizedData.length - 3]); // TODO temporary debug
            }            
        }
        if (checkSum == calcdCheckSum) {
            result = true;
        } else {
            System.err.println("------------- checksum mismatch! " + checkSum + "vs" + calcdCheckSum); // TODO temporary debug
        }
        return result;
    }
    
    private int checksumProvided(byte[] data) {
        return checksumProvided(data, 0, data.length - 2);
    }
    
    private int checksumProvided(byte[] data, int startIdx, int checksumStartPos) {
        System.out.println("startIdx: " + startIdx + ", checksumStartPos: " + checksumStartPos);
        return data.length >= startIdx + checksumStartPos + 1 ?
                ((data[startIdx + checksumStartPos] & 0xFF) << 8) | (data[startIdx + checksumStartPos + 1] & 0xFF) : -1;
    }
    
    private int checksum(byte[] data, int lastBytesToSkip) {
        return checksum(data, 0, data.length, lastBytesToSkip);
    }
    
    private int checksum(byte[] data, int startIdx, int dataSize, int lastBytesToSkip) {
        int result = 0;
        result += data[startIdx + 0] & 0xFF;    // start characters
        result += data[startIdx + 1] & 0xFF;
        result += data[startIdx + 2] & 0xFF;    // data size
        result += data[startIdx + 3] & 0xFF;
        
        int checksumDataLength = ((data[startIdx + 2] & 0xFF) << 8) | (data[startIdx + 3] & 0xFF);
        for (int i = 0; i < checksumDataLength - lastBytesToSkip; i++) {
            result += data[startIdx + 4 + i] & 0xFF;
        }
        return result;
    }
    
    private PLANTOWER_MODEL identifyModel(byte[] sampleData, byte[] needle) {
        PLANTOWER_MODEL result = PLANTOWER_MODEL.UNKNOWN;
        
        for (int searchStartContinue = 0; searchStartContinue < sampleData.length - needle.length;) {
            int firstIdx = indexOfArray(sampleData, searchStartContinue, needle);
            if (firstIdx >= 0) {
                int secondIdx = indexOfArray(sampleData, searchStartContinue + 1, needle);
                if (secondIdx >= 0) {
                    System.out.println(secondIdx);
                    System.out.println(firstIdx);
                    result = PLANTOWER_MODEL.identify(secondIdx - firstIdx);
                    System.out.println(result);
                    // let's verify checksum
                    int checksumProvided = checksumProvided(sampleData, firstIdx, result.dataLength() - 2);
                    int checksumCalcd = checksum(sampleData, firstIdx, result.dataLength(), result.lastBytesToSkipForChecksum());
                    if (checksumProvided == checksumCalcd) {
                        // frame used for device identification has a correct checksum, good 
                        break;
                    } else {
                        // let's continue starting from the second found needle
                        searchStartContinue = secondIdx;
                        System.err.println("------ during model identification found a frame with wrong checksum, lets continue");
                    }
                } else {
                    // second needle not found, bailing out
                    break;
                }
            } else {
                // even first needle not found, bailing out
                break;
            }
        }
        

        return result;
    }
    
    private int indexOfArray(byte[] data, byte[] needle) {
        return indexOfArray(data, 0, needle);
    }
    
    private int indexOfArray(byte[] data, int startAt, byte[] needle) {
        int result = -1;
        boolean found;
        for (int i = startAt; i < data.length - needle.length + 1; ++i) {
            found = true;
            for (int j = 0; j < needle.length; ++j) {
                if (data[i + j] != needle[j]) {
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
