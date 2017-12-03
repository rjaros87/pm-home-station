/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.core.plantower;

public interface IPlanTowerObserver {
    void onNewValue(ParticulateMatterSample sample);
}
