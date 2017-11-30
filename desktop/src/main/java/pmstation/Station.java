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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import pmstation.observers.JframeChartObserver;
import pmstation.observers.JframeComponentsObserver;
import pmstation.plantower.PlanTowerSensor;

public class Station {
    
    private static final Logger logger = LoggerFactory.getLogger(Station.class);
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
                logger.info("Disconnect device...");
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
        
        JLabel appNameLink = new JLabel("pm-station-usb");
        appNameLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        appNameLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(PROJECT_URL));
                } catch (URISyntaxException | IOException ex) {
                    logger.warn("Failed to parse URI", ex);
                }
            }
        });
        appNameLink.setToolTipText("Visit: " + PROJECT_URL);
        appNameLink.setHorizontalAlignment(SwingConstants.RIGHT);
        frame.getContentPane().setLayout(new MigLayout("", "[50px][46px][6px][137px][271px]", "[29px][16px][338px][16px]"));
        frame.getContentPane().add(connectionBtn, "cell 0 0,alignx left,aligny top");
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Last measurements", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(panel, "cell 0 1 5 1,grow");
        panel.setLayout(new MigLayout("", "[50px][6px][70px][6px][46px][6px][76px][6px][42px][12px][137px]", "[16px][]"));
        
                JLabel pm1_0Label = new JLabel("PM 1.0:");
                panel.add(pm1_0Label, "cell 0 0,alignx left,aligny top");
                
                        JLabel pm1_0 = new JLabel();
                        panel.add(pm1_0, "cell 2 0,growx,aligny top");
                        pm1_0.setText("----");
                        jFrameComponentsMap.put("pm1_0", pm1_0);
                        
                                JLabel pm2_5Label = new JLabel("PM 2.5:");
                                panel.add(pm2_5Label, "cell 4 0,alignx left,aligny top");
                                
                                        JLabel pm2_5 = new JLabel();
                                        panel.add(pm2_5, "cell 6 0,growx,aligny top");
                                        pm2_5.setText("----");
                                        jFrameComponentsMap.put("pm2_5", pm2_5);
                                        
                                                JLabel pm10Label = new JLabel("PM 10:");
                                                panel.add(pm10Label, "cell 8 0,alignx left,aligny top");
                                                
                                                        JLabel pm10 = new JLabel();
                                                        panel.add(pm10, "cell 10 0,alignx left,aligny top");
                                                        pm10.setText("----");
                                                        jFrameComponentsMap.put("pm10", pm10);
                                                        
                                                                JLabel pmMeasurementTime_label = new JLabel("Time: ");
                                                                panel.add(pmMeasurementTime_label, "cell 0 1 2 1,alignx left");
                                                                JLabel pmMeasurementTime = new JLabel();
                                                                panel.add(pmMeasurementTime, "cell 2 1 9 1");
                                                                jFrameComponentsMap.put("measurmentTime", pmMeasurementTime);
        frame.getContentPane().add(deviceStatus, "cell 1 0 4 1,alignx left,aligny center");
        frame.getContentPane().add(chartPanel, "cell 0 2 5 1,grow");
        frame.getContentPane().add(appNameLink, "cell 2 3 3 1,alignx right,aligny top");

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
            connectionBtn.setEnabled(true);
        }
    }
}
