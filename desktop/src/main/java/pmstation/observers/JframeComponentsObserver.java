package pmstation.observers;

import pmstation.plantower.ParticulateMatterSample;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class JframeComponentsObserver implements PlanTowerObserver {
    private JLabel deviceStatus, measurmentTime, pm1_0, pm2_5, pm10;
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                                            Locale.getDefault());

    public void setJframeComponents(HashMap<String, JLabel> components) {
        deviceStatus = get(components, "deviceStatus");
        measurmentTime = get(components, "measurmentTime");
        pm1_0 = get(components, "pm1_0");
        pm2_5 = get(components, "pm2_5");
        pm10 = get(components, "pm10");
    }

    @Override
    public void notify(ParticulateMatterSample sample) {
        if (sample == null) {
            deviceStatus.setText("Status: sensor not ready");
        } else {
            String unit = " \u03BCg/m\u00B3";
            deviceStatus.setText("Status: Measuring ...");
            measurmentTime.setText(dateFormat.format(sample.getDate()));
            pm1_0.setText(String.valueOf(sample.getPm1_0()) + unit);
            pm2_5.setText(String.valueOf(sample.getPm2_5()) + unit);
            pm10.setText(String.valueOf(sample.getPm10()) + unit);
        }
    }
    
    private JLabel get(HashMap<String, JLabel> components, String name) {
    		JLabel component = components.get(name);
    		if (component == null) {
    			throw new IllegalArgumentException("Component of name: " + name + " not found! Going down.");
    		}
    		return component;
    }
}
