package org.tim.qmorph.viewer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.text.DecimalFormat;
import java.util.List;

import org.tim.qmorph.geom.Edge;
import org.tim.qmorph.geom.Node;
import org.tim.qmorph.meshing.GeomBasics;

/**
 * The Canvas class which paints the background grid, the nodes, the edges etc.
 */

class GCanvas extends Canvas {
    GUI gui;

    double xmin, ymin, xmax, ymax;
    int gridIncr;
    int scale;
    int width = 640, height = 480;
    private DecimalFormat df = new DecimalFormat("0.##"); // 保留两位小数

    int xaxis_yval;
    int yaxis_xval;

    public GCanvas(GUI gui, int scale) {
        this.gui = gui;
        this.scale = scale;

        // 设置默认的坐标范围
        xmin = -10.0;
        xmax = 10.0;
        ymin = -10.0;
        ymax = 10.0;

        // 获取父容器的大小
        if (getParent() != null) {
            width = getParent().getWidth();
            height = getParent().getHeight();
        }

        // 根据窗口大小计算合适的初始缩放比例
        double contentWidth = xmax - xmin;
        double contentHeight = ymax - ymin;
        double scaleX = (width * 0.85) / contentWidth;
        double scaleY = (height * 0.85) / contentHeight;
        this.scale = (int) Math.min(scaleX, scaleY);

        // 确保缩放比例在合理范围内
        this.scale = Math.max(10, Math.min(400, this.scale));

        // 调整坐标范围以适应窗口比例
        double aspectRatio = (double) width / height;
        if (aspectRatio > 1) {
            // 窗口更宽，调整x范围
            double newWidth = contentHeight * aspectRatio;
            double extension = (newWidth - contentWidth) / 2;
            xmin -= extension;
            xmax += extension;
        } else {
            // 窗口更高，调整y范围
            double newHeight = contentWidth / aspectRatio;
            double extension = (newHeight - contentHeight) / 2;
            ymin -= extension;
            ymax += extension;
        }

        // 调整网格密度
        gridIncr = (int) (this.scale / 1.0);

        setSize(width, height);

        double ymaxXscale = ymax * this.scale, xminXscale = xmin * this.scale;
        double rounded_ymaxXscale = signOf(ymax)
                * (Math.abs(ymaxXscale) + gridIncr - Math.IEEEremainder(Math.abs(ymaxXscale), gridIncr));
        double rounded_xminXscale = signOf(xmin)
                * (Math.abs(xminXscale) + gridIncr - Math.IEEEremainder(Math.abs(xminXscale), gridIncr));

        xaxis_yval = gridIncr + (int) (rounded_ymaxXscale);
        yaxis_xval = gridIncr + (int) (-rounded_xminXscale);

        // 更新GUI中的缩放比例显示
        updateScalePercentage();
    }

    public GCanvas(GUI gui, double xmin, double ymin, double xmax, double ymax, int scale) {
        this.gui = gui;
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;

        // 获取父容器的大小
        if (getParent() != null) {
            width = getParent().getWidth();
            height = getParent().getHeight();
        }

        // 根据窗口大小计算合适的初始缩放比例
        double contentWidth = xmax - xmin;
        double contentHeight = ymax - ymin;
        double scaleX = (width * 0.85) / contentWidth;
        double scaleY = (height * 0.85) / contentHeight;
        this.scale = (int) Math.min(scaleX, scaleY);

        // 确保缩放比例在合理范围内
        this.scale = Math.max(10, Math.min(400, this.scale));

        // 调整坐标范围以适应窗口比例
        double aspectRatio = (double) width / height;
        if (aspectRatio > 1) {
            // 窗口更宽，调整x范围
            double newWidth = contentHeight * aspectRatio;
            double extension = (newWidth - contentWidth) / 2;
            this.xmin -= extension;
            this.xmax += extension;
        } else {
            // 窗口更高，调整y范围
            double newHeight = contentWidth / aspectRatio;
            double extension = (newHeight - contentHeight) / 2;
            this.ymin -= extension;
            this.ymax += extension;
        }

        // 调整网格密度
        gridIncr = (int) (this.scale / 1.0);

        setSize(width, height);

        double ymaxXscale = ymax * this.scale, xminXscale = xmin * this.scale;
        double rounded_ymaxXscale = signOf(ymax)
                * (Math.abs(ymaxXscale) + gridIncr - Math.IEEEremainder(Math.abs(ymaxXscale), gridIncr));
        double rounded_xminXscale = signOf(xmin)
                * (Math.abs(xminXscale) + gridIncr - Math.IEEEremainder(Math.abs(xminXscale), gridIncr));

        xaxis_yval = gridIncr + (int) (rounded_ymaxXscale);
        yaxis_xval = gridIncr + (int) (-rounded_xminXscale);

        // 更新GUI中的缩放比例显示
        updateScalePercentage();
    }

    /** Returns the sign of the parameter. */
    double signOf(double val) {
        if (val < 0) {
            return -1;
        } else if (val == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public int getYAxisXPos() {
        return yaxis_xval;
    }

    public int getXAxisYPos() {
        return xaxis_yval;
    }

    public void setScale(int scale) {
        this.scale = scale;
        gridIncr = (int) (scale / 1.0);

        // 计算新的尺寸，考虑边距
        int w = (int) ((xmax - xmin) * scale * 1.1) + 2 * gridIncr; // 增加10%的边距
        int h = (int) ((ymax - ymin) * scale * 1.1) + 2 * gridIncr;

        // 确保尺寸至少和父容器一样大
        if (getParent() != null) {
            w = Math.max(w, getParent().getWidth());
            h = Math.max(h, getParent().getHeight());
        }

        width = w;
        height = h;

        setSize(width, height);

        double ymaxXscale = ymax * scale, xminXscale = xmin * scale;
        double rounded_ymaxXscale = signOf(ymax)
                * (Math.abs(ymaxXscale) + gridIncr - Math.IEEEremainder(Math.abs(ymaxXscale), gridIncr));
        double rounded_xminXscale = signOf(xmin)
                * (Math.abs(xminXscale) + gridIncr - Math.IEEEremainder(Math.abs(xminXscale), gridIncr));

        xaxis_yval = gridIncr + (int) (rounded_ymaxXscale);
        yaxis_xval = gridIncr + (int) (-rounded_xminXscale);

        // 更新GUI中的缩放比例显示
        updateScalePercentage();

        repaint();
    }

    public void resize(double xmin, double ymin, double xmax, double ymax, int scale) {
        this.scale = scale;
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;

        setScale(scale);
    }

    public void clear() {
        repaint();
    }

    // Method for drawing everything
    @Override
    public void paint(Graphics g) {
        Edge e;
        Node n;
        List<Node> nodeList = GeomBasics.getNodeList();
        List<Edge> edgeList = GeomBasics.getEdgeList();
        int halfGridIncr = gridIncr / 2;

        // 使用Graphics2D获得更好的渲染效果
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.clearRect(0, 0, getWidth(), getHeight());

        // Draw background grid
        if (gui.grid) {
            // 使用半透明的深色网格
            g2d.setColor(new Color(80, 80, 80, 160));
            g2d.setStroke(new BasicStroke(0.8f));

            // 绘制网格线
            for (int i = 0; i < getWidth(); i += gridIncr) {
                g2d.drawLine(i, 0, i, getHeight());
            }
            for (int i = 0; i < getHeight(); i += gridIncr) {
                g2d.drawLine(0, i, getWidth(), i);
            }
        }

        // Draw axis
        if (gui.axis) {
            // 坐标轴使用纯白色，稍微加粗
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(0, xaxis_yval, getWidth(), xaxis_yval);
            g2d.drawLine(yaxis_xval, 0, yaxis_xval, getHeight());

            // 使用优雅的字体
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));

            // X轴刻度
            for (int i = yaxis_xval; i < getWidth(); i += gridIncr * 5) {
                g2d.drawLine(i, xaxis_yval + halfGridIncr / 2, i, xaxis_yval - halfGridIncr / 2);
                double value = ((double) (i - yaxis_xval)) / scale;
                String label = df.format(value);

                // 计算文本宽度以居中显示
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2d.drawString(label, i - labelWidth / 2, xaxis_yval + gridIncr / 2 + 12);
            }
            for (int i = yaxis_xval - gridIncr * 5; i >= 0; i -= gridIncr * 5) {
                g2d.drawLine(i, xaxis_yval + halfGridIncr / 2, i, xaxis_yval - halfGridIncr / 2);
                double value = ((double) (i - yaxis_xval)) / scale;
                String label = df.format(value);

                // 计算文本宽度以居中显示
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2d.drawString(label, i - labelWidth / 2, xaxis_yval + gridIncr / 2 + 12);
            }

            // Y轴刻度
            for (int i = xaxis_yval + gridIncr * 5; i < getHeight(); i += gridIncr * 5) {
                g2d.drawLine(yaxis_xval - halfGridIncr / 2, i, yaxis_xval + halfGridIncr / 2, i);
                double value = ((double) (xaxis_yval - i)) / scale;
                String label = df.format(value);

                // 计算文本宽度以对齐显示
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2d.drawString(label, yaxis_xval - labelWidth - 8, i + 4);
            }
            for (int i = xaxis_yval - gridIncr * 5; i >= 0; i -= gridIncr * 5) {
                g2d.drawLine(yaxis_xval - halfGridIncr / 2, i, yaxis_xval + halfGridIncr / 2, i);
                double value = ((double) (xaxis_yval - i)) / scale;
                String label = df.format(value);

                // 计算文本宽度以对齐显示
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2d.drawString(label, yaxis_xval - labelWidth - 8, i + 4);
            }
        }

        // Draw mesh with anti-aliasing
        if (edgeList != null) {
            g2d.setStroke(new BasicStroke(1.2f));
            for (int i = 0; i < edgeList.size(); i++) {
                e = edgeList.get(i);

                if (e.color == java.awt.Color.red) {
                    g2d.setColor(e.color);
                } else if (e.leftNode.edgeList.indexOf(e) == -1 && e.rightNode.edgeList.indexOf(e) == -1) {
                    g2d.setColor(Color.blue);
                } else if (e.frontEdge) {
                    g2d.setColor(Color.yellow);
                } else {
                    g2d.setColor(e.color);
                }

                g2d.drawLine((int) (e.leftNode.x * scale + yaxis_xval), (int) (-e.leftNode.y * scale + xaxis_yval),
                        (int) (e.rightNode.x * scale + yaxis_xval),
                        (int) (-e.rightNode.y * scale + xaxis_yval));
            }
        }

        if (nodeList != null) {
            for (Object element : nodeList) {
                n = (Node) element;
                g2d.setColor(n.color);
                int nodeSize = 6;
                g2d.fillOval((int) (n.x * scale + yaxis_xval - nodeSize / 2),
                        (int) (-n.y * scale + xaxis_yval - nodeSize / 2),
                        nodeSize, nodeSize);
            }
        }
    }

    public void autoFit() {
        // 获取当前窗口大小
        if (getParent() != null) {
            width = getParent().getWidth();
            height = getParent().getHeight();
        }

        // 设置新的尺寸
        setSize(width, height);

        // 根据窗口大小计算合适的缩放比例
        double contentWidth = xmax - xmin;
        double contentHeight = ymax - ymin;
        double scaleX = (width * 0.85) / contentWidth;
        double scaleY = (height * 0.85) / contentHeight;
        int newScale = (int) Math.min(scaleX, scaleY);

        // 确保缩放比例在合理范围内
        newScale = Math.max(10, Math.min(400, newScale));

        // 调整坐标范围以适应窗口比例
        double aspectRatio = (double) width / height;
        if (aspectRatio > 1) {
            // 窗口更宽，调整x范围
            double newWidth = contentHeight * aspectRatio;
            double extension = (newWidth - contentWidth) / 2;
            xmin = -2.0 - extension;
            xmax = 2.0 + extension;
        } else {
            // 窗口更高，调整y范围
            double newHeight = contentWidth / aspectRatio;
            double extension = (newHeight - contentHeight) / 2;
            ymin = -2.0 - extension;
            ymax = 2.0 + extension;
        }

        // 更新缩放比例
        setScale(newScale);

        // 重新计算网格增量
        gridIncr = (int) (newScale / 1.0);

        // 更新坐标轴位置
        double ymaxXscale = ymax * newScale;
        double xminXscale = xmin * newScale;
        double rounded_ymaxXscale = signOf(ymax)
                * (Math.abs(ymaxXscale) + gridIncr - Math.IEEEremainder(Math.abs(ymaxXscale), gridIncr));
        double rounded_xminXscale = signOf(xmin)
                * (Math.abs(xminXscale) + gridIncr - Math.IEEEremainder(Math.abs(xminXscale), gridIncr));

        xaxis_yval = gridIncr + (int) (rounded_ymaxXscale);
        yaxis_xval = gridIncr + (int) (-rounded_xminXscale);

        // 更新GUI中的缩放比例显示
        updateScalePercentage();

        repaint();
    }

    // 添加更新缩放百分比的方法
    private void updateScalePercentage() {
        if (gui != null && gui.gctrls != null) {
            // 计算缩放百分比
            int percentage = (int) ((scale / 100.0) * 100);
            gui.gctrls.scaleCombo.setSelectedItem(percentage + "%");
        }
    }

}
