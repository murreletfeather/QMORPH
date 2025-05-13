package org.tim.qmorph.viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

/** A class which opens an "mesh metrics report" dialog window. */

public class ReportDialog extends JDialog implements ItemListener {
    JButton ok;
    JTextArea textArea;
    GridBagLayout gridbag;

    public ReportDialog(JFrame f, String text) {
        super(f, "Mesh Metrics Report", true);

        gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipadx = 0;
        c.ipady = 0;
        c.fill = GridBagConstraints.NONE;

        textArea = new JTextArea(text, 18, 80);
        textArea.setEditable(false);
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.yellow);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(scrollPane, c);

        ok = new JButton("OK");
        add(ok);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(ok, c);

        ok.addActionListener(new ButtonActionListener());
        pack();
        setLocationRelativeTo(f);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    }

    class ButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("OK")) {
                dispose();
            }
        }
    }
}
