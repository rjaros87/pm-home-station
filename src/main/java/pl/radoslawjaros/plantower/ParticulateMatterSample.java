package pl.radoslawjaros.plantower;

import java.time.Instant;

public class ParticulateMatterSample {
    private int pm1_0;
    private int pm2_5;
    private int pm10;
    private String dateTime;

    public ParticulateMatterSample(int pm1_0, int pm2_5, int pm10) {
        dateTime = Instant.now().toString();
        this.pm1_0 = pm1_0;
        this.pm2_5 = pm2_5;
        this.pm10 = pm10;
    }

    public String getDateTime() {
        return dateTime;
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
