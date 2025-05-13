package org.tim.qmorph.viewer;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * 这个类支持导出Latex格式，要求在Latex文档头部引入epic和eepic两个包以支持图形绘制
 */
public class ExportToLaTeXOptionsDialog extends JDialog {
    boolean okState = false;
    JTextField xCorr, yCorr, unitlength;
    JCheckBox nodes;
    JButton ok, cancel;
    JLabel xCorrLabel, yCorrLabel, unitlengthLabel;
    JPanel unitContainer, corrContainer, nodesContainer, buttonContainer;

    public ExportToLaTeXOptionsDialog(JFrame f, String title, boolean modal) {
        super(f, title, modal);

        setLayout(new GridLayout(4, 1));

        unitContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        corrContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nodesContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));

        unitContainer.add(unitlengthLabel = new JLabel("Length of a unit i mm: "));
        unitContainer.add(unitlength = new JTextField("10", 4));

        corrContainer.add(xCorrLabel = new JLabel("Offset for x coordinates: "));
        corrContainer.add(xCorr = new JTextField("0", 4));
        corrContainer.add(yCorrLabel = new JLabel("Offset for y coordinates: "));
        corrContainer.add(yCorr = new JTextField("0", 4));

        nodesContainer.add(nodes = new JCheckBox("Visible nodes (diameter of each node is 0.1 units)", true));

        buttonContainer.add(ok = new JButton("OK"));
        buttonContainer.add(cancel = new JButton("Cancel"));

        ok.addActionListener(new ButtonActionListener());
        cancel.addActionListener(new ButtonActionListener());

        add(unitContainer);
        add(corrContainer);
        add(nodesContainer);
        add(buttonContainer);

        pack();
        setLocationRelativeTo(f);
    }

    public int getUnitlength() {
        return Integer.parseInt(unitlength.getText().trim());
    }

    public double getXCorr() {
        return Double.parseDouble(xCorr.getText().trim());
    }

    public double getYCorr() {
        return Double.parseDouble(yCorr.getText().trim());
    }

    public boolean getVisibleNodes() {
        return nodes.isSelected();
    }

    public boolean okPressed() {
        return okState;
    }

    class ButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("OK")) {
                okState = true;
            }
            dispose();
        }
    }

}
