package sanchin.pmstation;

import android.graphics.Color;

/**
 * Created by rgabiga on 04.11.17.
 */

public enum AQIColor {
    VERY_GOOD(Color.parseColor("#00ccff")),
    GOOD(Color.parseColor("#00e400")),
    MODERATE(Color.parseColor("#ffff00")),
    SATISFACTORY(Color.parseColor("#ff7e00")),
    BAD(Color.parseColor("#ff0000")),
    HAZARDOUS(Color.parseColor("#7e0023"));

    private final int color;

    AQIColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
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
