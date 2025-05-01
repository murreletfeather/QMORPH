package org.tim.qmorph.viewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Font;

/** A class which opens a "help" dialog window. */
public class HelpDialog extends JDialog {
    JButton ok;
    JTextArea textArea;
    GridBagLayout gridbag;
    String text = "网格编辑器使用帮助\n\n" +
            "基本操作：\n" +
            "- 左键点击：选择或创建节点\n" +
            "- 右键拖动：平移视图\n" +
            "- 鼠标滚轮：缩放视图\n\n" +
            "显示控制：\n" +
            "- 显示网格：显示或隐藏背景网格\n" +
            "- 显示坐标轴：显示或隐藏坐标轴\n" +
            "- 视图：选择缩放比例\n\n" +
            "网格操作：\n" +
            "- 下一步：执行下一个网格生成步骤\n" +
            "- 自动适应：自动调整视图以适应当前网格\n\n" +
            "快捷键：\n" +
            "- Ctrl+N：新建\n" +
            "- Ctrl+O：打开\n" +
            "- Ctrl+S：保存\n" +
            "- Ctrl+Q：退出\n";

    public HelpDialog(JFrame f) {
        super(f, "网格编辑器帮助", true);

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
