/*
 * pm-home-station
 * 2020 (C) Copyright - https://github.com/rjaros87/pm-home-station
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


public class HHTChartObserver implements IPlanTowerObserver {
    private final XYChart chart;
    private final JPanel chartPanel;
    private EvictingQueue<Integer> hcho = EvictingQueue.create(Constants.CHART_MAX_SAMPLES);
    private EvictingQueue<Double> humi = EvictingQueue.create(Constants.CHART_MAX_SAMPLES);
    private EvictingQueue<Double> temp = EvictingQueue.create(Constants.CHART_MAX_SAMPLES);

    
    public HHTChartObserver(XYChart chart, JPanel chartPanel) {
        this.chart = chart;
        this.chartPanel = chartPanel;
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        if (sample != null) {            
            hcho.add(sample.getHcho() >= 0 ? sample.getHcho() : 0);
            humi.add(sample.getHumidity() >= 0 ? sample.getHumidity() : 0);
            temp.add(sample.getTemperature() != Double.NaN ? sample.getTemperature() : 0);
            if (chart.getSeriesMap().isEmpty()) {
                chart.addSeries("CH\u2082O", null, Arrays.asList(hcho.toArray(new Integer[0])));
                chart.addSeries("RH", null, Arrays.asList(humi.toArray(new Double[0])));
                chart.addSeries("Temp", null, Arrays.asList(temp.toArray(new Double[0])));
            } else {
                chart.updateXYSeries("CH\u2082O", null, Arrays.asList(hcho.toArray(new Integer[0])), null);
                chart.updateXYSeries("RH", null, Arrays.asList(humi.toArray(new Double[0])), null);
                chart.updateXYSeries("Temp", null, Arrays.asList(temp.toArray(new Double[0])), null);
            }
            if (sample.getHcho() >= 0 && sample.getHumidity() >= 0 && sample.getTemperature() != Double.NaN) {
                chartPanel.setVisible(true);
                chartPanel.revalidate();
                chartPanel.repaint();                
            } else {
                chartPanel.setVisible(false);
            }
        }
    }
}
