/*
 * pm-home-station
 * 2017-2018 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;

public class UIScreenShotObserver implements IPlanTowerObserver {

    private static final Logger logger = LoggerFactory.getLogger(UIScreenShotObserver.class);

    private final JFrame jFrame;
    private final String screenShotPath;

    public UIScreenShotObserver(JFrame jFrame, String screenShotPath) {
        this.jFrame = jFrame;
        this.screenShotPath = screenShotPath;
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        screenshot(jFrame);
    }

    @Override
    public void connecting() {
        screenshot(jFrame);
    }

    @Override
    public void connected() {
        screenshot(jFrame);
    }

    @Override
    public void disconnected() {
        screenshot(jFrame);
    }

    @Override
    public void disconnecting() {
        screenshot(jFrame);
    }

    public void screenshot(JFrame jFrame) {
        logger.info("Going to take a screenshot...");
        Component component = jFrame.getContentPane();
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        component.paint(image.getGraphics());
        try {
            File destFile = new File(screenShotPath);
            File tmpFile = File.createTempFile("pm-station-screenshot", "png", destFile.getParentFile());
            
            ImageIO.write(image, "png", tmpFile);
            destFile.delete();
            if (!tmpFile.renameTo(destFile)) {
                logger.error("There was a problem renaming temp screenshot file to target name"); 
            }
            logger.info("Screenshot written to: {}", screenShotPath);
        } catch (Exception e) {
            logger.error("Error writing screenshot to {}", screenShotPath, e);
        }
    }
}
