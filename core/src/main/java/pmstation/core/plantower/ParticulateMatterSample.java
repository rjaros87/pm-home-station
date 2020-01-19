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

    private double hcho;
    private double humidity;
    private int pm1_0;
    private int pm2_5;
    private int pm10;
    private double temperature;
    private Date date;


    public ParticulateMatterSample(int pm1_0, int pm2_5, int pm10, int hcho, int humidity, int temperature) {
        date = new Date();
        this.hcho = hcho == -1 ? -1 : (double) hcho / 1000.0; // mg/m^3 http://archiwum.ciop.pl/13999
        this.humidity = humidity == -1 ? -1 : (double) humidity / 10.0; // %
        this.pm1_0 = pm1_0; // ug/m^3
        this.pm2_5 = pm2_5; // ug/m^3
        this.pm10 = pm10; // ug/m^3
        this.temperature = temperature == -1 ? -1 : (double) temperature / 10.0; // Celsius
    }

    public Date getDate() {
        return date;
    }

    public double getHcho() {
        return hcho;
    }

    public double getHumidity() {
        return humidity;
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

    public double getTemperature() {
        return temperature;
    }
}
