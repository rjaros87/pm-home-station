/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.observers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import javax.swing.JLabel;

import pmstation.configuration.Constants;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.core.plantower.IPlanTowerObserver;

public class LabelObserver implements IPlanTowerObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(LabelObserver.class);
    
    private JLabel deviceStatus, measurementTime, pm1_0, pm2_5, pm10;

    public void setJframeComponents(HashMap<String, JLabel> components) {
        deviceStatus = get(components, "deviceStatus");
        measurementTime = get(components, "measurementTime");
        pm1_0 = get(components, "pm1_0");   // TODO don't use hardcoded labels like that
        pm2_5 = get(components, "pm2_5");
        pm10 = get(components, "pm10");
    }

    @Override
    public void onNewValue(ParticulateMatterSample sample) {
        if (sample == null) {
            deviceStatus.setText("Status: sensor not ready");
        } else {
            String unit = " \u03BCg/m\u00B3";
            deviceStatus.setText("Status: Measuring ...");
            measurementTime.setText(Constants.DATE_FORMAT.format(sample.getDate()));
            pm1_0.setText(String.valueOf(sample.getPm1_0()) + unit);
            pm2_5.setText(String.valueOf(sample.getPm2_5()) + unit);
            pm10.setText(String.valueOf(sample.getPm10()) + unit);
        }
    }

    private JLabel get(HashMap<String, JLabel> components, String name) {
        JLabel component = components.get(name);
        if (component == null) {
            logger.error("Component of name: {} not found! Going down.", name);
            throw new IllegalArgumentException("Component of name: " + name + " not found! Going down.");
        }
        return component;
    }
}
