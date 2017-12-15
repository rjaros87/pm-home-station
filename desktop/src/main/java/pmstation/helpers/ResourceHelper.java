/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.helpers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import pmstation.Station;

public class ResourceHelper {

    public static Icon getIcon(String name) {
        return new ImageIcon(Station.class.getResource("/pmstation/" + name));
    }
    
    public static BufferedImage getAppIcon(String name) throws IOException {
        BufferedImage iconImage = ImageIO.read(Station.class.getResource("/pmstation/" + name));
        LocalDate localDate = LocalDate.now();
        if (localDate.getMonth().equals(Month.DECEMBER) && localDate.getDayOfMonth() > 21 && localDate.getDayOfMonth() < 28) {
            BufferedImage hat = ImageIO.read(Station.class.getResource("/pmstation/app-icon-santa-hat.png"));
            BufferedImage combinedImage = new BufferedImage(iconImage.getWidth(), iconImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = combinedImage.createGraphics();
            g.drawImage(iconImage, 0, 0, null);
            g.drawImage(hat, 0, 0, null);
            g.dispose();
            iconImage = combinedImage;
        }
        
        return iconImage;
        
    }
}
