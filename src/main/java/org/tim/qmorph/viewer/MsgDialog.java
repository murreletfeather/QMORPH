package org.tim.qmorph.viewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Font;

/**
 * 消息对话框类。
 * 用于弹出显示信息、警告或报告的对话框，支持多行文本和滚动。
 */
public class MsgDialog extends JDialog {
    /** 确定按钮 */
    JButton ok;
    /** 显示消息内容的文本区域 */
    JTextArea textArea;
    /** 布局管理器 */
    GridBagLayout gridbag;

    /**
     * 构造方法，创建消息对话框。
     * 
     * @param f     父窗口
     * @param title 对话框标题
     * @param text  显示的消息内容
     * @param x     文本区列数（宽度）
     * @param y     文本区行数（高度）
     */
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
