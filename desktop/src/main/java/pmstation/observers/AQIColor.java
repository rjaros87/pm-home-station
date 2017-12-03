/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.observers;

import java.awt.Color;

// taken from Android version of pm-station-usb and slightly modified - kept levels the same though
public enum AQIColor {
    VERY_GOOD(Color.decode("#00ccff"), "Very good"),
    GOOD(Color.decode("#00e400"), "Good"),
    MODERATE(Color.decode("#c0c000"), "Moderate"),
    SATISFACTORY(Color.decode("#ff7e00"), "Satisfactory"),
    BAD(Color.decode("#ff0000"), "Bad"),
    HAZARDOUS(Color.decode("#7e0023"), "Hazardous");

    private final Color color;
    private final String description;

    AQIColor(Color color, String description) {
        this.color = color;
        this.description = description;
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
}
