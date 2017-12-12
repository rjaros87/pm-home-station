package pmstation.plantower;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Sanchin on 03.12.2017.
 */

public class Settings {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat dateFormatTimeOnly = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
}
