/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.helpers;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;

import pmstation.Station;

public class MacOSIntegration implements MRJAboutHandler, MRJPrefsHandler, MRJQuitHandler{
    
    private static final Logger logger = LoggerFactory.getLogger(MacOSIntegration.class);
    private final JFrame frame;
    private final Station station;
    
    public MacOSIntegration(JFrame frame, Station station) {
        this.frame = frame;
        this.station = station;
    }
    
    public void integrate() {
        try { 
            MRJApplicationUtils.registerAboutHandler(this);
            MRJApplicationUtils.registerPrefsHandler(this);
            MRJApplicationUtils.registerQuitHandler(this);
        } catch (Exception e) {
            logger.error("Ooops, there was problem integrating with OSX", e);
        }
        
    }

    @Override
    public void handleQuit() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void handlePrefs() {
        station.openConfigDlg(frame);
    }

    @Override
    public void handleAbout() {
        station.openAboutDlg(frame);
    }

}
