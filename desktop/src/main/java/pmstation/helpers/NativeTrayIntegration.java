/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pmstation.Station;
import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.observers.AQIColor;

public class NativeTrayIntegration {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeTrayIntegration.class);
    private static final String APP_ICON_FORMAT = "app-icon-%s.png";
    private final Station station;
    
    private final Map<AQIColor, Image> scaryIcons = new HashMap<>();
    private Image defaultIcon = null;
    private Image disconnectedIcon = null;
    
    public NativeTrayIntegration(Station station) {
        this.station = station;
    }
    
    public void integrate() {
        if (!SystemTray.isSupported()) {
            logger.info("System tray is not supported on this OS");
            return;
        }
        loadIcons();
        if (defaultIcon == null) {
            logger.warn("No default icon found in resources - no integration with system tray");
            return;
        }
        PopupMenu menu = new PopupMenu();

        MenuItem infoMenuItem = new MenuItem("No measurements");
        infoMenuItem.setEnabled(false);
        menu.add(infoMenuItem);
        
        menu.addSeparator();
        
        MenuItem itemMainWindow = new MenuItem("Main window");
        itemMainWindow.addActionListener((e) -> station.setVisible(true));
        menu.add(itemMainWindow);
        
        MenuItem itemPrefs = new MenuItem("Preferences");
        itemPrefs.addActionListener((e) -> station.openConfigDlg());
        menu.add(itemPrefs);
        
        menu.addSeparator();
        
        MenuItem itemQuit = new MenuItem("Quit");
        itemQuit.addActionListener((e) -> station.closeApp());
        menu.add(itemQuit);

        try {
            SystemTray tray = SystemTray.getSystemTray();
            
            TrayIcon menuBarIcon = new TrayIcon(defaultIcon, "pm-home-station", menu);
            
            station.addObserver(new IPlanTowerObserver() {
                @Override
                public void update(ParticulateMatterSample sample) {
                    infoMenuItem.setLabel("PM1.0: " + sample.getPm1_0() + ", " +
                            "PM2.5: " + sample.getPm2_5() + ", " +
                            "PM10 : " + sample.getPm10() + " (" + Constants.UNITS + ")");
                    menuBarIcon.setToolTip("PM1.0 : " + sample.getPm1_0() + Constants.UNITS + " \n" + // space before \n required for Linux/Gnome
                            "PM2.5 : " + sample.getPm2_5() + Constants.UNITS + " \n" +
                            "PM10  : " + sample.getPm10() + Constants.UNITS);
                    
                    setScaryIcon(menuBarIcon, AQIColor.fromPM25Level(sample.getPm2_5()), AQIColor.fromPM10Level(sample.getPm10()));
                }
                
                @Override
                public void disconnected() {
                    menuBarIcon.setImage(disconnectedIcon != null ? disconnectedIcon : defaultIcon);
                    menuBarIcon.displayMessage("Device disconnected", "Sensor has just been disconnected", MessageType.INFO);
                }
                
            });
            tray.add(menuBarIcon);
            menuBarIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() >= 2){
                        station.setVisible(true);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error adding menubar app to OSX menubar", e);
        }
    }
    
    private void setScaryIcon(TrayIcon menuBarIcon, AQIColor pm2, AQIColor pm10) {
        AQIColor scarierIcon = pm2.worseThan(pm10) ? pm2 : pm10;
        Image icon = scaryIcons.get(scarierIcon);
        menuBarIcon.setImage(icon != null ? icon : defaultIcon);
    }
    
    private Image getIcon(String name) throws IOException {
        BufferedImage trayIconImage = ResourceHelper.getAppIcon(name);
        int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
        return trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH); // scaling is better for Win7
    }

    private void loadIcons() {
        for (AQIColor level : AQIColor.values()) {
            try {
                scaryIcons.put(level, getIcon(String.format(APP_ICON_FORMAT, level.name().toLowerCase())));
            } catch (Exception e) {
                logger.error("Error loading scary toolbar icon for level: {}", level, e);
            }
        }
        try {
            disconnectedIcon = getIcon(String.format(APP_ICON_FORMAT, "disconnected"));
            defaultIcon = disconnectedIcon;
        } catch (IOException e) {
            logger.error("Error loading scary toolbar icon for disconnected", e);
        }
    }
    
}
