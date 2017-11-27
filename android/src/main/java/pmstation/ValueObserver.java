package pmstation;

import pmstation.core.plantower.ParticulateMatterSample;

public interface ValueObserver {
    void onNewValue(ParticulateMatterSample sample);
}
