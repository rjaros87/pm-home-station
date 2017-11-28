package pmstation.observers;

import pmstation.core.plantower.ParticulateMatterSample;

public interface PlanTowerObserver {
    public void notify(ParticulateMatterSample sample);
}
