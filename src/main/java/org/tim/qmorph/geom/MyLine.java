package org.tim.qmorph.geom;

import org.tim.qmorph.viewer.Msg;

/**
 * This class holds information for lines, and has methods for dealing with
 * line-related issues. The purpose of this class is solely to determine the
 * intersection point between two lines. The length of a line is, of course,
 * infinite.
 */

/**
 * 表示一条无限长的直线。
 * 该类主要用于计算两条直线的交点。
 * 
 * @author Tim
 */
public class MyLine {
    // n1: A point that the line passes through
    // n2: A point that the line passes through
    /**
     * 通过两个点构造一条直线。
     * 使用第一个点作为参考点，计算从第一个点到第二个点的向量作为直线的方向向量。
     *
     * @param n1 直线上的第一个点，作为参考点
     * @param n2 直线上的第二个点，用于确定方向向量
     */
    public MyLine(Node n1, Node n2) {
        ref = n1;
        x = n2.x - n1.x;
        y = n2.y - n1.y;
    }

    /**
     * 通过一个点和一个向量构造一条直线。
     * 使用给定的点作为参考点，向量作为直线的方向向量。
     *
     * @param n1 直线上的一个点，作为参考点
     * @param x  向量的x分量
     * @param y  向量的y分量
     */
    public MyLine(Node n1, double x, double y) {
        ref = n1;
        this.x = x;
        this.y = y;
    }

    public double cross(MyLine l) {
        return x * l.y - l.x * y;
    }

    /**
     * 判断当前直线与给定直线是否相交，并返回相交点。
     *
     * @param d1 要判断相交的直线对象
     * @return 如果相交，返回相交点的Node对象；如果不相交，返回null
     */
    public Node pointIntersectsAt(MyLine d1) {
        Msg.debug("Entering MyLine.pointIntersectsAt(..)");
        Node p0 = ref, p1 = d1.ref;
        MyLine delta = new MyLine(p0, p1.x - p0.x, p1.y - p0.y);
        MyLine d0 = this;
        Msg.debug("... d0:" + d0.descr());
        Msg.debug("... d1:" + d1.descr());
        double d0crossd1 = d0.cross(d1);

        if (d0crossd1 == 0) { // Parallel and, alas, no pointintersection
            Msg.debug("Leaving MyLine.pointIntersectsAt(..), returns null");
            return null;
        } else {
            double t = delta.cross(d0) / d0crossd1;

            double x = d1.ref.x + t * d1.x;
            double y = d1.ref.y + t * d1.y;
            Msg.debug("Leaving MyLine.pointIntersectsAt(..), returns x: " + x + ", y:" + y);
            return new Node(x, y); // Intersects at this line point
        }
    }

    public String descr() {
        return ref.descr() + ", (" + (x + ref.x) + ", " + (y + ref.y) + ")";
    }

    /** 直线上的参考点 */
    Node ref;

    /** 直线的x方向分量 */
    double x;

    /** 直线的y方向分量 */
    double y;
}
