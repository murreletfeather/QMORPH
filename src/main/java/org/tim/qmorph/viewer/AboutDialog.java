package org.tim.qmorph.viewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Font;

/** A class which opens an "about" dialog window. */
public class AboutDialog extends JDialog {
    JButton ok;
    JTextArea textArea;
    GridBagLayout gridbag;
    String text = "网格编辑器 v1.0\n\n" +
            "一个用于创建和编辑二维网格的工具。\n" +
            "支持三角形和四边形网格的生成和编辑。\n\n" +
            "作者：Tim\n" +
            "日期：2025\n";

    public AboutDialog(JFrame f) {
        super(f, "关于网格编辑器", true);

        gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipadx = 0;
        c.ipady = 0;
        c.fill = GridBagConstraints.NONE;

        textArea = new JTextArea(text, 18, 40);
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
