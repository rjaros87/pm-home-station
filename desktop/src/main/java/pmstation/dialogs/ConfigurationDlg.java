/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.dialogs;

import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.serial.SerialUART;

public class ConfigurationDlg {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDlg.class);

    private JFrame mainFrame;
    private String title;
    private JSpinner textInterval;
    private JDialog frame = null;
    private JTextField textAddDevice;

    public ConfigurationDlg(JFrame parent, String title) {
        this.mainFrame = parent;
        this.title = title;
    }

    /**
     * @wbp.parser.entryPoint
     */
    public void show() {
        if (frame != null) {
            frame.toFront();
            frame.requestFocus();
            return;
        }
        frame = new JDialog(mainFrame, title, ModalityType.APPLICATION_MODAL);
        frame.setResizable(false);
        frame.setBounds(350, 350, 588, 496);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(mainFrame);
        Container mainCfg = frame.getContentPane();

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainCfg.add(tabbedPane);

        JPanel panelGeneral = new JPanel();
        panelGeneral.setBorder(new TitledBorder(null, "<html><b>General</b></html>", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));

        JPanel panelUI = new JPanel();
        panelUI.setBorder(new TitledBorder(null, "<html><b>User Interface</b></html>", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));

        JPanel panelDevice = new JPanel();
        panelDevice.setBorder(new TitledBorder(null, "<html><b>Device</b></html>", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));

        JPanel panelBottom = new JPanel();
        panelBottom.setBorder(null);

        tabbedPane.addTab("General", panelGeneral);
        tabbedPane.addTab("User Interface", panelUI);
        tabbedPane.addTab("Device", panelDevice);
        panelDevice.setLayout(null);

        JList<String> listAvailableDevices = new JList<String>();
        listAvailableDevices.setToolTipText("The list of available devices found in the system");
        JLabel lblWarnUserOSX = new JLabel(
                "<html>Warn before close / disconnect if device is not detached:<br><small><i>- a workaround for macOS and faulty PL2303 drivers</i></small></html>");
        lblWarnUserOSX.setBounds(6, 22, 358, 29);
        panelDevice.add(lblWarnUserOSX);
        lblWarnUserOSX.setEnabled(SystemUtils.IS_OS_MAC_OSX);

        JCheckBox chbxWarnOSX = new JCheckBox("");
        chbxWarnOSX.setBounds(510, 22, 30, 23);
        panelDevice.add(chbxWarnOSX);
        chbxWarnOSX.setEnabled(SystemUtils.IS_OS_MAC_OSX);
        chbxWarnOSX.setSelected(!SystemUtils.IS_OS_MAC_OSX || Config.instance().to()
                .getBoolean(Config.Entry.WARN_ON_OSX_TO_DETACH.key(), SystemUtils.IS_OS_MAC_OSX));
        chbxWarnOSX.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Config.instance().to().setProperty(Config.Entry.WARN_ON_OSX_TO_DETACH.key(), chbxWarnOSX.isSelected());
            }
        });

        JLabel lblPreferredDevices = new JLabel(
                "<html>Preferred devices <small>(priority top -> down)</small>:</html>");
        lblPreferredDevices.setBounds(6, 55, 260, 16);
        panelDevice.add(lblPreferredDevices);

        JList<String> listPreferredDevices = new JList<String>();
        listPreferredDevices.setToolTipText("The list of preferred devices");
        JScrollPane listPreferredDevicesScroller = new JScrollPane();
        listPreferredDevicesScroller.setViewportView(listPreferredDevices);
        listPreferredDevicesScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listPreferredDevicesScroller.setBounds(6, 75, 490, 90);
        listPreferredDevices.setModel(getPreferredDevices());
        panelDevice.add(listPreferredDevicesScroller);

        JButton btnPriorityUp = new JButton("⇑");
        btnPriorityUp.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPriorityUp.setAlignmentY(0.0f);
        btnPriorityUp.setToolTipText("Increase priority");
        btnPriorityUp.setBounds(500, 72, 50, 33);
        btnPriorityUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = listPreferredDevices.getSelectedIndex();
                if (idx > 0) {
                    swapInList((DefaultListModel<String>) listPreferredDevices.getModel(), idx, idx - 1);
                    listPreferredDevices.setSelectedIndex(idx - 1);
                    listPreferredDevices.ensureIndexIsVisible(idx - 1);
                    savePreferredDevices(listPreferredDevices);
                }
            }
        });
        panelDevice.add(btnPriorityUp);

        JButton btwPriorityRemove = new JButton("⌫");
        btwPriorityRemove.setAlignmentY(0.0f);
        btwPriorityRemove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultListModel<String> model = (DefaultListModel<String>) listPreferredDevices.getModel();
                if (listPreferredDevices.getSelectedIndices().length > 0) {
                    int[] tmp = listPreferredDevices.getSelectedIndices();
                    int[] selectedIndices = listPreferredDevices.getSelectedIndices();
                    for (int i = tmp.length - 1; i >= 0; i--) {
                        selectedIndices = listPreferredDevices.getSelectedIndices();
                        model.removeElementAt(selectedIndices[i]);
                        savePreferredDevices(listPreferredDevices);
                    }
                }
            }
        });
        btwPriorityRemove.setToolTipText("Remove from the list");
        btwPriorityRemove.setBounds(500, 103, 50, 33);
        panelDevice.add(btwPriorityRemove);

        JButton btnPriorityDown = new JButton("⇓");
        btnPriorityDown.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPriorityDown.setAlignmentY(0.0f);
        btnPriorityDown.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = listPreferredDevices.getSelectedIndex();
                if (idx + 1 < listPreferredDevices.getModel().getSize()) {
                    swapInList((DefaultListModel<String>) listPreferredDevices.getModel(), idx, idx + 1);
                    listPreferredDevices.setSelectedIndex(idx + 1);
                    listPreferredDevices.ensureIndexIsVisible(idx + 1);
                }
                savePreferredDevices(listPreferredDevices);
                listPreferredDevices.updateUI();
            }
        });
        btnPriorityDown.setToolTipText("Decrease priority");
        btnPriorityDown.setBounds(500, 134, 50, 33);
        panelDevice.add(btnPriorityDown);

        JLabel lblAddPreferredDevice = new JLabel("Add preferred device:");
        lblAddPreferredDevice.setBounds(6, 173, 142, 16);
        panelDevice.add(lblAddPreferredDevice);

        JButton btnAddDevice = new JButton("Add");
        btnAddDevice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addPreferredDevice(textAddDevice.getText());
                listPreferredDevices.setModel(getPreferredDevices()); // refresh
            }
        });
        btnAddDevice.setBounds(425, 167, 70, 29);
        btnAddDevice.setEnabled(false);
        panelDevice.add(btnAddDevice);

        textAddDevice = new JTextField();
        textAddDevice.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            private void check() {
                boolean isOK = false;
                String val = textAddDevice.getText();
                if (val != null && val.replaceAll("\\*", "").trim().length() > 0) {
                    isOK = true;
                    for (int i = 0; i < listPreferredDevices.getModel().getSize(); i++) {
                        if (val.equals(listPreferredDevices.getModel().getElementAt(i))) {
                            isOK = false;
                            break;
                        }
                    }
                }
                btnAddDevice.setEnabled(isOK);
            }
        });
        textAddDevice.setBounds(145, 168, 280, 26);
        panelDevice.add(textAddDevice);
        textAddDevice.setColumns(10);

        JLabel lblAddPreferredDeviceContd = new JLabel(
                "<html><small>You can double click an item on the list below to fill the device name and edit before adding to the list (wildcards supported)</small></html>");
        lblAddPreferredDeviceContd.setBounds(6, 196, 403, 23);
        panelDevice.add(lblAddPreferredDeviceContd);

        JLabel labelAvailableDevices = new JLabel("<html>Available devices:</html>");
        labelAvailableDevices.setBounds(6, 221, 470, 16);
        panelDevice.add(labelAvailableDevices);

        listAvailableDevices.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    textAddDevice.setText(listAvailableDevices.getSelectedValue());
                }
            }
        });
        listAvailableDevices.setBackground(UIManager.getColor("Button.background"));
        listAvailableDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listAvailableDevicesScroller = new JScrollPane();
        listAvailableDevicesScroller.setViewportView(listAvailableDevices);
        listAvailableDevicesScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listAvailableDevicesScroller.setBounds(6, 237, 490, 112);
        panelDevice.add(listAvailableDevicesScroller);
        listAvailableDevices.setModel(getAvailableDevices());

        JButton btnReloadAvailable = new JButton("↻");
        btnReloadAvailable.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        btnReloadAvailable.setAutoscrolls(true);
        btnReloadAvailable.setHorizontalTextPosition(SwingConstants.CENTER);
        btnReloadAvailable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listAvailableDevices.setModel(getAvailableDevices());
                listAvailableDevices.updateUI();
            }
        });
        btnReloadAvailable.setToolTipText("Refresh");
        btnReloadAvailable.setBounds(500, 235, 50, 33);
        panelDevice.add(btnReloadAvailable);

        GroupLayout groupLayout = new GroupLayout(mainCfg);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout
                .createSequentialGroup().addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(tabbedPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                        .addComponent(panelBottom, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE))
                .addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 401, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelBottom, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));

        JButton btnClose = new JButton("OK");
        panelBottom.add(btnClose);
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                frame = null;
            }
        });
        panelUI.setLayout(null);

        JLabel labelAlwaysOnTop = new JLabel("Keep the window always on top:");
        labelAlwaysOnTop.setBounds(6, 31, 386, 16);
        panelUI.add(labelAlwaysOnTop);

        JCheckBox chbxAlwaysOnTop = new JCheckBox();
        chbxAlwaysOnTop.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Config.instance().to().setProperty(Config.Entry.ALWAYS_ON_TOP.key(), chbxAlwaysOnTop.isSelected());
                mainFrame.setAlwaysOnTop(chbxAlwaysOnTop.isSelected());
            }
        });
        chbxAlwaysOnTop.setSelected(Config.instance().to().getBoolean(Config.Entry.ALWAYS_ON_TOP.key(), false));
        chbxAlwaysOnTop.setBounds(494, 24, 55, 29);

        panelUI.add(chbxAlwaysOnTop);

        JLabel lblSystemTray = new JLabel(
                "<html>Show app icon in the System Tray:<br><small><i>- only on selected operating systems<br>- requires app restart</i></small></html>");
        lblSystemTray.setBounds(6, 66, 386, 60);
        lblSystemTray.setEnabled(SystemTray.isSupported());
        panelUI.add(lblSystemTray);

        JCheckBox chbxSystemTray = new JCheckBox("");

        chbxSystemTray.setSelected(
                SystemTray.isSupported() && Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false));
        chbxSystemTray.setBounds(494, 74, 55, 23);
        chbxSystemTray.setEnabled(SystemTray.isSupported());
        panelUI.add(chbxSystemTray);

        JLabel lblHideMainWindow = new JLabel("Hide the main window on start:");
        lblHideMainWindow.setEnabled(chbxSystemTray.isSelected() && chbxSystemTray.isEnabled());
        lblHideMainWindow.setBounds(6, 140, 386, 16);
        panelUI.add(lblHideMainWindow);

        JCheckBox chbxHideMainWindow = new JCheckBox("");
        chbxHideMainWindow.setEnabled(chbxSystemTray.isSelected() && chbxSystemTray.isEnabled());
        chbxHideMainWindow.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Config.instance().to().setProperty(Config.Entry.HIDE_MAIN_WINDOW.key(),
                        chbxHideMainWindow.isSelected());
            }
        });
        chbxHideMainWindow.setSelected(Config.instance().to().getBoolean(Config.Entry.HIDE_MAIN_WINDOW.key(), false));
        chbxHideMainWindow.setBounds(494, 133, 55, 23);
        panelUI.add(chbxHideMainWindow);

        JLabel labelTheme = new JLabel(
                "<html>Decorate the main window with theme:<br><small><i>- requires app restart</i></small></html>");
        labelTheme.setBounds(6, 170, 386, 29);
        panelUI.add(labelTheme);

        JCheckBox chbxTheme = new JCheckBox("");
        chbxTheme.setSelected(Config.instance().to().getBoolean(Config.Entry.WINDOW_THEME.key(), true));
        chbxTheme.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Config.instance().to().setProperty(Config.Entry.WINDOW_THEME.key(), chbxTheme.isSelected());
            }
        });
        chbxTheme.setEnabled(true);
        chbxTheme.setBounds(494, 168, 55, 23);
        panelUI.add(chbxTheme);

        panelGeneral.setLayout(null);

        chbxSystemTray.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Config.instance().to().setProperty(Config.Entry.SYSTEM_TRAY.key(), chbxSystemTray.isSelected());
                lblHideMainWindow.setEnabled(chbxSystemTray.isSelected());
                chbxHideMainWindow.setEnabled(chbxSystemTray.isSelected());
            }
        });

        JCheckBox chkbxAutostart = new JCheckBox();
        chkbxAutostart.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Config.instance().to().setProperty(Config.Entry.AUTOSTART.key(), chkbxAutostart.isSelected());
            }
        });

        JLabel lblAutostart = new JLabel("<html>Autostart measurements:</html>");
        lblAutostart.setBounds(6, 30, 386, 16);
        panelGeneral.add(lblAutostart);
        chkbxAutostart.setBounds(471, 24, 78, 29);
        chkbxAutostart.setSelected(Config.instance().to().getBoolean(Config.Entry.AUTOSTART.key(), true));
        panelGeneral.add(chkbxAutostart);

        textInterval = new JSpinner(new SpinnerNumberModel(
                Config.instance().to().getInt(Config.Entry.INTERVAL.key(), Constants.DEFAULT_INTERVAL),
                Constants.MIN_INTERVAL, Constants.MAX_INTERVAL, 1));
        textInterval.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int interval = (Integer) textInterval.getValue();
                if (verifyInterval(interval)) {
                    Config.instance().to().setProperty(Config.Entry.INTERVAL.key(), interval);
                }
            }
        });
        textInterval.addKeyListener(onlyDigitsKeyListener());

        JLabel lblInterval = new JLabel("<html>Measurements interval <i>(in seconds)</i>:</html>");
        lblInterval.setHorizontalAlignment(SwingConstants.LEFT);
        lblInterval.setBounds(6, 63, 363, 16);
        panelGeneral.add(lblInterval);
        textInterval.setBounds(471, 58, 78, 26);
        panelGeneral.add(textInterval);

        JLabel lblPM25MaxSafe = new JLabel("<html>PM 2.5 maximum concentration level considered <i>safe</i>:</html>");
        lblPM25MaxSafe.setHorizontalAlignment(SwingConstants.LEFT);
        lblPM25MaxSafe.setBounds(6, 99, 363, 16);
        panelGeneral.add(lblPM25MaxSafe);

        JSpinner textPM25MaxSafe = new JSpinner(new SpinnerNumberModel(
                Config.instance().to().getInt(Config.Entry.PM25_MAX_SAFE_LIMIT.key(), Constants.DEFAULT_PM25_MAX_SAFE),
                0, 1000, 1));
        textPM25MaxSafe.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int max25 = (Integer) textPM25MaxSafe.getValue();
                if (verifyMaxLimit(max25)) {
                    Config.instance().to().setProperty(Config.Entry.PM25_MAX_SAFE_LIMIT.key(), max25);
                }
            }
        });
        textPM25MaxSafe.addKeyListener(onlyDigitsKeyListener());
        textPM25MaxSafe.setBounds(471, 89, 78, 26);
        panelGeneral.add(textPM25MaxSafe);

        JLabel lblPM10MaxSafe = new JLabel("<html>PM 10 maximum concentration level considered <i>safe</i>:</html>");
        lblPM10MaxSafe.setHorizontalAlignment(SwingConstants.LEFT);
        lblPM10MaxSafe.setBounds(6, 127, 363, 16);
        panelGeneral.add(lblPM10MaxSafe);

        JSpinner textPM10MaxSafe = new JSpinner(new SpinnerNumberModel(
                Config.instance().to().getInt(Config.Entry.PM10_MAX_SAFE_LIMIT.key(), Constants.DEFAULT_PM10_MAX_SAFE),
                0, 1000, 1));
        textPM10MaxSafe.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int max10 = (Integer) textPM10MaxSafe.getValue();
                if (verifyMaxLimit(max10)) {
                    Config.instance().to().setProperty(Config.Entry.PM10_MAX_SAFE_LIMIT.key(), max10);
                }
            }
        });
        textPM10MaxSafe.addKeyListener(onlyDigitsKeyListener());
        textPM10MaxSafe.setBounds(471, 122, 78, 26);
        panelGeneral.add(textPM10MaxSafe);

        JLabel labelCheckVersion = new JLabel("<html>Automatically check for a new version on start:</html>");
        labelCheckVersion.setBounds(6, 164, 386, 16);
        panelGeneral.add(labelCheckVersion);

        JCheckBox chkbxCheckVersion = new JCheckBox();
        chkbxCheckVersion.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Config.instance().to().setProperty(Config.Entry.CHECK_LATEST_VERSION.key(),
                        chkbxCheckVersion.isSelected());
            }
        });
        chkbxCheckVersion.setSelected(Config.instance().to().getBoolean(Config.Entry.CHECK_LATEST_VERSION.key(), true));
        chkbxCheckVersion.setBounds(471, 160, 78, 29);
        panelGeneral.add(chkbxCheckVersion);

        JLabel lblCSVLog = new JLabel("<html>Log measurements to CSV file:</html>");
        lblCSVLog.setBounds(6, 199, 386, 37);
        panelGeneral.add(lblCSVLog);

        JButton btnChooseCSVFilePath = new JButton("...");

        JCheckBox chkbxCSVLog = new JCheckBox();
        chkbxCSVLog.setSelected(Config.instance().to().getBoolean(Config.Entry.CSV_LOG_ENABLED.key(), false));
        chkbxCSVLog.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                btnChooseCSVFilePath.setEnabled(chkbxCSVLog.isSelected());
                if (!chkbxCSVLog.isSelected()) {
                    Config.instance().to().setProperty(Config.Entry.CSV_LOG_ENABLED.key(), false);
                } else {
                    SwingUtilities.invokeLater(() -> btnChooseCSVFilePath.doClick());
                }
            }
        });
        chkbxCSVLog.setBounds(471, 199, 35, 29);
        panelGeneral.add(chkbxCSVLog);

        btnChooseCSVFilePath.setEnabled(chkbxCSVLog.isSelected());
        btnChooseCSVFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setDialogTitle("Please specify where to write CSV file...");
                fileChooser.setSelectedFile(new File("pm-home-station.csv"));
                fileChooser.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
                if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    Config.instance().to().setProperty(Config.Entry.CSV_LOG_ENABLED.key(), true);
                    Config.instance().to().setProperty(Config.Entry.CSV_LOG_FILE.key(), file.getAbsolutePath());
                } else {
                    chkbxCSVLog.setSelected(false);
                }
            }
        });
        btnChooseCSVFilePath.setToolTipText("Choose filepath to write meaurements in CSV format");
        btnChooseCSVFilePath.setBounds(508, 204, 28, 21);
        panelGeneral.add(btnChooseCSVFilePath);

        frame.getContentPane().setLayout(groupLayout);
        frame.toFront();
        frame.requestFocus();
        frame.setVisible(true); // blocks for modals...
        frame = null;
    }

    // OMG! all these boiler code just for user input verification which still is
    // crappy ... not copy-paste resilient
    // that's why I used to use SWT in the past.... 10 years later and Swing still
    // inherits its crappiness, hoping JavaFX is gonna be better
    private KeyListener onlyDigitsKeyListener() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() < '0' || e.getKeyChar() > '9') {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };
    }

    private boolean verifyInterval(int interval) {
        return interval >= Constants.MIN_INTERVAL && interval <= Constants.MAX_INTERVAL;
    }

    private boolean verifyMaxLimit(int val) {
        return val > 0 && val <= 1000;
    }

    private void addPreferredDevice(String deviceName) {
        Config.instance().to().addProperty(Config.Entry.PREFERRED_DEVICES.key(), deviceName);
    }

    private ListModel<String> getPreferredDevices() {
        DefaultListModel<String> result = new DefaultListModel<>();
        Set<String> ports = new HashSet<>(Config.instance().to().getList(String.class,
                Config.Entry.PREFERRED_DEVICES.key(), new ArrayList<String>()));
        for (String port : ports) {
            result.addElement(port);
        }
        return result;
    }

    private void savePreferredDevices(JList<String> list) {
        Config.instance().to().clearProperty(Config.Entry.PREFERRED_DEVICES.key());
        for (int i = 0; i < list.getModel().getSize(); i++) {
            Config.instance().to().addProperty(Config.Entry.PREFERRED_DEVICES.key(), list.getModel().getElementAt(i));
        }
    }

    private ListModel<String> getAvailableDevices() {
        DefaultListModel<String> result = new DefaultListModel<>();
        SerialUART serial = new SerialUART();
        Set<String> ports = serial.listPorts();
        for (String port : ports) {
            result.addElement(port);
        }
        return result;
    }

    private void swapInList(DefaultListModel<String> model, int a, int b) {
        String aObject = model.getElementAt(a);
        String bObject = model.getElementAt(b);
        model.set(a, bObject);
        model.set(b, aObject);
    }
}
