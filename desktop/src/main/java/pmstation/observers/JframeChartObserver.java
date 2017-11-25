package pmstation.observers;

import org.knowm.xchart.XYChart;
import pmstation.plantower.ParticulateMatterSample;

import javax.swing.*;
import java.util.*;

public class JframeChartObserver implements PlanTowerObserver {
    private XYChart chart;
    private JPanel chartPanel;
    private List<Integer> pm1_0 = new ArrayList<Integer>();
    private List<Integer> pm2_5 = new ArrayList<Integer>();
    private List<Integer> pm10 = new ArrayList<Integer>();

    public void setChart(XYChart chart) {
        this.chart = chart;
    }

    public void setChartPanel(JPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    @Override
    public void notify(ParticulateMatterSample sample) {
        if (sample != null) {
            pm1_0.add(sample.getPm1_0());
            pm2_5.add(sample.getPm2_5());
            pm10.add(sample.getPm10());

            int max = pm1_0.size();
            int limit = max - 10;
            int min = limit > 0 ? limit : 0;

            if (chart.getSeriesMap().isEmpty()) {
                chart.addSeries("PM 1.0", null, pm1_0.subList(min, max));
                chart.addSeries("PM 2.5", null, pm2_5.subList(min, max));
                chart.addSeries("PM 10", null, pm10.subList(min, max));
            } else {
                chart.updateXYSeries("PM 1.0", null, pm1_0.subList(min, max), null);
                chart.updateXYSeries("PM 2.5", null, pm2_5.subList(min, max), null);
                chart.updateXYSeries("PM 10", null, pm10.subList(min, max), null);
            }
//            pm1_0.
            chartPanel.revalidate();
            chartPanel.repaint();
        }
    }
}
