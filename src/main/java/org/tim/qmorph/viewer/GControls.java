package org.tim.qmorph.viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.tim.qmorph.meshing.GeomBasics;

/**
 * The Panel class with step button, zoom menu, and axis and grid toggle buttons
 * etc.
 */
class GControls extends JPanel {
    GUI gui;
    GCanvas canvas;
    JLabel clickStatus;
    JComboBox<String> scaleCombo;
    JButton stepButton;
    JCheckBox showGridBox, showAxisBox;
    JLabel nodesLabel;

    /**
     * Constructor for the panel.
     *
     * @param gui  a pointer to the GUI instance
     * @param cvas a pointer to the Canvas instance
     */
    public GControls(GUI gui, GCanvas cvas) {
        this.gui = gui;
        this.canvas = cvas;

        setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        setBackground(new Color(45, 45, 48));
        setForeground(Color.WHITE);

        // 使用系统默认字体
        Font controlFont = new Font(Font.DIALOG, Font.PLAIN, 12);

        // 节点计数显示
        nodesLabel = new JLabel("剩余节点数量: ");
        nodesLabel.setFont(controlFont);
        nodesLabel.setForeground(Color.WHITE);
        add(nodesLabel);

        clickStatus = new JLabel("3");
        clickStatus.setFont(controlFont);
        clickStatus.setForeground(Color.WHITE);
        add(clickStatus);

        // 网格显示控制
        showGridBox = new JCheckBox("显示网格", true);
        showGridBox.setFont(controlFont);
        showGridBox.setForeground(Color.WHITE);
        showGridBox.setBackground(new Color(45, 45, 48));
        showGridBox.setOpaque(true);
        showGridBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                gui.grid = showGridBox.isSelected();
                canvas.repaint();
            }
        });
        add(showGridBox);

        // 坐标轴显示控制
        showAxisBox = new JCheckBox("显示坐标轴", true);
        showAxisBox.setFont(controlFont);
        showAxisBox.setForeground(Color.WHITE);
        showAxisBox.setBackground(new Color(45, 45, 48));
        showAxisBox.setOpaque(true);
        showAxisBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                gui.axis = showAxisBox.isSelected();
                canvas.repaint();
            }
        });
        add(showAxisBox);

        // 缩放控制
        JLabel viewLabel = new JLabel("视图:");
        viewLabel.setFont(controlFont);
        viewLabel.setForeground(Color.WHITE);
        add(viewLabel);

        String[] scales = { "25%", "50%", "75%", "100%", "150%", "200%", "300%", "400%" };
        scaleCombo = new JComboBox<>(scales);
        scaleCombo.setFont(controlFont);
        scaleCombo.setBackground(new Color(60, 60, 63));
        scaleCombo.setForeground(Color.WHITE);
        scaleCombo.setSelectedItem("100%");
        scaleCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String s = (String) scaleCombo.getSelectedItem();
                    s = s.substring(0, s.length() - 1);
                    canvas.setScale(Integer.parseInt(s));
                }
            }
        });
        // 设置下拉框的首选大小
        scaleCombo.setPreferredSize(new Dimension(80, scaleCombo.getPreferredSize().height));
        add(scaleCombo);

        // 步进按钮
        stepButton = new JButton("下一步");
        stepButton.setFont(controlFont);
        stepButton.setBackground(new Color(70, 70, 73));
        stepButton.setForeground(Color.WHITE);
        stepButton.setFocusPainted(false); // 移除焦点边框
        stepButton.setBorderPainted(false); // 移除边框
        stepButton.setOpaque(true);
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (gui.qm != null) {
                    gui.qm.step();
                } else if (gui.tri != null) {
                    gui.tri.step();
                }
                canvas.repaint(); // 自动刷新画布，显示最新网格
            }
        });
        add(stepButton);
    }
}
