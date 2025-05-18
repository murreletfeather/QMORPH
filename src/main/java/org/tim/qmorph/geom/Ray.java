package org.tim.qmorph.geom;

import org.tim.qmorph.viewer.Msg;

/**
 * This class holds information for rays, and has methods for dealing with
 * ray-related issues. The purpose of this class is solely to determine the
 * intersection point between a ray (origin and direction) and a vector (origin
 * and x,y giving the direction, the length of the ray is considered to be
 * infinite).
 */
/**
 * 表示一条射线。
 * 该类主要用于计算射线与向量的交点。
 * 射线由起点(origin)和方向向量(x,y)组成。
 * 射线的长度被认为是无限的。
 * 
 * @author Tim
 */
public class Ray {

    /**
     * 通过一个点、一条参考边和一个角度构造一条射线。
     * 射线的方向由参考边在给定点的角度加上指定的角度确定。
     *
     * @param origin  射线的起点
     * @param relEdge 参考边，用于确定射线的基准方向
     * @param angle   相对于参考边的角度（弧度）
     */
    public Ray(Node origin, Edge relEdge, double angle) {
        this.origin = origin;
        double temp = relEdge.angleAt(origin);
        Msg.debug("relEdge.angleAt(origin)==" + Math.toDegrees(temp) + " degrees");
        double ang = temp + angle;
        Msg.debug("Ray(..): relEdge.angleAt(origin)+ angle== " + Math.toDegrees(ang) + " degrees");

        this.x = Math.cos(ang);
        this.y = Math.sin(ang);
    }

    /**
     * 通过一个点和角度构造一条射线。
     * 射线的方向由给定的角度确定，角度是相对于x轴的角度（弧度）。
     *
     * @param origin 射线的起点
     * @param angle  射线相对于x轴的角度（弧度）
     */
    public Ray(Node origin, double angle) {
        this.origin = origin;
        this.x = Math.cos(angle);
        this.y = Math.sin(angle);
    }

    /**
     * 通过一个起点和一个经过点构造一条射线。
     * 射线的方向由从起点指向经过点的向量确定，并将该向量归一化为单位向量。
     *
     * @param origin      射线的起点
     * @param passThrough 射线经过的点，用于确定射线的方向
     */
    public Ray(Node origin, Node passThrough) {
        this.origin = origin;
        double tempx = passThrough.x - origin.x;
        double tempy = passThrough.y - origin.y;
        double hyp = Math.sqrt(tempx * tempx + tempy * tempy);

        this.x = tempx / hyp;
        this.y = tempy / hyp;
    }

    public double cross(MyVector v) {
        return x * v.y - v.x * y;
    }

    /**
     * 计算当前射线与给定向量的交点。
     * 该方法使用参数方程求解射线与向量的交点。
     * 如果射线与向量平行或不相交，则返回null。
     * 如果射线与向量共线且重叠，则返回射线的原点。
     * 如果射线与向量相交，则返回交点坐标。
     *
     * @param d1 要计算交点的向量
     * @return 如果存在交点，返回交点坐标的Node对象；否则返回null
     */
    public Node pointIntersectsAt(MyVector d1) {
        Node p0 = origin, p1 = d1.origin;
        MyVector delta = new MyVector(p0, p1.x - p0.x, p1.y - p0.y);
        Ray d0 = this;
        double d0crossd1 = d0.cross(d1);

        if (d0crossd1 == 0) {
            if (delta.cross(d0) == 0) { // Parallel
                if (d0.origin.equals(d1.origin)
                        || (d0.origin.x == d1.origin.x + d1.x && d0.origin.y == d1.origin.y + d1.y)) {
                    return d0.origin;
                }
            }
            return null;
        } else {
            double s = delta.cross(d1) / d0crossd1;
            double t = delta.cross(d0) / d0crossd1;
            if (t < 0 || t > 1 || s < 0) {
                return null; // Intersects not at a ray/vector point
            } else {
                double x = d1.origin.x + t * d1.x;
                double y = d1.origin.y + t * d1.y;
                return new Node(x, y); // Intersects at this ray point
            }
        }
    }

    /**
     * 返回射线的描述信息。
     * 描述信息包括射线的起点、方向向量和原点。
     * 
     * @return 射线的描述信息
     */
    public String values() {
        return origin.descr() + ", x= " + x + ", y= " + y;
    }

    public String descr() {
        return origin.descr() + ", (" + (x + origin.x) + ", " + (y + origin.y) + ")";
    }

    public void printMe() {
        System.out.println(descr());
    }

    /** 射线的x方向分量，与y分量一起构成单位向量 */
    public double x;

    /** 射线的y方向分量，与x分量一起构成单位向量 */
    public double y;

    /** 射线的原点 */
    public Node origin;
}
