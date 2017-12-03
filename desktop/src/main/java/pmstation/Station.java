/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */

package pmstation;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.SystemUtils;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.dialogs.ConfigurationDlg;
import pmstation.observers.ChartObserver;
import pmstation.observers.ConsoleObserver;
import pmstation.observers.LabelObserver;
import pmstation.plantower.PlanTowerSensor;

public class Station {
    
    private static final Logger logger = LoggerFactory.getLogger(Station.class);

    private final PlanTowerSensor planTowerSensor;
    
    public Station(PlanTowerSensor planTowerSensor) {
        this.planTowerSensor = planTowerSensor;
    }

    
    /**
     * @wbp.parser.entryPoint
     */
    public void showUI() {
        final JFrame frame = new JFrame("Particulate matter station");

        frame.getContentPane().setMinimumSize(new Dimension(474, 180));
        frame.getContentPane().setPreferredSize(new Dimension(740, 480));
        
        // Enforce min window size on OSX...
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension currentDim = frame.getSize();
                Dimension minDim = frame.getMinimumSize();
                Dimension toSetDim = new Dimension(Math.max(currentDim.width, minDim.width),
                                                   Math.max(currentDim.height, minDim.height));
                frame.setSize(toSetDim);
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (!SystemUtils.IS_OS_MAC_OSX) {
                    logger.info("Disconnecting device...");
                    planTowerSensor.disconnectDevice();
                }
                saveScreenAndDimensions(frame);
                super.windowClosing(windowEvent);
            }

        });

        HashMap<String, JLabel> jFrameComponentsMap = new HashMap<>();
        XYChart chart = new XYChartBuilder().xAxisTitle("sample").yAxisTitle("\u03BCg/m\u00B3").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setMarkerSize(2);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);
        chartPanel.setMinimumSize(new Dimension(50, 50));
        ChartObserver chartObserve = new ChartObserver();
        chartObserve.setChart(chart);
        chartObserve.setChartPanel(chartPanel);
        planTowerSensor.addObserver(chartObserve);
        planTowerSensor.addObserver(new ConsoleObserver());

        JLabel deviceStatus = new JLabel("Status: ");
        JButton connectionBtn = new JButton("Connect");
        connectionBtn.setFocusable(false);
        connectionBtn.setEnabled(false);
        connectionBtn.addActionListener(actionEvent -> {
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
        });
        frame.getContentPane().setLayout(new MigLayout("", "[50px][100px:200px,grow][100px]", "[29px][16px][338px][16px]"));
        frame.getContentPane().add(connectionBtn, "cell 0 0,alignx left,aligny center");
        
        jFrameComponentsMap.put("deviceStatus", deviceStatus);
        frame.getContentPane().add(deviceStatus, "flowx,cell 1 0,alignx left,aligny center");
        
        JLabel appNameLink = new JLabel(Constants.PROJECT_NAME);
        appNameLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        appNameLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(Constants.PROJECT_URL));
                } catch (URISyntaxException | IOException ex) {
                    logger.warn("Failed to parse URI", ex);
                }
            }
        });
        appNameLink.setToolTipText("Visit: " + Constants.PROJECT_URL);
        appNameLink.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JButton btnCfg = new JButton("");
        btnCfg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ConfigurationDlg(frame, "Configuration").initGUI();
            }
        });
        btnCfg.setToolTipText("Configuration");
        btnCfg.setIcon(new ImageIcon(Station.class.getResource("/pmstation/btn_config.png")));
        frame.getContentPane().add(btnCfg, "cell 2 0,alignx right,aligny center");
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Last measurements", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(panel, "cell 0 1 3 1,grow");
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
                                                                jFrameComponentsMap.put("measurementTime", pmMeasurementTime);
        frame.getContentPane().add(chartPanel, "cell 0 2 3 1,grow");
        frame.getContentPane().add(appNameLink, "cell 2 3 3 1,alignx right,aligny top");

        LabelObserver jframeComponentsObserver = new LabelObserver();
        jframeComponentsObserver.setJframeComponents(jFrameComponentsMap);
        planTowerSensor.addObserver(jframeComponentsObserver);
        frame.pack();
        setScreenAndDimensions(frame);  // must be after frame.pack()
        frame.setVisible(true);

        boolean autostart = Config.instance().to().getBoolean(Config.Entry.AUTOSTART.key(), !SystemUtils.IS_OS_MAC_OSX);
        if (autostart) {
            if (planTowerSensor.connectDevice()) {
                connectionBtn.setText("Disconnect");
                deviceStatus.setText("Status: Connected");
                planTowerSensor.startMeasurements();
            } else {
                deviceStatus.setText("Status: Device not found");
            }
        } else {
            deviceStatus.setText("Status: not started");
        }
        connectionBtn.setEnabled(true);
    }
    
    private void saveScreenAndDimensions(JFrame frame) {
        // check multiple displays
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Point pos = frame.getLocationOnScreen();
        Dimension size = frame.getSize();
        if (screens.length == 1) {
            Config.instance().to().setProperty(Config.Entry.POS_X.key(), pos.x);
            Config.instance().to().setProperty(Config.Entry.POS_Y.key(), pos.y);
            Config.instance().to().setProperty(Config.Entry.POS_WIDTH.key(), size.width);
            Config.instance().to().setProperty(Config.Entry.POS_HEIGHT.key(), size.height);
        } else {
            Rectangle screenBounds = frame.getGraphicsConfiguration().getBounds();
            pos.x -= screenBounds.x;
            pos.y -= screenBounds.y;
            GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
            Config.instance().to().setProperty(Config.Entry.SCREEN_POS_X.key(), pos.x);
            Config.instance().to().setProperty(Config.Entry.SCREEN_POS_Y.key(), pos.y);
            Config.instance().to().setProperty(Config.Entry.SCREEN_POS_WIDTH.key(), size.width);
            Config.instance().to().setProperty(Config.Entry.SCREEN_POS_HEIGHT.key(), size.height);
            
            Config.instance().to().setProperty(Config.Entry.SCREEN.key(), device.getIDstring());
        }
    }
    
    private void setDimensions(JFrame frame, int x, int y, int width, int height) {
        if (x >= 0 && y >= 0 && width > 0 && height > 0) {
            frame.setLocation(x, y);
            frame.setSize(width, height);
        }
    }

    private void setScreenAndDimensions(JFrame frame) {
        // check multiple displays
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        String display = Config.instance().to().getString(Config.Entry.SCREEN.key(), "-");
        GraphicsDevice[] screens = ge.getScreenDevices();
        if (screens.length == 1) {
            setDimensions(frame, 
                    Config.instance().to().getInt(Config.Entry.POS_X.key(), -1),
                    Config.instance().to().getInt(Config.Entry.POS_Y.key(), -1),
                    Config.instance().to().getInt(Config.Entry.POS_WIDTH.key(), -1),
                    Config.instance().to().getInt(Config.Entry.POS_HEIGHT.key(), -1));
        } else {
            for (GraphicsDevice screen : screens) { // if multiple screens available then try to open on saved display
                if (screen.getIDstring().contentEquals(display)) {
                    JFrame dummy = new JFrame(screen.getDefaultConfiguration());
                    frame.setLocationRelativeTo(dummy);
                    dummy.dispose();
                    Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
                    Point pos = new Point(Config.instance().to().getInt(Config.Entry.SCREEN_POS_X.key(), -1),
                                          Config.instance().to().getInt(Config.Entry.SCREEN_POS_Y.key(), -1));
                    if (pos.x >= 0 && pos.y >= 0) {
                        pos.x += screenBounds.x;
                        pos.y += screenBounds.y;
                        logger.info(" new pos {} {} ", pos.x, pos.y);
                    }
                    setDimensions(frame,
                            pos.x,
                            pos.y,
                            Config.instance().to().getInt(Config.Entry.SCREEN_POS_WIDTH.key(), -1),
                            Config.instance().to().getInt(Config.Entry.SCREEN_POS_HEIGHT.key(), -1));
                    break;
                }
            }
        }
    }
}
