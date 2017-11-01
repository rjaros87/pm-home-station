package pl.radoslawjaros.observers;

import pl.radoslawjaros.plantower.ParticulateMatterSample;

public interface PlanTowerObserver {
    public void notify(ParticulateMatterSample sample);
}
