package org.tim.qmorph.viewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Font;

/** A class which opens a message dialog box. */
public class MsgDialog extends JDialog {
    JButton ok;
    JTextArea textArea;
    GridBagLayout gridbag;

    public MsgDialog(JFrame f, String title, String text, int x, int y) {
        super(f, title, true);

        gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipadx = 0;
        c.ipady = 0;
        c.fill = GridBagConstraints.NONE;

        textArea = new JTextArea(text, y, x);
        textArea.setEditable(false);
        textArea.setBackground(new Color(45, 45, 48));
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(scrollPane, c);

        ok = new JButton("确定");
        ok.setBackground(new Color(60, 60, 63));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setBorderPainted(false);
        ok.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(ok);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(ok, c);

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(f);
    }
}
