package pmstation;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

//import pmstation.observers.ConsoleObserver;
import pmstation.observers.JframeChartObserver;
import pmstation.observers.JframeComponentsObserver;
import pmstation.plantower.PlanTowerSensor;

public class Station {
    
    private static final String PROJECT_URL = "https://github.com/rjaros87/pm-station-usb";

    public static void main(String[] args) {
        PlanTowerSensor planTowerSensor = new PlanTowerSensor();

        JFrame frame = new JFrame("Particulate matter station");
        frame.getContentPane().setMinimumSize(new Dimension(100, 100));
        frame.getContentPane().setPreferredSize(new Dimension(740, 480));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("Disconnect device...");
                planTowerSensor.disconnectDevice();
                super.windowClosing(windowEvent);
            }

        });

        HashMap<String, JLabel> jFrameComponentsMap = new HashMap<>();
        XYChart chart = new XYChartBuilder().xAxisTitle("sample").yAxisTitle("\u03BCg/m\u00B3").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setMarkerSize(2);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);
        chartPanel.setMinimumSize(new Dimension(50, 50));
        JframeChartObserver chartObserve = new JframeChartObserver();
        chartObserve.setChart(chart);
        chartObserve.setChartPanel(chartPanel);
        planTowerSensor.addObserver(chartObserve);

        JLabel deviceStatus = new JLabel("Status: ");
        jFrameComponentsMap.put("deviceStatus", deviceStatus);

        JButton connectionBtn = new JButton("Connect");
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

        JLabel pm1_0Label = new JLabel("PM 1.0:");

        JLabel pm1_0 = new JLabel();
        pm1_0.setText("----");
        jFrameComponentsMap.put("pm1_0", pm1_0);

        JLabel pm2_5Label = new JLabel("PM 2.5:");

        JLabel pm2_5 = new JLabel();
        pm2_5.setText("----");
        jFrameComponentsMap.put("pm2_5", pm2_5);

        JLabel pm10Label = new JLabel("PM 10:");

        JLabel pm10 = new JLabel();
        pm10.setText("----");
        jFrameComponentsMap.put("pm10", pm10);

        JLabel pmMeasurementTime_label = new JLabel("Measurement time: ");
        JLabel pmMeasurementTime = new JLabel();
        jFrameComponentsMap.put("measurmentTime", pmMeasurementTime);
        
        JLabel appNameLink = new JLabel("pm-station-usb");
        appNameLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        appNameLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(PROJECT_URL));
                } catch (URISyntaxException | IOException ex) {
                    // TODO log it
                }
            }
        });
        appNameLink.setToolTipText("Visit: https://github.com/rjaros87/pm-station-usb");
        appNameLink.setHorizontalAlignment(SwingConstants.RIGHT);

        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(3)
                            .addComponent(connectionBtn, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE))
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(7)
                            .addComponent(pm1_0Label)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(pm1_0, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(17)
                            .addComponent(deviceStatus)
                            .addGap(143)
                            .addComponent(pmMeasurementTime_label, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
                            .addGap(3)
                            .addComponent(pmMeasurementTime, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE))
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(6)
                            .addComponent(pm2_5Label)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(pm2_5, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(pm10Label)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(pm10, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(105, Short.MAX_VALUE))
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
                    .addContainerGap())
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap(595, Short.MAX_VALUE)
                    .addComponent(appNameLink, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
                    .addGap(23))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(14)
                            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(connectionBtn)
                                .addComponent(deviceStatus, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(pmMeasurementTime_label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(pm1_0Label)
                                .addComponent(pm1_0)
                                .addComponent(pm2_5Label)
                                .addComponent(pm2_5)
                                .addComponent(pm10Label)
                                .addComponent(pm10))
                            .addGap(7))
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(19)
                            .addComponent(pmMeasurementTime, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)))
                    .addComponent(chartPanel, 50, 312, Short.MAX_VALUE)
                    .addGap(22)
                    .addComponent(appNameLink)
                    .addContainerGap())
        );
        frame.getContentPane().setLayout(groupLayout);

        JframeComponentsObserver jframeComponentsObserver = new JframeComponentsObserver();
        jframeComponentsObserver.setJframeComponents(jFrameComponentsMap);
        planTowerSensor.addObserver(jframeComponentsObserver);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (planTowerSensor.connectDevice()) {
            connectionBtn.setText("Disconnect");
            deviceStatus.setText("Status: Connected");
            planTowerSensor.startMeasurements();
            connectionBtn.setEnabled(true);
        } else {
            deviceStatus.setText("Status: Device not found");
        }
    }
}
