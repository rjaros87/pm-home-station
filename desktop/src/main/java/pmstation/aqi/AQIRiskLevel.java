/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.aqi;

public enum AQIRiskLevel {
    VERY_GOOD("Very good", 0),
    GOOD("Good", 1),
    MODERATE("Moderate", 2),
    SATISFACTORY("Satisfactory", 3),
    BAD("Bad", 4),
    HAZARDOUS("Hazardous", 5);

    private final String description;
    private final int riskLevel;

    AQIRiskLevel(String description, int riskLevel) {
        this.description = description;
        this.riskLevel = riskLevel;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean worseThan(AQIRiskLevel other) {
        return this.riskLevel > other.riskLevel;
    }
}
