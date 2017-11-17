package pl.radoslawjaros;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
//import pl.radoslawjaros.observers.ConsoleObserver;
import pl.radoslawjaros.observers.JframeChartObserver;
import pl.radoslawjaros.observers.JframeComponentsObserver;
import pl.radoslawjaros.plantower.PlanTowerSensor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class Station {

    public static void main(String[] args) {
        PlanTowerSensor planTowerSensor = new PlanTowerSensor();

        JFrame frame = new JFrame("Particulate matter station");
        frame.getContentPane().setPreferredSize(new Dimension(640, 480));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("Disconnect device");
                planTowerSensor.disconnectDevice();
                super.windowClosing(windowEvent);
            }

        });
        GridBagConstraints gbc = new GridBagConstraints();

        HashMap<String, JLabel> jFrameComponentsMap = new HashMap<>();
        XYChart chart = new XYChartBuilder().width(300).height(300).xAxisTitle("sample").yAxisTitle("\u03BCg/m\u00B3").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setMarkerSize(2);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 5;
        chartPanel.setPreferredSize(new Dimension(300, 300));
        frame.add(chartPanel, gbc);

        JframeChartObserver chartObserve = new JframeChartObserver();
        chartObserve.setChart(chart);
        chartObserve.setChartPanel(chartPanel);
        planTowerSensor.addObserver(chartObserve);

        JLabel deviceStatus = new JLabel("Status: ");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(20, 0, 0, 20);
        frame.add(deviceStatus, gbc);
        jFrameComponentsMap.put("deviceStatus", deviceStatus);

        JButton connectionBtn = new JButton("Connect");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 0, 0);
        connectionBtn.setFocusable(false);
        connectionBtn.setEnabled(false);
        connectionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                connectionBtn.setEnabled(false);
                switch (connectionBtn.getText()) {
                    case "Connect":
                        if (planTowerSensor.connectDevice()) {
                            connectionBtn.setText("Disconnect");
                            deviceStatus.setText("Status: Connected");
                            planTowerSensor.startMeasurements();
                        }
                        break;
                    case "Disconnect":
                        planTowerSensor.disconnectDevice();
                        connectionBtn.setText("Connect");
                        deviceStatus.setText("Status: Disconnected");
                        break;
                }
                connectionBtn.setEnabled(true);
            }
        });
        frame.add(connectionBtn, gbc);

        JLabel pmMeasurementTimeLabel = new JLabel("Measurement time: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(40, 0, 0, 0);
        frame.add(pmMeasurementTimeLabel, gbc);


        JLabel pmMeasurementTime = new JLabel();
        gbc.gridx = 1;
        frame.add(pmMeasurementTime, gbc);
        jFrameComponentsMap.put("measurmentTime", pmMeasurementTime);

        JLabel pm1_0Label = new JLabel("PM 1.0:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(40, 0, 0, 0);
        frame.add(pm1_0Label, gbc);

        JLabel pm1_0 = new JLabel();
        gbc.gridx = 1;
        frame.add(pm1_0, gbc);
        jFrameComponentsMap.put("pm1_0", pm1_0);

        JLabel pm2_5Label = new JLabel("PM 2.5:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(40, 0, 0, 0);
        frame.add(pm2_5Label, gbc);

        JLabel pm2_5 = new JLabel();
        gbc.gridx = 1;
        frame.add(pm2_5, gbc);
        jFrameComponentsMap.put("pm2_5", pm2_5);

        JLabel pm10Label = new JLabel("PM 10:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(40, 0, 0, 0);
        frame.add(pm10Label, gbc);

        JLabel pm10 = new JLabel();
        gbc.gridx = 1;
        frame.add(pm10, gbc);
        jFrameComponentsMap.put("pm10", pm10);

        JframeComponentsObserver jframeComponentsObserver = new JframeComponentsObserver();
        jframeComponentsObserver.setJframeComponents(jFrameComponentsMap);
        planTowerSensor.addObserver(jframeComponentsObserver);

        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (planTowerSensor.connectDevice()) {
            connectionBtn.setText("Disconnect");
            deviceStatus.setText("Status: Connected");
            planTowerSensor.startMeasurements();
            connectionBtn.setEnabled(true);
        }
        else {
            deviceStatus.setText("Status: Device not found");
        }
    }
}
