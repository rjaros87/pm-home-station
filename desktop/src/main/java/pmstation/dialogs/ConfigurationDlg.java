/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pmstation.configuration.Config;
import pmstation.configuration.Constants;

public class ConfigurationDlg {

    private JFrame mainFrame;
    private String title;
    private JSpinner textInterval;
    
    public ConfigurationDlg(JFrame parent, String title) {
        this.mainFrame = parent;
        this.title = title;
    }
    
    /**
     * @wbp.parser.entryPoint
     */
    public void initGUI() {
        final JDialog frame = new JDialog(mainFrame, title, true);
        frame.setResizable(false);
        frame.setBounds(350, 350, 513, 397);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JButton btnClose = new JButton("OK");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(panel, GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                        .addComponent(btnClose, Alignment.TRAILING))
                    .addContainerGap())
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel, GroupLayout.PREFERRED_SIZE, 272, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClose))
        );
        panel.setLayout(null);
        
        JLabel lblInterval = new JLabel("Measurements interval (in seconds):");
        lblInterval.setHorizontalAlignment(SwingConstants.LEFT);
        lblInterval.setBounds(6, 71, 363, 16);
        panel.add(lblInterval);
        
        JCheckBox chkbxAutostart = new JCheckBox();
        chkbxAutostart.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Config.instance().to().setProperty(Config.Entry.AUTOSTART.key(), chkbxAutostart.isSelected());
            }
        });
        chkbxAutostart.setBounds(393, 24, 101, 29);
        panel.add(chkbxAutostart);
        
        JLabel lblAutostart = new JLabel("Autostart measurements (no recommended for OSX users):");
        lblAutostart.setBounds(6, 30, 386, 16);
        panel.add(lblAutostart);
        
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
        // OMG! all these boiler code just for user input verification which still is creepy ... not copy-paste resilient
        // that's why I used to use SWT in the past.... 10 years later and Swing still inherits its crappiness, hoping JavaFX is gonna be better
        textInterval.addKeyListener(new KeyListener() {
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
        });

        textInterval.setBounds(396, 64, 94, 26);
        panel.add(textInterval);
        frame.getContentPane().setLayout(groupLayout);
        frame.setVisible(true);
    }
    
    private boolean verifyInterval(int interval) {
        return interval >= Constants.MIN_INTERVAL && interval <= Constants.MAX_INTERVAL;
    }
}
