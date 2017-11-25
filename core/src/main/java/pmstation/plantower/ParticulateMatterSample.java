package pmstation.plantower;

import java.util.Date;

public class ParticulateMatterSample {
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
