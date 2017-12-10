/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.observers;

import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;

public class LabelObserver implements IPlanTowerObserver {

    private static final Logger logger = LoggerFactory.getLogger(LabelObserver.class);

    private JLabel deviceStatus, measurementTime, pm1_0, pm2_5, pm10;
    private JButton btnConnect;
    private static final String UNIT = " \u03BCg/m\u00B3";
    private static final String PRE_HTML = "<html><b>";
    private static final String POST_HTML = "</b></html>";

    public void setLabelsToUpdate(HashMap<String, JComponent> components) {
        deviceStatus = (JLabel)get(components, "deviceStatus");
        measurementTime = (JLabel)get(components, "measurementTime");
        pm1_0 = (JLabel)get(components, "pm1_0"); // TODO don't use hardcoded labels like that
        pm2_5 = (JLabel)get(components, "pm2_5");
        pm10 = (JLabel)get(components, "pm10");
        btnConnect = (JButton)get(components, "connect");
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        if (sample == null) {
            deviceStatus.setText("Status: sensor not ready");
        } else {
            
            deviceStatus.setText("Status: Measuring ...");
            measurementTime.setText("<html><small>" + Constants.DATE_FORMAT.format(sample.getDate()) + "</small></html>");
            pm1_0.setText(PRE_HTML + String.valueOf(sample.getPm1_0()) + UNIT + POST_HTML);
            
            pm2_5.setText(PRE_HTML + String.valueOf(sample.getPm2_5()) + UNIT + POST_HTML);
            AQIColor color2_5 = AQIColor.fromPM25Level(sample.getPm2_5());
            pm2_5.setForeground(color2_5.getColor());
            pm2_5.setToolTipText(color2_5.getDescription());
            AQIColor color10 = AQIColor.fromPM10Level(sample.getPm10());
            pm10.setText(PRE_HTML + String.valueOf(sample.getPm10()) + UNIT + POST_HTML);
            pm10.setForeground(color10.getColor());
            pm10.setToolTipText(color10.getDescription());
        }
    }
    
    @Override
    public void disconnected() {
        btnConnect.setText("Connect");
        deviceStatus.setText("Status: Device isconnected");
    }

    private JComponent get(HashMap<String, JComponent> components, String name) {
        JComponent component = components.get(name);
        if (component == null) {
            logger.error("Component of name: {} not found! Going down.", name);
            throw new IllegalArgumentException("Component of name: " + name + " not found! Going down.");
        }
        return component;
    }

}