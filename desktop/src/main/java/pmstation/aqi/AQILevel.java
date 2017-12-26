/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.aqi;

public interface AQILevel {
    
    public String name();
    
    public AQIRiskLevel getRiskLevel();
    
    public boolean worseThan(AQILevel other);
}
