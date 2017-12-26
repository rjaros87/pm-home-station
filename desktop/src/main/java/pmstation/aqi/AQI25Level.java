/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.aqi;

// taken from Android version of pm-home-station and slightly modified - kept levels the same though
public enum AQI25Level implements AQILevel {
    VERY_GOOD(0, 12, AQIRiskLevel.VERY_GOOD),
    GOOD(12, 36, AQIRiskLevel.GOOD),
    MODERATE(36, 60, AQIRiskLevel.MODERATE),
    SATISFACTORY(60, 84, AQIRiskLevel.SATISFACTORY),
    BAD(84, 120, AQIRiskLevel.BAD),
    HAZARDOUS(120, Integer.MAX_VALUE, AQIRiskLevel.HAZARDOUS);

    private final int min, max;
    private final AQIRiskLevel riskLevel;

    AQI25Level(int min, int max, AQIRiskLevel riskLevel) {
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

    public static AQI25Level fromValue(int pmLevel) {
        AQI25Level result = HAZARDOUS;
        for (AQI25Level level : values()) {
            if (pmLevel > level.min && pmLevel <= level.max) {
                result = level;
            }
        }
        return result;
    }
    
    public static AQI25Level fromRiskLevel(AQIRiskLevel riskLevel) {
        AQI25Level result = HAZARDOUS;
        for (AQI25Level level : values()) {
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
