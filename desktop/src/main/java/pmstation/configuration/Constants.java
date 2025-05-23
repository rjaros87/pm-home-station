/*
 * pm-home-station
 * 2017-2018 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation.configuration;

import java.util.Locale;

import org.apache.commons.lang3.time.FastDateFormat;
import pmstation.core.plantower.Unit;

public class Constants {
    
    public static final String PROJECT_NAME = "pm-home-station";
    public static final String VERSION = "1.3.1-alpha";
    public static final String PROJECT_URL = "https://github.com/rjaros87/pm-home-station";
    public static final Locale DEFAULT_LOCALE = Locale.getDefault(); 
    public static final String DEFAULT_ICON = "app-icon.png";
    public static final String MAIN_WINDOW_TITLE = String.format("Particulate Matter home station (v%s)", VERSION);
    
    public static final String GITHUB_LATEST_RELEASE = "https://api.github.com/repos/rjaros87/pm-home-station/releases/latest";
    
    
    public static final String PM_UNITS = Unit.PARTICULATE_MATTER.toString();
    public static final String HHCO_MG_UNITS = Unit.HCHO_MG.toString();
    public static final String HHCO_UG_UNITS = Unit.HCHO_UG.toString();
    public static final String HUMI_UNITS = Unit.HUMIDITY.toString();
    public static final String TEMP_UNITS = Unit.TEMPERATURE.toString();
    
    public static final String HHT_UNITS = HHCO_UG_UNITS + ", " + HUMI_UNITS + ", " + TEMP_UNITS; 
    
    // preferred Window size
    public static final int WINDOW_WIDTH = 494;
    public static final int WINDOW_HEIGHT = 480;
    // min Window size
    public static final int MIN_WINDOW_WIDTH = 470;
    public static final int MIN_WINDOW_HEIGHT = 180;
    
    public static final String MAIN_BG_IMG = "bg.jpg";
    public static final String MAIN_BG_IMG_DARK_MODE = "bg-dark.jpg";

    public static final boolean HIDE_MAIN_WINDOW = false;
    public static final boolean SYSTEM_TRAY = true;
    public static final boolean DARK_MODE = false;
    public static final boolean CHARTS_HORIZONTAL = false;
    public static final boolean CHARTS_MODE_AREA = false;
    
    public static final int CHART_MAX_SAMPLES = 60;
    
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", DEFAULT_LOCALE);
    public static final int DEFAULT_INTERVAL = 3; // in sec
    public static final int MIN_INTERVAL = 2; // in sec
    public static final int MAX_INTERVAL = 3600; // in sec
    
    public static final int DEFAULT_PM25_MAX_SAFE = 25;
    public static final int DEFAULT_PM10_MAX_SAFE = 50;

}
