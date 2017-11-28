/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.observers;

import pmstation.core.plantower.ParticulateMatterSample;

public interface PlanTowerObserver {
    void notify(ParticulateMatterSample sample);
}
