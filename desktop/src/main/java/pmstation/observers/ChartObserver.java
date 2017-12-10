/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.observers;

import java.util.Arrays;

import javax.swing.JPanel;

import org.knowm.xchart.XYChart;

import com.google.common.collect.EvictingQueue;

import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;


public class ChartObserver implements IPlanTowerObserver {
    private final XYChart chart;
    private final JPanel chartPanel;
    private EvictingQueue<Integer> pm1_0 = EvictingQueue.create(Constants.CHART_MAX_SAMPLES);
    private EvictingQueue<Integer> pm2_5 = EvictingQueue.create(Constants.CHART_MAX_SAMPLES);
    private EvictingQueue<Integer> pm10 = EvictingQueue.create(Constants.CHART_MAX_SAMPLES);

    
    public ChartObserver(XYChart chart, JPanel chartPanel) {
        this.chart = chart;
        this.chartPanel = chartPanel;
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        if (sample != null) {
            pm1_0.add(sample.getPm1_0());
            pm2_5.add(sample.getPm2_5());
            pm10.add(sample.getPm10());
            
            if (chart.getSeriesMap().isEmpty()) {
                chart.addSeries("PM 1.0", null, Arrays.asList(pm1_0.toArray(new Integer[0])));
                chart.addSeries("PM 2.5", null, Arrays.asList(pm2_5.toArray(new Integer[0])));
                chart.addSeries("PM 10", null, Arrays.asList(pm10.toArray(new Integer[0])));
            } else {
                chart.updateXYSeries("PM 1.0", null, Arrays.asList(pm1_0.toArray(new Integer[0])), null);
                chart.updateXYSeries("PM 2.5", null, Arrays.asList(pm2_5.toArray(new Integer[0])), null);
                chart.updateXYSeries("PM 10", null, Arrays.asList(pm10.toArray(new Integer[0])), null);
            }
            
            chartPanel.revalidate();
            chartPanel.repaint();
        }
    }
}
