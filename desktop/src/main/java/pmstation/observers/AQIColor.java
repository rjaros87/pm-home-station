/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import java.awt.Color;

// taken from Android version of pm-home-station and slightly modified - kept levels the same though
public enum AQIColor {
    VERY_GOOD(Color.decode("#00ccff"), "Very good", 0),
    GOOD(Color.decode("#00e400"), "Good", 1),
    MODERATE(Color.decode("#c0c000"), "Moderate", 2),
    SATISFACTORY(Color.decode("#ff7e00"), "Satisfactory", 3),
    BAD(Color.decode("#ff0000"), "Bad", 4),
    HAZARDOUS(Color.decode("#7e0023"), "Hazardous", 5);

    private final Color color;
    private final String description;
    private final int riskLevel;

    AQIColor(Color color, String description, int riskLevel) {
        this.color = color;
        this.description = description;
        this.riskLevel = riskLevel;
    }

    public Color getColor() {
        return color;
    }
    
    public String getDescription() {
        return description;
    }

    public static AQIColor fromPM10Level(int pmLevel) {
        if (pmLevel > 200) {
            return HAZARDOUS;
        } else if (pmLevel > 140) {
            return BAD;
        } else if (pmLevel > 100) {
            return SATISFACTORY;
        } else if (pmLevel > 60) {
            return MODERATE;
        } else if (pmLevel > 20) {
            return GOOD;
        } else {
            return VERY_GOOD;
        }
    }

    public static AQIColor fromPM25Level(int pmLevel) {
        if (pmLevel > 120) {
            return HAZARDOUS;
        } else if (pmLevel > 84) {
            return BAD;
        } else if (pmLevel > 60) {
            return SATISFACTORY;
        } else if (pmLevel > 36) {
            return MODERATE;
        } else if (pmLevel > 12) {
            return GOOD;
        } else {
            return VERY_GOOD;
        }
    }
    
    public boolean worseThan(AQIColor other) {
        return this.riskLevel > other.riskLevel;
    }
}
