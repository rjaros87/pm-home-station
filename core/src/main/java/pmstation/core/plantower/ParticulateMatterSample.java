/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.plantower;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ParticulateMatterSample implements Serializable {

    private enum ParticulateMatterSampleEnum {
        PM1_0("pm1_0", Unit.PARTICULATE_MATTER, ParticulateMatterSample::getPm1_0),
        PM2_5("pm2_5", Unit.PARTICULATE_MATTER, ParticulateMatterSample::getPm2_5),
        PM10("pm10", Unit.PARTICULATE_MATTER, ParticulateMatterSample::getPm10),
        HCHO("hcho", Unit.HCHO_UG, ParticulateMatterSample::getHcho),
        TEMPERATURE("temperature", Unit.TEMPERATURE, ParticulateMatterSample::getTemperature),
        HUMIDITY("humidity", Unit.HUMIDITY, ParticulateMatterSample::getHumidity),
        ;

        private final String name;
        private final Unit unit;
        private final Function<ParticulateMatterSample, Number> valueAccessor;

        ParticulateMatterSampleEnum(String name, Unit unit,
                                    Function<ParticulateMatterSample, Number> valueAccessor) {
            this.name = name;
            this.unit = unit;
            this.valueAccessor = valueAccessor;
        }
    }

    private static final long serialVersionUID = 3387284515078504042L;

    private final int hcho;
    private final double humidity;
    private final int pm1_0;
    private final int pm2_5;
    private final int pm10;
    private final double temperature;
    private final Date date;
    private final byte modelVersion;
    private final byte errCode;
    
    
    public ParticulateMatterSample(int pm1_0, int pm2_5, int pm10) {
        this(pm1_0, pm2_5, pm10, -1, -1, Integer.MIN_VALUE, (byte)0, (byte)0);
    }

    public ParticulateMatterSample(int pm1_0, int pm2_5, int pm10,
            int hcho, int humidity, int temperature,
            byte modelVersion, byte errCode) {
        date = new Date();
        this.hcho = hcho; // ug/m^3
                // see levels in mg/m^3:
                // http://archiwum.ciop.pl/13999 (quite old, 1995),
                // or here in ug/m^3:
                // https://www.canada.ca/content/dam/canada/health-canada/migration/healthy-canadians/publications/healthy-living-vie-saine/formaldehyde/alt/formaldehyde-eng.pdf (2006)
        this.humidity = humidity >= 0 ? (double) humidity / 10.0 : Double.NaN; // %
        this.pm1_0 = pm1_0; // ug/m^3
        this.pm2_5 = pm2_5; // ug/m^3
        this.pm10 = pm10; // ug/m^3
        this.temperature = temperature != Integer.MIN_VALUE ? (double) temperature / 10.0 : Double.NaN; // Celsius
        this.modelVersion = modelVersion;
        this.errCode = errCode;
    }

    public Date getDate() {
        return date;
    }

    /**
     * Returns error code stated by a device
     * @return a value, zero means no error or error reporting not supported by a device
     */
    public byte getErrCode() {
        return errCode;
    }
    
    /**
     * Returns HCHO aka Formaldehyde concentration in ug/m^3
     * @return a value, negative means no reading
     */
    public int getHcho() {
        return hcho;
    }

    /**
     * Returns PM model version 
     * @return a value, 0 if not stated
     */
    public byte getModelVersion() {
        return modelVersion;
    }

    /**
     * Returns RH level (%)
     * @return a value, negative means no reading
     */
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

    /**
     * Returns temperature readings ('C)
     * @return a value, Double.NaN means no reading
     */
    public double getTemperature() {
        return temperature;
    }

    public Map<String, Map<String, Object>> getAsMap() {
        Map<String, Map<String, Object>> pmMap = new HashMap<>();

        for (ParticulateMatterSampleEnum sample: ParticulateMatterSampleEnum.values()) {
            Number value = sample.valueAccessor.apply(this);
            if ((value instanceof Double && !((Double) value).isNaN())
                    || (value instanceof Integer && value.intValue() != -1)) {
                pmMap.put(sample.name, Map.of("value", value, "unit", sample.unit.toString()));
            }
        }

        return pmMap;
    }
}
