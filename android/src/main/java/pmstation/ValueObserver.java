package pmstation;

import pmstation.plantower.ParticulateMatterSample;

public interface ValueObserver {
    void onNewValue(ParticulateMatterSample sample);
}
