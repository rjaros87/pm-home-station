/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;

import pmstation.Station;

public class MacOSIntegration implements MRJAboutHandler, MRJPrefsHandler, MRJQuitHandler{
    
    private static final Logger logger = LoggerFactory.getLogger(MacOSIntegration.class);
    private final Station station;
    
    public MacOSIntegration(Station station) {
        this.station = station;
    }
    
    public void integrate() {
        try { 
            MRJApplicationUtils.registerAboutHandler(this);
            MRJApplicationUtils.registerPrefsHandler(this);
            MRJApplicationUtils.registerQuitHandler(this);
        } catch (Exception e) {
            logger.error("Ooops, there was a problem integrating with OSX menu", e);
        }
    }

    @Override
    public void handleQuit() {
        station.closeApp();
    }

    @Override
    public void handlePrefs() {
        station.openConfigDlg();
    }

    @Override
    public void handleAbout() {
        station.openAboutDlg();
    }

}
