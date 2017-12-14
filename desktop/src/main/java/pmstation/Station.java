/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */

package pmstation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window.Type;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.SystemUtils;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.dialogs.AboutDlg;
import pmstation.dialogs.ConfigurationDlg;
import pmstation.helpers.MacOSIntegration;
import pmstation.helpers.NativeTrayIntegration;
import pmstation.observers.ChartObserver;
import pmstation.observers.ConsoleObserver;
import pmstation.observers.LabelObserver;
import pmstation.plantower.PlanTowerSensor;

public class Station {
    
    private static final Logger logger = LoggerFactory.getLogger(Station.class);

    private final PlanTowerSensor planTowerSensor;
    private JFrame frame = null;
    private ConfigurationDlg configDlg = null;
    private AboutDlg aboutDlg = null;
    
    public Station(PlanTowerSensor planTowerSensor) {
        this.planTowerSensor = planTowerSensor;
    }

    
    /**
     * @wbp.parser.entryPoint
     */
    public void showUI() {
        frame = new JFrame("Particulate matter station");
        frame.setAlwaysOnTop(Config.instance().to().getBoolean(Config.Entry.ALWAYS_ON_TOP.key(), false));
        frame.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
        SwingUtilities.updateComponentTreeUI(frame);
        setIcon(frame);
        if (Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false)) {
            frame.setType(Type.UTILITY);
        }

        frame.setMinimumSize(new Dimension(Constants.MIN_WINDOW_WIDTH, Constants.MIN_WINDOW_HEIGHT));
        frame.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));

        frame.setDefaultCloseOperation(
                Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false) ? JFrame.HIDE_ON_CLOSE : JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {

                if (frame.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
                    diaplayWarnForDetach(frame);
                    logger.info("Closing the application...");
                    if (!SystemUtils.IS_OS_MAC_OSX) {
                        logger.info("Disconnecting device...");
                        planTowerSensor.disconnectDevice();
                    }
                }
                saveScreenAndDimensions(frame);
                super.windowClosing(windowEvent);
            }
        });

        HashMap<String, JComponent> labelsToBeUpdated = new HashMap<>();
        XYChart chart = new XYChartBuilder().xAxisTitle("samples").yAxisTitle(Constants.UNITS).build();
        chart.getStyler().setXAxisMin((double) 0);
        chart.getStyler().setXAxisMax((double) Constants.CHART_MAX_SAMPLES);
        chart.getStyler().setLegendFont(new Font(Font.SERIF, Font.PLAIN, 10));
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendBackgroundColor(new Color(255, 255, 255, 20)); // white with alpha
        chart.getStyler().setMarkerSize(2);
        chart.getStyler().setAntiAlias(true);
        chart.getStyler().setLegendLayout(LegendLayout.Horizontal);
        
        JPanel chartPanel = new XChartPanel<XYChart>(chart);
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chart.getStyler().setLegendVisible(false);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                chart.getStyler().setLegendVisible(true);
            }
        });
        chartPanel.setMinimumSize(new Dimension(50, 50));
        
        ChartObserver chartObserve = new ChartObserver(chart, chartPanel);
        addObserver(chartObserve);
        addObserver(new ConsoleObserver());

        JLabel deviceStatus = new JLabel("Status: ");
        deviceStatus.setVisible(false); // TODO to be removed?
        JLabel labelStatus = new JLabel("Status..."); // use this one instead...
        labelsToBeUpdated.put("deviceStatus", labelStatus);
        
        JButton btnConnect = new JButton("Connect");
        btnConnect.setFocusable(true);
        btnConnect.grabFocus();
        btnConnect.setEnabled(false);
        btnConnect.addActionListener(actionEvent -> {
            btnConnect.setEnabled(false);
            switch (btnConnect.getText()) {
            case "Connect":
                if (planTowerSensor.connectDevice()) {
                    btnConnect.setText("Disconnect");
                    labelStatus.setText("Status: Connected");
                    planTowerSensor.startMeasurements(Config.instance().to().getInt(Config.Entry.INTERVAL.key(), Constants.DEFAULT_INTERVAL) * 1000L);
                }
                break;
            case "Disconnect":
                diaplayWarnForDetach(frame);
                planTowerSensor.disconnectDevice();
                btnConnect.setText("Connect");
                labelStatus.setText("Status: Disconnected");
                break;
            }
            btnConnect.setEnabled(true);
        });
        labelsToBeUpdated.put("connect", btnConnect);
        
        final JPanel panelMain = new JPanel();
        
        panelMain.setLayout(new MigLayout("", "[50px][100px:120px,grow][150px]", "[29px][16px][338px][16px]"));
        panelMain.add(btnConnect, "cell 0 0,alignx left,aligny center");
        
        panelMain.add(deviceStatus, "flowx,cell 1 0,alignx left,aligny center");
        
        JButton btnCfg = new JButton("");
        btnCfg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openConfigDlg();
            }
        });
        btnCfg.setToolTipText("Configuration");
        btnCfg.setIcon(new ImageIcon(Station.class.getResource("/pmstation/btn_config.png")));
        btnCfg.setMaximumSize(new Dimension(btnCfg.getIcon().getIconWidth()+12, btnCfg.getIcon().getIconHeight()+12));
        panelMain.add(btnCfg, "flowx,cell 2 0,alignx right,aligny center");
        
        JButton btnAbout = new JButton("");
        btnAbout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openAboutDlg();
            }
        });

        btnAbout.setIcon(new ImageIcon(Station.class.getResource("/pmstation/btn_about.png")));
        btnAbout.setMaximumSize(new Dimension(btnAbout.getIcon().getIconWidth()+12, btnAbout.getIcon().getIconHeight()+12));
        btnAbout.setToolTipText("About...");
        
        panelMain.add(btnAbout, "cell 2 0,alignx right,aligny center");
        
        JPanel panelMeasurements = new JPanel();
        panelMeasurements.setBorder(new TitledBorder(null, "<html><b>Last measurements</b></html>", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelMain.add(panelMeasurements, "cell 0 1 3 1,grow");
        panelMeasurements.setLayout(new MigLayout("", "[50px][6px][70px:100px][6px][50px][6px][70px:100px][6px][42px][12px][70px:137px]", "[16px][]"));
        
                JLabel pm1_0Label = new JLabel("PM 1.0:");
                panelMeasurements.add(pm1_0Label, "cell 0 0,alignx left,aligny top");
                
                        JLabel pm1_0 = new JLabel();
                        panelMeasurements.add(pm1_0, "cell 2 0,growx,aligny top");
                        pm1_0.setText("----");
                        labelsToBeUpdated.put("pm1_0", pm1_0);
                        
                                JLabel pm2_5Label = new JLabel("PM 2.5:");
                                panelMeasurements.add(pm2_5Label, "cell 4 0,alignx left,aligny top");
                                
                                        JLabel pm2_5 = new JLabel();
                                        panelMeasurements.add(pm2_5, "cell 6 0,growx,aligny top");
                                        pm2_5.setText("----");
                                        labelsToBeUpdated.put("pm2_5", pm2_5);
                                        
                                                JLabel pm10Label = new JLabel("PM 10:");
                                                panelMeasurements.add(pm10Label, "cell 8 0,alignx left,aligny top");
                                                
                                                        JLabel pm10 = new JLabel();
                                                        panelMeasurements.add(pm10, "cell 10 0,alignx left,aligny top");
                                                        pm10.setText("----");
                                                        labelsToBeUpdated.put("pm10", pm10);
                                                        
                                                                JLabel pmMeasurementTime_label = new JLabel("<html><small>Time: </small></html>");
                                                                panelMeasurements.add(pmMeasurementTime_label, "cell 0 1 2 1,alignx left");
                                                                JLabel pmMeasurementTime = new JLabel();
                                                                panelMeasurements.add(pmMeasurementTime, "cell 2 1 9 1");
                                                                labelsToBeUpdated.put("measurementTime", pmMeasurementTime);
        panelMain.add(chartPanel, "cell 0 2 3 1,grow");

        JPanel panelStatus = new JPanel();
        panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panelStatus.setPreferredSize(new Dimension(frame.getWidth(), 16));
        panelStatus.setLayout(new BorderLayout(0, 0));
        
        labelStatus.setForeground(Color.GRAY);
        labelStatus.setHorizontalAlignment(SwingConstants.LEFT);
        panelStatus.add(labelStatus, BorderLayout.WEST);
        
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.getContentPane().add(panelMain); //, BorderLayout.NORTH);
        frame.getContentPane().add(panelStatus, BorderLayout.SOUTH);
        
        JLabel appNameLink = new JLabel(" // " + Constants.PROJECT_NAME);
        panelStatus.add(appNameLink, BorderLayout.EAST);
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
        labelStatus.setText("... .   .     .         .               .");
        LabelObserver labelObserver = new LabelObserver();
        labelObserver.setLabelsToUpdate(labelsToBeUpdated);
        addObserver(labelObserver);
        
        frame.pack();
        setScreenAndDimensions(frame);  // must be after frame.pack()
        frame.setVisible(!Config.instance().to().getBoolean(Config.Entry.HIDE_MAIN_WINDOW.key(), false));
        integrateNativeOS(frame);

        // register dialogs (they can be opened from SystemTray and OSX menubar)
        aboutDlg = new AboutDlg(frame, "About");
        configDlg = new ConfigurationDlg(frame, "Configuration");

        handleAutostart(labelStatus, btnConnect);
        btnConnect.setEnabled(true);
    }

    public void addObserver(IPlanTowerObserver observer) {
        planTowerSensor.addObserver(observer);
    }

    public void openConfigDlg() {
        SwingUtilities.invokeLater(() -> { 
            if (configDlg != null) {
                configDlg.show(); // modal i.e. it blocks
                scheduleMeasurements();
            } 
        });
    }

    public void openAboutDlg() {
        SwingUtilities.invokeLater(() -> { if (aboutDlg != null) { aboutDlg.show(); } });
    }
    
    public void closeApp() {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
    }
    
    public void setVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(visible);
            if (visible) {
                frame.setExtendedState(JFrame.NORMAL);
                frame.toFront();
                frame.requestFocus();
            }
        });
    }

    private void handleAutostart(JLabel labelStatus, JButton connectionBtn) {
        boolean autostart = Config.instance().to().getBoolean(Config.Entry.AUTOSTART.key(), true);
        if (autostart) {
            startMeasurements(labelStatus, connectionBtn);
        } else {
            labelStatus.setText("Status: not started");
        }
    }
    
    private void startMeasurements(JLabel labelStatus, JButton connectionBtn) {
        SwingUtilities.invokeLater(() -> {
            if (planTowerSensor.connectDevice()) {
                connectionBtn.setText("Disconnect");
                labelStatus.setText("Status: Connected");
                scheduleMeasurements();
            } else {
                labelStatus.setText("Status: Device not found");
            }
        });
    }
    
    private void scheduleMeasurements() {
        if (planTowerSensor.isConnected()) {
            planTowerSensor.startMeasurements(Config.instance().to().getInt(Config.Entry.INTERVAL.key(), Constants.DEFAULT_INTERVAL) * 1000L);
        }
    }
    
    private void integrateNativeOS(JFrame frame) {
        if (SystemUtils.IS_OS_MAC_OSX) {
            new MacOSIntegration(this).integrate();
        }
        if (Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false)) {
            new NativeTrayIntegration(this).integrate();
        }
    }

    private void setIcon(JFrame frame) {
        ImageIcon icon = new ImageIcon(Station.class.getResource("/pmstation/app-icon.png"));
        frame.setIconImage(icon.getImage());
        if (SystemUtils.IS_OS_MAC_OSX) {
            try {
                // equivalent of:
                // com.apple.eawt.Application.getApplication().setDockIconImage( new ImageIcon(Station.class.getResource("/pmstation/btn_config.png")).getImage());
                Class<?> clazz = Class.forName( "com.apple.eawt.Application", false, null);
                Method methodGetApp = clazz.getMethod("getApplication");
                Method methodSetDock = clazz.getMethod("setDockIconImage", Image.class);
                methodSetDock.invoke(methodGetApp.invoke(null), icon.getImage());
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("Unable to set dock icon", e);
            }
            
        }
    }

    private void saveScreenAndDimensions(JFrame frame) {
        if (!frame.isVisible()) {
            return;
        }

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
            logger.info("Saved window dimensions to config file (single screen found)");
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
            logger.info("Saved window dimensions to config file (multi screen found)");
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

    private void diaplayWarnForDetach(JFrame parent) {
        if (Config.instance().to().getBoolean(Config.Entry.WARN_ON_OSX_TO_DETACH.key(), SystemUtils.IS_OS_MAC_OSX)) {
            if (planTowerSensor.isConnected()) {
                JOptionPane.showMessageDialog(parent,
                        "<html>The sensor is still attached.<br><br>" +
                        "This instance or the next start of the application may <b>hang</b><br>" +
                        "when the device is attached and app is closing. In such a case only reboot helps.<br>" +
                        "This behavior is being observed when using PL2303 and its original driver.<br><br>" +
                        "You can now forcibly detach the device now.<br><br>" +
                        "Press OK to continue closing.</html>",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
        
    }
    
}
