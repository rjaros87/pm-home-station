package sanchin.pmstation;

import pl.radoslawjaros.plantower.ParticulateMatterSample;

public interface ValueObserver {
    void onNewValue(ParticulateMatterSample sample);
}
