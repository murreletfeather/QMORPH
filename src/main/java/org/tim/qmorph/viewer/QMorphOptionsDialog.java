package org.tim.qmorph.viewer;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.tim.qmorph.meshing.Constants;

import javax.swing.*;

/**
 * QMorph参数设置对话框类。
 * 用于为QMorph算法提供参数输入界面，支持三角转四边形、拓扑清理、全局平滑等参数配置。
 */
public class QMorphOptionsDialog extends JDialog implements ItemListener {
    /** 运行状态标志 */
    boolean runState = false;
    /** 各参数输入框 */
    JTextField epsilon1, epsilon2;
    JTextField chevronMin;
    JTextField coinctol, movetolerance, obstol, deltafactor, mymin, thetamax, tol, gamma, maxiter;

    /** 各参数标签 */
    JLabel epsilon1Label, epsilon2Label;
    JLabel chevronMinLabel;
    JLabel coinctolLabel, movetoleranceLabel, obstolLabel, deltafactorLabel, myminLabel, thetamaxLabel, tolLabel,
            gammaLabel, maxiterLabel;

    /** 按钮 */
    JButton run, defaults, cancel;

    /** 各参数分组面板 */
    JPanel seamContainer, topoContainer, smoothContainer, buttonContainer;
    /** 算法选项复选框 */
    JCheckBox tri2quadBox, topoBox, smoothBox;

    /** 布局管理器 */
    GridBagLayout gridbag;

    /**
     * 构造方法，初始化参数对话框。
     * 
     * @param f 父窗口
     */
    public QMorphOptionsDialog(JFrame f) {
        super(f, "Parameters for QMorph", true);

        gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipadx = 0;
        c.ipady = 0;
        c.insets = new Insets(0, 0, 0, 0);

        seamContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        smoothContainer = new JPanel(new GridLayout(3, 3));
        topoContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));

        seamContainer.add(epsilon1Label = new JLabel("Epsilon1= e1 * PI, e1= "));
        seamContainer.add(epsilon1 = new JTextField("", 6));
        seamContainer.add(epsilon2Label = new JLabel("Epsilon2= e2 * PI, e2= "));
        seamContainer.add(epsilon2 = new JTextField("", 6));

        topoContainer.add(chevronMinLabel = new JLabel("Minimum size of greatest angle in a chevron"));
        topoContainer.add(chevronMin = new JTextField("", 6));

        smoothContainer.add(coinctolLabel = new JLabel("COINCTOL"));
        smoothContainer.add(coinctol = new JTextField("", 6));
        smoothContainer.add(movetoleranceLabel = new JLabel("MOVETOLERANCE"));
        smoothContainer.add(movetolerance = new JTextField("", 6));
        smoothContainer.add(obstolLabel = new JLabel("OBSTOL"));
        smoothContainer.add(obstol = new JTextField("", 6));
        smoothContainer.add(deltafactorLabel = new JLabel("DELTAFACTOR"));
        smoothContainer.add(deltafactor = new JTextField("", 6));
        smoothContainer.add(myminLabel = new JLabel("MYMIN"));
        smoothContainer.add(mymin = new JTextField("", 6));
        smoothContainer.add(thetamaxLabel = new JLabel("THETAMAX"));
        smoothContainer.add(thetamax = new JTextField("", 6));
        smoothContainer.add(tolLabel = new JLabel("TOL"));
        smoothContainer.add(tol = new JTextField("", 6));
        smoothContainer.add(gammaLabel = new JLabel("GAMMA"));
        smoothContainer.add(gamma = new JTextField("", 6));
        smoothContainer.add(maxiterLabel = new JLabel("MAXITER"));
        smoothContainer.add(maxiter = new JTextField("", 6));

        buttonContainer.add(run = new JButton("Run"));
        buttonContainer.add(defaults = new JButton("Set defaults"));
        defaults.setActionCommand("Defaults");
        buttonContainer.add(cancel = new JButton("Cancel"));

        run.addActionListener(new ButtonActionListener());
        defaults.addActionListener(new ButtonActionListener());
        cancel.addActionListener(new ButtonActionListener());

        setDefaults();

        tri2quadBox = new JCheckBox("Triangle to quad conversion", true);
        tri2quadBox.addItemListener(this);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.NONE;
        add(tri2quadBox);
        gridbag.setConstraints(tri2quadBox, c);

        add(seamContainer);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(seamContainer, c);

        topoBox = new JCheckBox("Topological clean-up", true);
        topoBox.addItemListener(this);
        add(topoBox);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(topoBox, c);

        add(topoContainer);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(topoContainer, c);

        smoothBox = new JCheckBox("Global smoothing", true);
        smoothBox.addItemListener(this);
        add(smoothBox);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(smoothBox, c);

        add(smoothContainer);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(smoothContainer, c);

        add(buttonContainer);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(buttonContainer, c);

        pack();
        setLocationRelativeTo(f);
    }

    /**
     * 处理复选框状态变化，启用/禁用相关参数输入框。
     * 
     * @param e 事件对象
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof JCheckBox) {
            JCheckBox box = (JCheckBox) e.getSource();
            if (box == tri2quadBox) {
                if (tri2quadBox.isSelected()) {
                    epsilon1.setEnabled(true);
                    epsilon2.setEnabled(true);
                } else {
                    epsilon1.setEnabled(false);
                    epsilon2.setEnabled(false);
                }
            } else if (box == topoBox) {
                if (topoBox.isSelected()) {
                    chevronMin.setEnabled(true);
                } else {
                    chevronMin.setEnabled(false);
                }
            } else if (box == smoothBox) {
                if (smoothBox.isSelected()) {
                    coinctol.setEnabled(true);
                    movetolerance.setEnabled(true);
                    obstol.setEnabled(true);
                    deltafactor.setEnabled(true);
                    mymin.setEnabled(true);
                    thetamax.setEnabled(true);
                    tol.setEnabled(true);
                    gamma.setEnabled(true);
                    maxiter.setEnabled(true);
                } else {
                    coinctol.setEnabled(false);
                    movetolerance.setEnabled(false);
                    obstol.setEnabled(false);
                    deltafactor.setEnabled(false);
                    mymin.setEnabled(false);
                    thetamax.setEnabled(false);
                    tol.setEnabled(false);
                    gamma.setEnabled(false);
                    maxiter.setEnabled(false);
                }
            }
            repaint();
        }
    }

    /**
     * 设置参数为默认值。
     */
    public void setDefaults() {
        double tmp;
        epsilon1.setText(Double.toString(Constants.defaultE1Factor));
        epsilon2.setText(Double.toString(Constants.defaultE2Factor));
        tmp = Math.toDegrees(Constants.defaultCHEVRONMIN);
        chevronMin.setText(Double.toString(tmp));
        coinctol.setText(Double.toString(Constants.defaultCOINCTOL));
        movetolerance.setText(Double.toString(Constants.defaultMOVETOLERANCE));
        obstol.setText(Double.toString(Constants.defaultOBSTOL));
        deltafactor.setText(Double.toString(Constants.defaultDELTAFACTOR));
        mymin.setText(Double.toString(Constants.defaultMYMIN));
        tmp = Math.toDegrees(Constants.defaultTHETAMAX);
        thetamax.setText(Double.toString(tmp));
        tol.setText(Double.toString(Constants.defaultTOL));
        gamma.setText(Double.toString(Constants.defaultGAMMA));
        maxiter.setText(Integer.toString(Constants.defaultMAXITER));
        repaint();
    }

    /**
     * 将界面参数同步到全局常量。
     */
    public void copyToProgramParameters() {
        double tmp;
        Constants.EPSILON1 = java.lang.Math.PI * Double.parseDouble(epsilon1.getText().trim());
        Constants.EPSILON2 = java.lang.Math.PI * Double.parseDouble(epsilon2.getText().trim());
        tmp = Double.parseDouble(chevronMin.getText().trim());
        Constants.CHEVRONMIN = Math.toRadians(tmp);
        Constants.COINCTOL = Double.parseDouble(coinctol.getText().trim());
        Constants.MOVETOLERANCE = Double.parseDouble(movetolerance.getText().trim());
        Constants.OBSTOL = Double.parseDouble(obstol.getText().trim());
        Constants.DELTAFACTOR = Double.parseDouble(deltafactor.getText().trim());
        Constants.MYMIN = Double.parseDouble(mymin.getText().trim());
        tmp = Double.parseDouble(thetamax.getText().trim());
        Constants.THETAMAX = Math.toRadians(tmp);
        Constants.TOL = Double.parseDouble(tol.getText().trim());
        Constants.GAMMA = Double.parseDouble(gamma.getText().trim());
        Constants.MAXITER = Integer.parseInt(maxiter.getText().trim());

        Constants.doTri2QuadConversion = tri2quadBox.isSelected();
        Constants.doCleanUp = topoBox.isSelected();
        Constants.doSmooth = smoothBox.isSelected();
    }

    /**
     * 判断"Run"按钮是否被按下。
     * 
     * @return 是否按下
     */
    public boolean runPressed() {
        return runState;
    }

    /**
     * 按钮事件监听器。
     */
    class ButtonActionListener implements ActionListener {
        /**
         * 处理按钮点击事件。
         * 
         * @param e 事件对象
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Run")) {
                runState = true;
                dispose();
            } else if (command.equals("Defaults")) {
                setDefaults();
            } else if (command.equals("Cancel")) {
                dispose();
            }
        }
    }

}
