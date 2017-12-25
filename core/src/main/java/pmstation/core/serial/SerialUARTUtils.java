/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.serial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class SerialUARTUtils {
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        try (Formatter formatter = new Formatter(sb)) {;
            for (byte b : bytes) {
                formatter.format("0x%02X ", b);
            }
    
            return sb.toString();
        }
    }

    public static void simpleLog(String message, String logLevel, Class<?> className) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        System.out.println(
                dateFormat.format(new Date()) + "[" +
                        Thread.currentThread().getName() + "] " + logLevel + " " + className + " " +
                        message);
    }
}
