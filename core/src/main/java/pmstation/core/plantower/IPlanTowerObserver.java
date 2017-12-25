/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.plantower;

public interface IPlanTowerObserver {
    void update(ParticulateMatterSample sample);

    default void connecting() {};
    
    default void connected() {};

    default void disconnecting() { };

    default void disconnected() { };

}
