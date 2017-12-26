/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.aqi;

import java.awt.Color;

public enum AQIColor {
    VERY_GOOD("#00ccff", AQIRiskLevel.VERY_GOOD),
    GOOD("#00e400", AQIRiskLevel.GOOD),
    MODERATE("#c0c000", AQIRiskLevel.MODERATE),
    SATISFACTORY("#ff7e00", AQIRiskLevel.SATISFACTORY),
    BAD("#ff0000", AQIRiskLevel.BAD),
    HAZARDOUS("#7e0023", AQIRiskLevel.HAZARDOUS);

    private final Color color;
    private final AQIRiskLevel riskLevel;

    AQIColor(String colorHex, AQIRiskLevel riskLevel) {
        this.color = Color.decode(colorHex);
        this.riskLevel = riskLevel;
    }

    public Color getColor() {
        return color;
    }
    
    public static AQIColor fromLevel(AQILevel level) {
        AQIColor result = HAZARDOUS;
        for (AQIColor color : values()) {
            if (color.riskLevel == level.getRiskLevel()) {
                result = color;
            }
        }
        return result;
    }
    
    public static AQIColor fromRiskLevel(AQIRiskLevel riskLevel) {
        AQIColor result = HAZARDOUS;
        for (AQIColor level : values()) {
            if (level.riskLevel == riskLevel) {
                result = level;
            }
        }
        return result;
    }
    
    public boolean worseThan(AQIColor other) {
        return this.riskLevel.worseThan(other.riskLevel);
    }
}
