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
import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.helpers.ResourceHelper;

public class LabelObserver implements IPlanTowerObserver {

    private static final Logger logger = LoggerFactory.getLogger(LabelObserver.class);
    
    public static class LabelsCollector {
        
        public static enum LABEL {
            DEVICE_STATUS, MEASURMENT_TIME,
            PM1, PM25, PM10,
            HCHO, HUMIDITY, TEMP,
            HCHO_LABEL, HUMIDITY_LABEL, TEMP_LABEL,
            CONNECT, ICON; 
        }
        
        private Map<LABEL, JComponent> labelsToBeUpdated = new HashMap<>();
        
        public void add(LABEL label, JComponent component) {
            labelsToBeUpdated.put(label, component);
        }
        
        JComponent get(LABEL label) {
            JComponent component = labelsToBeUpdated.get(label);
            if (component == null) {
                logger.error("Component of name: {} not found! Going down.", label);
                throw new IllegalArgumentException("Component of name: " + label + " not found! Going down.");
            }
            return component;
        }
    }

    private JLabel deviceStatus, measurementTime;
    private JLabel pm1_0, pm2_5, pm10;
    private JLabel hcho, humi, temp;
    private JLabel hchoLabel, humiLabel, tempLabel;
    
    private JButton btnConnect, icon;
    private static final String PM_UNIT = " <small>" + Constants.PM_UNITS + "</small>";
    private static final String HCHO_UNIT = " <small>" + Constants.HHCO_UNITS + "</small>";
    private static final String HUMI_UNIT = " <small>" + Constants.HUMI_UNITS + "</small>";
    private static final String TEMP_UNIT = " <small>" + Constants.TEMP_UNITS + "</small>";
    
    
    private static final String PRE_HTML = "<html><b>";
    private static final String POST_HTML = "</b></html>";
    private static final String APP_ICON_FORMAT = "app-icon-%s.png";
    
    private final Map<AQIRiskLevel, Image> scaryIcons = new HashMap<>();
    private Image disconnectedIcon = null;
    private Image defaultIcon = null;

    public LabelObserver(LabelsCollector collected) {
        loadIcons();

        deviceStatus = (JLabel)collected.get(LabelsCollector.LABEL.DEVICE_STATUS);
        measurementTime = (JLabel)collected.get(LabelsCollector.LABEL.MEASURMENT_TIME);
        pm1_0 = (JLabel)collected.get(LabelsCollector.LABEL.PM1);
        pm2_5 = (JLabel)collected.get(LabelsCollector.LABEL.PM25);
        pm10 = (JLabel)collected.get(LabelsCollector.LABEL.PM10);
        
        hcho = (JLabel)collected.get(LabelsCollector.LABEL.HCHO);
        humi = (JLabel)collected.get(LabelsCollector.LABEL.HUMIDITY);
        temp = (JLabel)collected.get(LabelsCollector.LABEL.TEMP);
        
        hchoLabel = (JLabel)collected.get(LabelsCollector.LABEL.HCHO_LABEL);
        humiLabel = (JLabel)collected.get(LabelsCollector.LABEL.HUMIDITY_LABEL);
        tempLabel = (JLabel)collected.get(LabelsCollector.LABEL.TEMP_LABEL);
        
        btnConnect = (JButton)collected.get(LabelsCollector.LABEL.CONNECT);
        icon = (JButton)collected.get(LabelsCollector.LABEL.ICON);
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        
        if (sample == null || sample.getPm1_0() <= 0 && sample.getPm2_5() <= 0 && sample.getPm10() <= 0) {
            deviceStatus.setText("Status: sensor not ready");
            if (icon != null) {
                icon.setIcon(new ImageIcon(disconnectedIcon.getScaledInstance(icon.getIcon().getIconWidth(), -1, Image.SCALE_SMOOTH)));
            }
        } else {
            deviceStatus.setText("Status: Measuring ...");

            int pm25maxSafe = Config.instance().to().getInt(Config.Entry.PM25_MAX_SAFE_LIMIT.key(), Constants.DEFAULT_PM25_MAX_SAFE);
            int pm10maxSafe = Config.instance().to().getInt(Config.Entry.PM10_MAX_SAFE_LIMIT.key(), Constants.DEFAULT_PM10_MAX_SAFE);
            int pm25percent = Math.round(sample.getPm2_5() * 1f / pm25maxSafe * 100);
            int pm10percent = Math.round(sample.getPm10() * 1f / pm10maxSafe * 100);

            measurementTime.setText("<html><small>" + Constants.DATE_FORMAT.format(sample.getDate()) + "</small></html>");
            pm1_0.setText(PRE_HTML + String.valueOf(sample.getPm1_0()) + PM_UNIT  + POST_HTML);
            
            pm2_5.setText(PRE_HTML + String.valueOf(sample.getPm2_5()) + PM_UNIT + "<br/>(" + pm25percent + "%)" + POST_HTML);
            AQI25Level color2_5 = AQI25Level.fromValue(sample.getPm2_5());
            pm2_5.setForeground(AQIColor.fromLevel(color2_5).getColor());            
            pm2_5.setToolTipText(color2_5.getDescription());
            
            AQI10Level color10 = AQI10Level.fromValue(sample.getPm10());
            pm10.setText(PRE_HTML + String.valueOf(sample.getPm10()) + PM_UNIT + "<br/>(" + pm10percent + "%)" + POST_HTML);
            pm10.setForeground(AQIColor.fromLevel(color10).getColor());
            pm10.setToolTipText(color10.getDescription());
            
            if (sample.getHcho() >= 0 && sample.getHumidity() >= 0 && sample.getTemperature() >= 0) {
                hcho.setText(PRE_HTML + "<<small>" + formatDouble("%.3f", sample.getHcho()) + "</small> "+ HCHO_UNIT + POST_HTML);
                humi.setText(PRE_HTML + formatDouble("%.1f", sample.getHumidity()) + HUMI_UNIT + POST_HTML);
                temp.setText(PRE_HTML + formatDouble("%.1f", sample.getTemperature()) + TEMP_UNIT + POST_HTML);
                additionalPanelVisible(true);
            } else {
                additionalPanelVisible(false);
            }
            
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

    private void additionalPanelVisible(boolean visible) {
        hcho.setVisible(visible);
        hchoLabel.setVisible(visible);
        humi.setVisible(visible);
        humiLabel.setVisible(visible);
        temp.setVisible(visible);
        tempLabel.setVisible(visible);
    }
    
    private String formatDouble(String doubleFormat, double value) {
        return String.format(doubleFormat, value);
    }
}
