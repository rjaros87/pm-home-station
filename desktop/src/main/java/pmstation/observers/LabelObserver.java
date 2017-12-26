/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.aqi.AQI10Level;
import pmstation.aqi.AQI25Level;
import pmstation.aqi.AQIColor;
import pmstation.aqi.AQILevel;
import pmstation.aqi.AQIRiskLevel;
import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.helpers.ResourceHelper;

public class LabelObserver implements IPlanTowerObserver {

    private static final Logger logger = LoggerFactory.getLogger(LabelObserver.class);

    private JLabel deviceStatus, measurementTime, pm1_0, pm2_5, pm10;
    private JButton btnConnect, icon;
    private static final String UNIT = " \u03BCg/m\u00B3";
    private static final String PRE_HTML = "<html><b>";
    private static final String POST_HTML = "</b></html>";
    private static final String APP_ICON_FORMAT = "app-icon-%s.png";
    
    private final Map<AQIRiskLevel, Image> scaryIcons = new HashMap<>();
    private Image disconnectedIcon = null;
    private Image defaultIcon = null;

    public LabelObserver() {
        loadIcons();
    }
    
    public void setLabelsToUpdate(HashMap<String, JComponent> components) {
        deviceStatus = (JLabel)get(components, "deviceStatus");
        measurementTime = (JLabel)get(components, "measurementTime");
        pm1_0 = (JLabel)get(components, "pm1_0"); // TODO don't use hardcoded labels like that
        pm2_5 = (JLabel)get(components, "pm2_5");
        pm10 = (JLabel)get(components, "pm10");
        btnConnect = (JButton)get(components, "connect");
        icon = (JButton)get(components, "icon");
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        
        if (sample == null || sample.getPm1_0() <= 0 && sample.getPm2_5() <= 0 && sample.getPm10() <= 0) {
            deviceStatus.setText("Status: sensor not ready");
            if (icon != null) {
                icon.setIcon(new ImageIcon(disconnectedIcon.getScaledInstance(icon.getIcon().getIconWidth(), -1, Image.SCALE_SMOOTH)));
            }
        } else {
            measurementTime.setText("<html><small>" + Constants.DATE_FORMAT.format(sample.getDate()) + "</small></html>");
            pm1_0.setText(PRE_HTML + String.valueOf(sample.getPm1_0()) + UNIT + POST_HTML);
            
            pm2_5.setText(PRE_HTML + String.valueOf(sample.getPm2_5()) + UNIT + POST_HTML);
            AQI25Level color2_5 = AQI25Level.fromValue(sample.getPm2_5());
            pm2_5.setForeground(AQIColor.fromLevel(color2_5).getColor());
            pm2_5.setToolTipText(color2_5.getDescription());
            AQI10Level color10 = AQI10Level.fromValue(sample.getPm10());
            pm10.setText(PRE_HTML + String.valueOf(sample.getPm10()) + UNIT + POST_HTML);
            pm10.setForeground(AQIColor.fromLevel(color10).getColor());
            pm10.setToolTipText(color10.getDescription());
            
            if (icon != null) {
                setScaryIcon(icon, color2_5, color10);
            }
        }
    }
    
    @Override
    public void connecting() {
        deviceStatus.setText("Status: Disconnecting from the sensor...");
    }
    
    @Override
    public void connected() {
        deviceStatus.setText("Status: Measuring ...");
    }
    
    @Override
    public void disconnected() {
        btnConnect.setText("Connect");
        deviceStatus.setText("Status: Device sensor disconnected");
        if (icon != null) {
            icon.setIcon(new ImageIcon(disconnectedIcon.getScaledInstance(icon.getIcon().getIconWidth(), -1, Image.SCALE_SMOOTH)));
        }
    }
    
    @Override
    public void disconnecting() {
        deviceStatus.setText("Status: Disconnecting from the sensor...");
    }

    private JComponent get(HashMap<String, JComponent> components, String name) {
        JComponent component = components.get(name);
        if (component == null) {
            logger.error("Component of name: {} not found! Going down.", name);
            throw new IllegalArgumentException("Component of name: " + name + " not found! Going down.");
        }
        return component;
    }
    
    private void setScaryIcon(JButton button, AQILevel pm2, AQILevel pm10) {
        AQILevel scarierIcon = pm2.worseThan(pm10) ? pm2 : pm10;
        Image icon = scaryIcons.get(scarierIcon.getRiskLevel());
        icon = icon != null ? icon : defaultIcon;
        button.setIcon(new ImageIcon(icon.getScaledInstance(button.getIcon().getIconWidth(), -1, Image.SCALE_SMOOTH)));
    }
    
    private void loadIcons() {
        for (AQIRiskLevel level : AQIRiskLevel.values()) {
            try {
                scaryIcons.put(level, ResourceHelper.getAppIcon(String.format(APP_ICON_FORMAT, level.name().toLowerCase())));
            } catch (Exception e) {
                logger.error("Error loading scary toolbar icon for level: {}", level, e);
            }
        }
        try {
            disconnectedIcon = ResourceHelper.getAppIcon(String.format(APP_ICON_FORMAT, "disconnected"));
            defaultIcon = disconnectedIcon;
        } catch (IOException e) {
            logger.error("Error loading scary toolbar icon for disconnected", e);
        }
    }

}
