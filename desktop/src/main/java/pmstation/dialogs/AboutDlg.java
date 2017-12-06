/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */
package pmstation.dialogs;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.Station;

public class AboutDlg {

    private static final Logger logger = LoggerFactory.getLogger(AboutDlg.class);
    
    private JFrame mainFrame;
    private String title;
    private JDialog frame = null;
    
    public AboutDlg(JFrame parent, String title) {
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
        frame = new JDialog(mainFrame, title, true);
        frame.setResizable(false);
        frame.setBounds(350, 350, 515, 386);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(mainFrame);
        
        JTabbedPane tabs = new JTabbedPane();
        
        JPanel panelAbout = new JPanel();
        loadHtml(panelAbout, Station.class.getResource("/pmstation/html/about.html"),
                             Station.class.getResource("/pmstation/html/about.css"));
        JPanel panelLicence = new JPanel();
        loadHtml(panelLicence, Station.class.getResource("/pmstation/html/licenses.html"),
                               Station.class.getResource("/pmstation/html/licenses.css"));
        
        JPanel panelBottom = new JPanel();
        panelBottom.setBorder(null);
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(tabs, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                        .addComponent(panelBottom, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE))
                    .addContainerGap())
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(tabs, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
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
        
        tabs.addTab("About", null, panelAbout, null);
        tabs.addTab("License", null, panelLicence, null);

        frame.getContentPane().setLayout(groupLayout);
        frame.toFront();
        frame.requestFocus();
        frame.setVisible(true); // blocks for modals
        frame = null;
    }
    
    private void loadHtml(JPanel panel, URL html, URL stylesheet) {
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        final JScrollPane scrollPane = new JScrollPane(jEditorPane);
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        if (stylesheet != null) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(stylesheet.openStream()));
                kit.getStyleSheet().loadRules(in, null);
            } catch (Exception e) {
                logger.error("Error reading stylesheets: {}", stylesheet, e);
                IOUtils.closeQuietly(in);
            }
        }
        jEditorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent ev) {
                if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(ev.getURL().toURI());
                    } catch (Exception e) {
                        logger.warn("Error opening the link from html", e);
                    }
                }
            }
            
        });
        
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
        
        InputStream htmlStream = null;
        try { 
            htmlStream = html.openStream();
            jEditorPane.setText(IOUtils.toString(htmlStream, "UTF-8"));
            IOUtils.closeQuietly(htmlStream);
        } catch (Exception e) {
            logger.error("Error reading html content: {}", html, e);
            IOUtils.closeQuietly(htmlStream);
        }
        panel.setLayout(new BorderLayout(0, 0));
        panel.add(scrollPane);
        
        // ensure html is displayed from the beginning...
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
                scrollPane.getVerticalScrollBar().setValue(0);
            }
         });
    }

}
