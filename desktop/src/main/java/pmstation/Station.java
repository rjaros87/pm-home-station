/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
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
import java.awt.SystemTray;
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
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
import pmstation.aqi.AQIAbout;
import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.dialogs.AboutDlg;
import pmstation.dialogs.ConfigurationDlg;
import pmstation.helpers.ResourceHelper;
import pmstation.helpers.VersionChecker;
import pmstation.integration.MacOSIntegration;
import pmstation.integration.NativeTrayIntegration;
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
    private NativeTrayIntegration nativeTray = null;
    
    public Station(PlanTowerSensor planTowerSensor) {
        this.planTowerSensor = planTowerSensor;
    }

    
    /**
     * @wbp.parser.entryPoint
     */
    public void showUI() {
        frame = new JFrame(Constants.MAIN_WINDOW_TITLE);
        frame.setAlwaysOnTop(Config.instance().to().getBoolean(Config.Entry.ALWAYS_ON_TOP.key(), false));
        frame.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
        SwingUtilities.updateComponentTreeUI(frame);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        setIcon(frame);

        // Windows 10 strange behaviour on multiscreen and multidisplay
        if (SystemUtils.IS_OS_MAC_OSX && Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false)) {
            frame.setType(Type.UTILITY);
        }

        frame.setMinimumSize(new Dimension(Constants.MIN_WINDOW_WIDTH, Constants.MIN_WINDOW_HEIGHT));
        frame.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));

        frame.setDefaultCloseOperation(
                SystemTray.isSupported() && Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false) ?
                        JFrame.HIDE_ON_CLOSE : JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {

                if (frame.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
                    diaplayWarnForDetach(frame);
                    logger.info("Closing the application...");
                    logger.info("Disconnecting device...");
                    planTowerSensor.disconnectDevice();
                }
                saveScreenAndDimensions(frame);
                super.windowClosing(windowEvent);
            }
        });

        LabelObserver.LabelsCollector labelsCollector = new LabelObserver.LabelsCollector();
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
        
        addObserver(new ChartObserver(chart, chartPanel));
        addObserver(new ConsoleObserver());

        JLabel labelStatus = new JLabel("Status...");
        labelsCollector.add(LabelObserver.LabelsCollector.LABEL.DEVICE_STATUS, labelStatus);
        
        JButton btnConnect = new JButton("Connect");
        btnConnect.setFocusable(true);
        btnConnect.grabFocus();
        btnConnect.setEnabled(false);
        btnConnect.addActionListener(actionEvent -> {
            btnConnect.setEnabled(false);   // TODO move the connection long running tasks to a separate thread and don't use swing's execLater for this purpose
            switch (btnConnect.getText()) { // TODO do this better thru label observer...
            case "Connect":
                labelStatus.setText("Status: Connecting...");
                btnConnect.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    if (planTowerSensor.connectDevice()) {
                        btnConnect.setText("Disconnect"); // TODO move this to label observer...
                        btnConnect.setToolTipText(planTowerSensor.portDetails());
                        scheduleMeasurements();
                    }
                    btnConnect.setEnabled(true);
                });
                break;
            case "Disconnect":
                diaplayWarnForDetach(frame);
                planTowerSensor.disconnectDevice();
                btnConnect.setText("Connect");
                btnConnect.setToolTipText(planTowerSensor.portDetails());
                break;
            }
            btnConnect.setEnabled(true);
        });
        labelsCollector.add(LabelObserver.LabelsCollector.LABEL.CONNECT, btnConnect);
        
        final JPanel panelMain = new JPanel();
        
        panelMain.setLayout(new MigLayout("", "[50px][100px:120px,grow][150px]", "[:29px:29px][16px][338px,grow][16px]"));
        panelMain.add(btnConnect, "cell 0 0,alignx left,aligny center");
        
        try {
            JButton btnState = new JButton("");
            btnState.setToolTipText("State indicator");
            Icon referenceIcon = ResourceHelper.getIcon("btn_config.png");
            btnState.setMaximumSize(new Dimension(ResourceHelper.getIcon("btn_config.png").getIconWidth()+12, ResourceHelper.getIcon("btn_config.png").getIconHeight()+12));
            btnState.setIcon(new ImageIcon(ResourceHelper.getAppIcon("app-icon-disconnected.png").getScaledInstance(referenceIcon.getIconWidth(), -1, Image.SCALE_SMOOTH)));
            panelMain.add(btnState, "flowx,cell 2 0,alignx right,aligny center");
            labelsCollector.add(LabelObserver.LabelsCollector.LABEL.ICON, btnState);
        } catch (IOException e) {
            logger.warn("Ugh, there was an error loading an icon", e);
        }
        
        JButton btnCfg = new JButton("");
        btnCfg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openConfigDlg();
            }
        });
        btnCfg.setToolTipText("Configuration");
        btnCfg.setIcon(ResourceHelper.getIcon("btn_config.png"));
        btnCfg.setMaximumSize(new Dimension(btnCfg.getIcon().getIconWidth()+12, btnCfg.getIcon().getIconHeight()+12));
        panelMain.add(btnCfg, "flowx,cell 2 0,alignx right,aligny center");
        
        JButton btnAbout = new JButton("");
        btnAbout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openAboutDlg();
            }
        });

        btnAbout.setIcon(ResourceHelper.getIcon("btn_about.png"));
        btnAbout.setMaximumSize(new Dimension(btnAbout.getIcon().getIconWidth()+12, btnAbout.getIcon().getIconHeight()+12));
        btnAbout.setToolTipText("About...");
        
        panelMain.add(btnAbout, "cell 2 0,alignx right,aligny center");
        
        JPanel panelMeasurements = new JPanel();
        panelMeasurements.setBorder(new TitledBorder(null, "<html><b>Last measurements</b></html>", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelMain.add(panelMeasurements, "cell 0 1 3 1,grow");
        panelMeasurements.setLayout(new MigLayout("", "[:20px:40px][1px:1px:1px][60px:80px:80px,grow 60][1px:1px:3px][:20px:40px][1px:1px:1px][80px:80px:100px,grow][1px:1px:3px][:20px:40px][1px:1px:1px][80px:80px:100px,grow]", "[::20px][::2px][::15px]"));
        
                JLabel pm1_0Label = new JLabel("PM 1.0:");
                panelMeasurements.add(pm1_0Label, "cell 0 0,alignx left,aligny top");
                
                        JLabel pm1_0 = new JLabel();
                        panelMeasurements.add(pm1_0, "flowy,cell 2 0,alignx leading,aligny top");
                        pm1_0.setText("----");
                        labelsCollector.add(LabelObserver.LabelsCollector.LABEL.PM1, pm1_0);
                        
                                JLabel pm2_5Label = new JLabel("PM 2.5:");
                                panelMeasurements.add(pm2_5Label, "cell 4 0,alignx left,aligny top");
                                
                                        JLabel pm2_5 = new JLabel();
                                        panelMeasurements.add(pm2_5, "flowx,cell 6 0,alignx leading,aligny top");
                                        pm2_5.setText("----");
                                        labelsCollector.add(LabelObserver.LabelsCollector.LABEL.PM25, pm2_5);
                                        
                                                JLabel pm10Label = new JLabel("PM 10:");
                                                panelMeasurements.add(pm10Label, "cell 8 0,alignx left,aligny top");
                                                
                                                        JLabel pm10 = new JLabel();
                                                        panelMeasurements.add(pm10, "flowx,cell 10 0,alignx leading,aligny top");
                                                        pm10.setText("----");
                                                        labelsCollector.add(LabelObserver.LabelsCollector.LABEL.PM10, pm10);

                                                        JLabel pmMeasurementTime_label = new JLabel("<html><small>Time: </small></html>");
                                                        panelMeasurements.add(pmMeasurementTime_label, "cell 0 2,alignx left,aligny top");

                                                        JLabel pmMeasurementTime = new JLabel();
                                                        panelMeasurements.add(pmMeasurementTime, "cell 2 2 6 1,aligny top");
                                                        labelsCollector.add(LabelObserver.LabelsCollector.LABEL.MEASURMENT_TIME, pmMeasurementTime);

                                                        JLabel lblAqi = new JLabel("<html><small>*) AQI colors</small></html>");
                                                        lblAqi.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                                        lblAqi.addMouseListener(new MouseAdapter() {
                                                            @Override
                                                            public void mouseClicked(MouseEvent e) {
                                                                openUrl(AQIAbout.AQI_PL_INFO);
                                                            }
                                                        });
                                                        lblAqi.setToolTipText("<html>" + AQIAbout.getHtmlTable() + "</html>");
                                                        panelMeasurements.add(lblAqi, "cell 8 2 3 1,alignx left");
                                                        
        panelMain.add(chartPanel, "cell 0 2 3 1,grow");

        JPanel panelStatus = new JPanel();
        panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panelStatus.setPreferredSize(new Dimension(frame.getWidth(), 16));
        panelStatus.setLayout(new BorderLayout(0, 0));
        
        labelStatus.setForeground(Color.GRAY);
        labelStatus.setHorizontalAlignment(SwingConstants.LEFT);
        panelStatus.add(labelStatus, BorderLayout.WEST);
        
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.getContentPane().add(panelMain);
        frame.getContentPane().add(panelStatus, BorderLayout.SOUTH);
        
        JLabel appNameLink = new JLabel(" // " + Constants.PROJECT_NAME);
        panelStatus.add(appNameLink, BorderLayout.EAST);
        appNameLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        appNameLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(Constants.PROJECT_URL);
            }
        });
        appNameLink.setToolTipText("Visit: " + Constants.PROJECT_URL);
        appNameLink.setHorizontalAlignment(SwingConstants.RIGHT);
        labelStatus.setText("... .   .     .         .               .");
        LabelObserver labelObserver = new LabelObserver(labelsCollector);
        addObserver(labelObserver);
        
        frame.pack();
        setScreenAndDimensions(frame);  // must be after frame.pack()
        frame.setVisible(!Config.instance().to().getBoolean(Config.Entry.HIDE_MAIN_WINDOW.key(), Constants.HIDE_MAIN_WINDOW));
        integrateNativeOS(frame);

        // register dialogs (they can be opened from SystemTray and OSX menubar)
        aboutDlg = new AboutDlg(frame, "About");
        configDlg = new ConfigurationDlg(frame, "Configuration");

        handleAutostart(labelStatus, btnConnect);
        btnConnect.setEnabled(true);

        if (Config.instance().to().getBoolean(Config.Entry.CHECK_LATEST_VERSION.key(), true)) {
            SwingUtilities.invokeLater(() -> {
                VersionChecker vc = new VersionChecker();
                VersionChecker.LatestRelease lr = vc.check();
                if (lr.isNewerVersion()) {
                    String newVerInfo = "A newer version version (" + lr.getVersion() + ") is available!";
                    labelStatus.setText(newVerInfo);
                    if (nativeTray != null) {
                        nativeTray.displayTrayMessage("New version available!", newVerInfo);
                    }
                }
            });

        }
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
                boolean alwaysOnTop = frame.isAlwaysOnTop();
                frame.setExtendedState(frame.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL);
                frame.setAlwaysOnTop(true);
                frame.toFront();
                frame.requestFocus();
                frame.setAlwaysOnTop(alwaysOnTop);
                frame.repaint();
                frame.requestFocus();
            }
        });
    }

    private void handleAutostart(JLabel labelStatus, JButton connectionBtn) {
        boolean autostart = Config.instance().to().getBoolean(Config.Entry.AUTOSTART.key(), true);
        if (autostart) {
            startMeasurements(labelStatus, connectionBtn);
        } else {
            labelStatus.setText("Status: not connected");
        }
    }
    
    private void startMeasurements(JLabel labelStatus, JButton connectionBtn) {
        labelStatus.setText("Status: Autoconnecting...");
        connectionBtn.setEnabled(false);
        SwingUtilities.invokeLater(() -> {
            if (planTowerSensor.connectDevice()) {
                connectionBtn.setText("Disconnect");
                labelStatus.setText("Status: Connected");
                scheduleMeasurements();
            } else {
                labelStatus.setText("Status: Device not found");
                planTowerSensor.disconnectDevice();
            }
            connectionBtn.setToolTipText(planTowerSensor.portDetails());
            connectionBtn.setEnabled(true);
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
        if (SystemTray.isSupported() && Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), Constants.SYSTEM_TRAY)) {
            nativeTray = new NativeTrayIntegration(this);
            nativeTray.integrate();
        } else {
            logger.info("System tray integration is either not supported or disabled in configuration.");
        }
    }

    private void setIcon(JFrame frame) {
        try {
            Image icon = ResourceHelper.getAppIcon(Constants.DEFAULT_ICON); 
            frame.setIconImage(icon);
            if (SystemUtils.IS_OS_MAC_OSX) {
                try {
                    // equivalent of:
                    // com.apple.eawt.Application.getApplication().setDockIconImage( new ImageIcon(Station.class.getResource("/pmstation/btn_config.png")).getImage());
                    Class<?> clazz = Class.forName( "com.apple.eawt.Application", false, null);
                    Method methodGetApp = clazz.getMethod("getApplication");
                    Method methodSetDock = clazz.getMethod("setDockIconImage", Image.class);
                    methodSetDock.invoke(methodGetApp.invoke(null), icon);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error("Unable to set dock icon", e);
                }
                
            }
        } catch (IOException e) {
            logger.error("Error loading icon", e);
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
        if (width > 0 && height > 0) {
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
            logger.info("Only one screen found - going to set the previous dimensions and position");
            setDimensions(frame, 
                    Config.instance().to().getInt(Config.Entry.POS_X.key(), -1),
                    Config.instance().to().getInt(Config.Entry.POS_Y.key(), -1),
                    Config.instance().to().getInt(Config.Entry.POS_WIDTH.key(), -1),
                    Config.instance().to().getInt(Config.Entry.POS_HEIGHT.key(), -1));
        } else {
            for (GraphicsDevice screen : screens) { // if multiple screens available then try to open on saved display
                logger.info("Checking screen: {} in hunt for the last remembered one: {}", screen.getIDstring(), display);
                if (screen.getIDstring().contentEquals(display)) {
                    logger.info("\tFound remembered dimensions and position for this screen: {}. Going to use it and set it.", display);
                    JFrame dummy = new JFrame(screen.getDefaultConfiguration());
                    frame.setLocationRelativeTo(dummy);
                    dummy.dispose();
                    Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
                    Point pos = new Point(Config.instance().to().getInt(Config.Entry.SCREEN_POS_X.key(), -1),
                                          Config.instance().to().getInt(Config.Entry.SCREEN_POS_Y.key(), -1));

                    logger.info("\tGoing to set position to: {}, {} relatively to the screen...", pos.x, pos.y);
                    logger.info("\tScreen bounds are: {}, {} and {}x{}...", screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height);

                    if (pos.x >= 0 && pos.y >= 0) {
                        pos.x += screenBounds.x;
                        pos.y += screenBounds.y;
                        logger.info("\tNew position to target the screen: {} {} ", pos.x, pos.y);
                    }
                    logger.info("\tGoing to set position to: {}, {} and dimensions to: {}x{}", pos.x, pos.y,
                            Config.instance().to().getInt(Config.Entry.SCREEN_POS_WIDTH.key(), -1),
                            Config.instance().to().getInt(Config.Entry.SCREEN_POS_HEIGHT.key(), -1));
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
                        "when the device is still attached while app or port is being closed.<br>" +
                        "<b>In such a case only reboot helps.</b><br><br>" +
                        "This behavior is being observed when using some cheap PL2303<br>" +
                        "uart-to-usb and their drivers.<br><br>" +
                        "You can now forcibly detach the device now.<br><br>" +
                        "Press OK to continue closing.</html>",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
        
    }
    
    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException ex) {
            logger.warn("Failed to parse URI", ex);
        }        
    }

}
