/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.plantower;

import java.io.Serializable;
import java.util.Date;

public class ParticulateMatterSample implements Serializable{

    private static final long serialVersionUID = 3387284515078504042L;

    private int pm1_0;
    private int pm2_5;
    private int pm10;
    private Date date;


    public ParticulateMatterSample(int pm1_0, int pm2_5, int pm10) {
        date = new Date();
        this.pm1_0 = pm1_0;
        this.pm2_5 = pm2_5;
        this.pm10 = pm10;
    }

    public Date getDate() {
        return date;
    }

    public int getPm1_0() {
        return pm1_0;
    }

    public int getPm2_5() {
        return pm2_5;
    }

    public int getPm10() {
        return pm10;
    }
}
