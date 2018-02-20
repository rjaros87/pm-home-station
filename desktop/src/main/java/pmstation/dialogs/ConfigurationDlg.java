/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.dialogs;

import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Config;
import pmstation.configuration.Constants;

public class ConfigurationDlg {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDlg.class);
    
    private JFrame mainFrame;
    private String title;
    private JSpinner textInterval;
    private JDialog frame = null;
    
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
        frame.setBounds(350, 350, 515, 426);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(mainFrame);
        Container mainCfg = frame.getContentPane();
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainCfg.add(tabbedPane);
        
        JPanel panelGeneral = new JPanel();
        panelGeneral.setBorder(new TitledBorder(null, "<html><b>General</b></html>", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JPanel panelUI = new JPanel();
        panelUI.setBorder(new TitledBorder(null, "<html><b>User Interface</b></html>", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JPanel panelBottom = new JPanel();
        panelBottom.setBorder(null);
        
        tabbedPane.addTab("General", panelGeneral);
        tabbedPane.addTab("User Interface", panelUI);

        GroupLayout groupLayout = new GroupLayout(mainCfg);
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(tabbedPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                        .addComponent(panelBottom, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE))
                    .addContainerGap())
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 322, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(panelBottom, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        
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
        chbxAlwaysOnTop.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Config.instance().to().setProperty(Config.Entry.ALWAYS_ON_TOP.key(), chbxAlwaysOnTop.isSelected());
                mainFrame.setAlwaysOnTop(chbxAlwaysOnTop.isSelected());
            }
        });
        chbxAlwaysOnTop.setSelected(Config.instance().to().getBoolean(Config.Entry.ALWAYS_ON_TOP.key(), false));
        chbxAlwaysOnTop.setBounds(393, 24, 76, 29);
        
        panelUI.add(chbxAlwaysOnTop);
        
        JLabel lblSystemTray = new JLabel("<html>Show app icon in the System Tray:<br><small><i>- only on selected operating systems<br>- requires app restart</i></html>");
        lblSystemTray.setBounds(6, 66, 386, 60);
        lblSystemTray.setEnabled(SystemTray.isSupported());
        panelUI.add(lblSystemTray);
        
        JCheckBox chbxSystemTray = new JCheckBox("");

        chbxSystemTray.setSelected(!SystemTray.isSupported() || Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), false));
        chbxSystemTray.setBounds(393, 74, 76, 23);
        chbxSystemTray.setEnabled(SystemTray.isSupported());
        panelUI.add(chbxSystemTray);
        
        JLabel lblHideMainWindow = new JLabel("Hide the main window on start:");
        lblHideMainWindow.setEnabled(chbxSystemTray.isSelected() && chbxSystemTray.isEnabled());
        lblHideMainWindow.setBounds(6, 140, 386, 16);
        panelUI.add(lblHideMainWindow);
        
        JCheckBox chbxHideMainWindow = new JCheckBox("");
        chbxHideMainWindow.setEnabled(chbxSystemTray.isSelected() && chbxSystemTray.isEnabled());
        chbxHideMainWindow.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Config.instance().to().setProperty(Config.Entry.HIDE_MAIN_WINDOW.key(), chbxHideMainWindow.isSelected());
            }
        });
        chbxHideMainWindow.setSelected(Config.instance().to().getBoolean(Config.Entry.HIDE_MAIN_WINDOW.key(), false));
        chbxHideMainWindow.setBounds(393, 133, 76, 23);
        panelUI.add(chbxHideMainWindow);

        panelGeneral.setLayout(null);

        chbxSystemTray.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Config.instance().to().setProperty(Config.Entry.SYSTEM_TRAY.key(), chbxSystemTray.isSelected());
                lblHideMainWindow.setEnabled(chbxSystemTray.isSelected());
                chbxHideMainWindow.setEnabled(chbxSystemTray.isSelected());
            }
        });
        
        JLabel lblInterval = new JLabel("<html>Measurements interval <i>(in seconds)</i>:</html>");
        lblInterval.setHorizontalAlignment(SwingConstants.LEFT);
        lblInterval.setBounds(6, 98, 363, 16);
        panelGeneral.add(lblInterval);
        
        JCheckBox chkbxAutostart = new JCheckBox();
        chkbxAutostart.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Config.instance().to().setProperty(Config.Entry.AUTOSTART.key(), chkbxAutostart.isSelected());
            }
        });
        chkbxAutostart.setBounds(393, 24, 76, 29);
        chkbxAutostart.setSelected(Config.instance().to().getBoolean(Config.Entry.AUTOSTART.key(), true));
        panelGeneral.add(chkbxAutostart);
        
        JLabel lblAutostart = new JLabel("<html>Autostart measurements:</html>");
        lblAutostart.setBounds(6, 30, 386, 16);
        panelGeneral.add(lblAutostart);
        
        textInterval = new JSpinner(new SpinnerNumberModel(
                Config.instance().to().getInt(Config.Entry.INTERVAL.key(), Constants.DEFAULT_INTERVAL),
                Constants.MIN_INTERVAL, Constants.MAX_INTERVAL, 1));
        textInterval.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int interval = (Integer)textInterval.getValue();
                if (verifyInterval(interval)) {
                    Config.instance().to().setProperty(Config.Entry.INTERVAL.key(), interval);
                }
            }
        });
        textInterval.addKeyListener(onlyDigitsKeyListener());
        textInterval.setBounds(391, 93, 78, 26);
        panelGeneral.add(textInterval);

        JLabel lblWarnUserOSX = new JLabel("<html>Warn before close / disconnect if device is not detached:<br><small><i>- a workaround for macOS and faulty PL2303 drivers</i></small></html>");
        lblWarnUserOSX.setEnabled(SystemUtils.IS_OS_MAC_OSX);
        lblWarnUserOSX.setBounds(6, 50, 363, 45);
        panelGeneral.add(lblWarnUserOSX);
        
        JCheckBox chbxWarnOSX = new JCheckBox("");
        chbxWarnOSX.setEnabled(SystemUtils.IS_OS_MAC_OSX);
        chbxWarnOSX.setSelected(!SystemUtils.IS_OS_MAC_OSX || Config.instance().to().getBoolean(Config.Entry.WARN_ON_OSX_TO_DETACH.key(), SystemUtils.IS_OS_MAC_OSX));
        chbxWarnOSX.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Config.instance().to().setProperty(Config.Entry.WARN_ON_OSX_TO_DETACH.key(), chbxWarnOSX.isSelected());
            }
        });
        chbxWarnOSX.setBounds(393, 55, 76, 23);
        panelGeneral.add(chbxWarnOSX);
        
        JLabel lblPM25MaxSafe = new JLabel("<html>PM 2.5 maximum allowed concentration level:</html>");
        lblPM25MaxSafe.setHorizontalAlignment(SwingConstants.LEFT);
        lblPM25MaxSafe.setBounds(6, 134, 363, 16);
        panelGeneral.add(lblPM25MaxSafe);
        
        JLabel lblPM10MaxSafe = new JLabel("<html>PM 10 maximum allowed concentration level:</html>");
        lblPM10MaxSafe.setHorizontalAlignment(SwingConstants.LEFT);
        lblPM10MaxSafe.setBounds(6, 162, 363, 16);
        panelGeneral.add(lblPM10MaxSafe);
        
        JSpinner textPM25MaxSafe = new JSpinner(new SpinnerNumberModel(
                Config.instance().to().getInt(Config.Entry.PM25_MAX_SAFE_LIMIT.key(), Constants.DEFAULT_PM25_MAX_SAFE),
                0, 1000, 1));
        textPM25MaxSafe.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int max25 = (Integer)textPM25MaxSafe.getValue();
                if (verifyMaxLimit(max25)) {
                    Config.instance().to().setProperty(Config.Entry.PM25_MAX_SAFE_LIMIT.key(), max25);
                }
            }
        });
        textPM25MaxSafe.addKeyListener(onlyDigitsKeyListener());
        textPM25MaxSafe.setBounds(391, 129, 78, 26);
        panelGeneral.add(textPM25MaxSafe);
        
        JSpinner textPM10MaxSafe = new JSpinner(new SpinnerNumberModel(
                Config.instance().to().getInt(Config.Entry.PM10_MAX_SAFE_LIMIT.key(), Constants.DEFAULT_PM10_MAX_SAFE),
                0, 1000, 1));
        textPM10MaxSafe.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int max10 = (Integer)textPM10MaxSafe.getValue();
                if (verifyMaxLimit(max10)) {
                    Config.instance().to().setProperty(Config.Entry.PM10_MAX_SAFE_LIMIT.key(), max10);
                }
            }
        });
        textPM10MaxSafe.addKeyListener(onlyDigitsKeyListener());
        textPM10MaxSafe.setBounds(391, 157, 78, 26);
        panelGeneral.add(textPM10MaxSafe);

        frame.getContentPane().setLayout(groupLayout);
        frame.toFront();
        frame.requestFocus();
        frame.setVisible(true); // blocks for modals...
        frame = null;
    }

    // OMG! all these boiler code just for user input verification which still is creepy ... not copy-paste resilient
    // that's why I used to use SWT in the past.... 10 years later and Swing still inherits its crappiness, hoping JavaFX is gonna be better
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
}
