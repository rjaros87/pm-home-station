/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.aqi;

// taken from Android version of pm-home-station and slightly modified - kept levels the same though
public enum AQI10Level implements AQILevel {
    VERY_GOOD(0, 20, AQIRiskLevel.VERY_GOOD),
    GOOD(20, 60, AQIRiskLevel.GOOD),
    MODERATE(60, 100, AQIRiskLevel.MODERATE),
    SATISFACTORY(100, 140, AQIRiskLevel.SATISFACTORY),
    BAD(140, 200, AQIRiskLevel.BAD),
    HAZARDOUS(200, Integer.MAX_VALUE, AQIRiskLevel.HAZARDOUS);

    private final int min, max;
    private final AQIRiskLevel riskLevel;

    AQI10Level(int min, int max, AQIRiskLevel riskLevel) {
        this.min = min;
        this.max = max;
        this.riskLevel = riskLevel;
    }

    public int getMax() {
        return max;
    }
    
    public int getMin() {
        return min;
    }
    
    public AQIRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getDescription() {
        return riskLevel.getDescription();
    }

    public static AQI10Level fromValue(int pmLevel) {
        AQI10Level result = HAZARDOUS;
        for (AQI10Level level : values()) {
            if (pmLevel > level.min && pmLevel <= level.max) {
                result = level;
            }
        }
        return result;
    }
    
    public static AQI10Level fromRiskLevel(AQIRiskLevel riskLevel) {
        AQI10Level result = HAZARDOUS;
        for (AQI10Level level : values()) {
            if (level.getRiskLevel() == riskLevel) {
                result = level;
            }
        }
        return result;
    }
    
    public boolean worseThan(AQILevel other) {
        return this.riskLevel.worseThan(other.getRiskLevel());
    }
}
