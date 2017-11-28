package pmstation.observers;

import pmstation.core.plantower.ParticulateMatterSample;

import javax.swing.*;
import java.time.Instant;

public class ConsoleObserver implements PlanTowerObserver {
    private JTextArea jTextArea;

    public void setTextArea(JTextArea textArea) {
        this.jTextArea = textArea;
    }

    @Override
    public void notify(ParticulateMatterSample sample) {
        if (sample == null) {
            jTextArea.setText(Instant.now().toString() + "PlanTower not ready");
        } else {
            jTextArea.setText(
                    JframeComponentsObserver.dateFormat.format(sample.getDate())
                            + " >>> PM1.0: " + sample.getPm1_0()
                            + ", PM2.5: " + sample.getPm2_5()
                            + ", PM10: " + sample.getPm10()
            );
        }
    }
}
