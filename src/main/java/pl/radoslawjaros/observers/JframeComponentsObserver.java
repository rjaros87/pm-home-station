package pl.radoslawjaros.observers;

import pl.radoslawjaros.plantower.ParticulateMatterSample;

import javax.swing.*;
import java.util.HashMap;

public class JframeComponentsObserver implements PlanTowerObserver {
    private JLabel deviceStatus, measurmentTime, pm1_0, pm2_5, pm10;

    public void setJframeComponents(HashMap<String, JLabel> components) {
        deviceStatus = components.get("deviceStatus");
        measurmentTime = components.get("measurmentTime");
        pm1_0 = components.get("pm1_0");
        pm2_5 = components.get("pm2_5");
        pm10 = components.get("pm10");
    }

    @Override
    public void notify(ParticulateMatterSample sample) {
        if (sample == null) {
            deviceStatus.setText("Status: PlanTower not ready");
        } else {
            String unit = " \u03BCg/m\u00B3";
            deviceStatus.setText("Status: Measuring ...");
            measurmentTime.setText(sample.getDateTime());
            pm1_0.setText(String.valueOf(sample.getPm1_0()) + unit);
            pm2_5.setText(String.valueOf(sample.getPm2_5()) + unit);
            pm10.setText(String.valueOf(sample.getPm10()) + unit);
        }
    }
}
