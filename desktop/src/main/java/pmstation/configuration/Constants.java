/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */

package pmstation.configuration;

import java.util.Locale;

import org.apache.commons.lang3.time.FastDateFormat;

public class Constants {
    
    public static final String PROJECT_NAME = "pm-station-usb";
    public static final String VERSION = "1.0.0 beta"; 
    public static final String PROJECT_URL = "https://github.com/rjaros87/pm-station-usb";
    public static final Locale DEFAULT_LOCALE = Locale.getDefault(); 
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", DEFAULT_LOCALE);
    public static final int DEFAULT_INTERVAL = 3; // in sec
    public static final int MIN_INTERVAL = 2; // in sec
    public static final int MAX_INTERVAL = 3600; // in sec
    
}
