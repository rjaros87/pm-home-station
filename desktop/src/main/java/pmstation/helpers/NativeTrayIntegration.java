/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.helpers;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.Station;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;

public class NativeTrayIntegration {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeTrayIntegration.class);
    private static final String UNIT = " \u03BCg/m\u00B3";
    private final Station station;
    
    public NativeTrayIntegration(Station station) {
        this.station = station;
    }
    
    public void integrate() {
        if (!SystemTray.isSupported()) {
            logger.info("System tray is not supported on this OS");
            return;
        }
        PopupMenu menu = new PopupMenu();

        MenuItem infoMenuItem = new MenuItem("No measurements");
        infoMenuItem.setEnabled(false);
        menu.add(infoMenuItem);
        
        menu.addSeparator();
        
        MenuItem itemMainWindow = new MenuItem("Main window");
        itemMainWindow.addActionListener((e) -> { station.setVisible(true); } );
        menu.add(itemMainWindow);
        
        MenuItem itemPrefs = new MenuItem("Preferences");
        itemPrefs.addActionListener((e) -> { station.openConfigDlg(); });
        menu.add(itemPrefs);
        
        menu.addSeparator();
        
        MenuItem itemQuit = new MenuItem("Quit");
        itemQuit.addActionListener((e) -> { station.closeApp(); } );
        menu.add(itemQuit);

        try {
            SystemTray tray = SystemTray.getSystemTray();
            
            // A simpler version but the icon on Windows7 is more distorted
            //TrayIcon menuBarIcon = new TrayIcon(new ImageIcon(Station.class.getResource("/pmstation/app-icon.png")).getImage(), "pm-station-usb", menu);
            //menuBarIcon.setImageAutoSize(true);
            
            BufferedImage trayIconImage = ImageIO.read(Station.class.getResource("/pmstation/app-icon.png"));
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            TrayIcon menuBarIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), "pm-station-usb", menu);
            
            station.addObserver(new IPlanTowerObserver() {
                @Override
                public void update(ParticulateMatterSample sample) {
                    infoMenuItem.setLabel("PM1.0: " + sample.getPm1_0() + ", " +
                            "PM2.5: " + sample.getPm2_5() + ", " +
                            "PM10 : " + sample.getPm10() + " (" + UNIT + ")");
                    menuBarIcon.setToolTip("PM1.0 : " + sample.getPm1_0() + UNIT + "\n" +
                            "PM2.5 : " + sample.getPm2_5() + UNIT + "\n" +
                            "PM10  : " + sample.getPm10() + UNIT);
                }
                
            });
            tray.add(menuBarIcon);
        } catch (Exception e) {
            logger.error("Error adding menubar app to OSX menubar", e);
        }
    }

}
