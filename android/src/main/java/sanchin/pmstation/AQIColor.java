package sanchin.pmstation;

import android.graphics.Color;

public enum AQIColor {
    VERY_GOOD(Color.parseColor("#00ccff"), 0f),
    GOOD(Color.parseColor("#00e400"), 0.2f),
    MODERATE(Color.parseColor("#ffff00"), 0.4f),
    SATISFACTORY(Color.parseColor("#ff7e00"), 0.6f),
    BAD(Color.parseColor("#ff0000"), 0.8f),
    HAZARDOUS(Color.parseColor("#7e0023"), 0.9f);

    private final int color;
    private final float alpha;

    AQIColor(int color, float alpha) {
        this.color = color;
        this.alpha = alpha;
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

    public float getAlpha() {
        return alpha;
    }
}
