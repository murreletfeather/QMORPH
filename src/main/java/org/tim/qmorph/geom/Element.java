package org.tim.qmorph.geom;

import org.tim.qmorph.meshing.Constants;

/**
 * 这个类声明了四边形和三角形共有的方法和变量。
 */

public abstract class Element extends Constants {

    /** 内部角度数组 */
    public double[] ang;
    /** 边数组 */
    public Edge[] edgeList;
    /** 用于确定反转的节点，以及其他一些东西。 */
    public Node firstNode;
    /**
     * 双精度浮点数，用于存储当前的畸变度量和畸变度量在扰动后的值
     */
    public double distortionMetric, newDistortionMetric;
    /** 双精度浮点数，用于存储梯度向量 */
    public double gX, gY;

    /** @return 共享边e的相邻元素 */
    public abstract Element neighbor(Edge e);

    /** @return 元素内部节点n处的局部角度 */
    public abstract double angle(Edge e, Node n);

    /** 计算并设置元素节点的角度 */
    public abstract void updateAngles();

    /**
     * 计算并设置此特定节点与该元素边相交的角度
     */
    public abstract void updateAngle(Node n);

    /** @return 元素的描述字符串（节点坐标列表） */
    public abstract String descr();

    /** 输出元素的描述字符串（节点坐标列表） */
    public abstract void printMe();

    /** 验证元素是否具有指定的边。 */
    public abstract boolean hasEdge(Edge e);

    /** 验证元素是否具有指定的节点。 */
    public abstract boolean hasNode(Node n);

    /** 验证元素的面积是否大于0。 */
    public abstract boolean areaLargerThan0();

    /** 返回节点n处的局部相邻边。 */
    public abstract Edge neighborEdge(Node n, Edge e);

    /** 返回此元素的边列表中此边的索引。 */
    public abstract int indexOf(Edge e);

    /** 返回此角度在此元素的ang数组中的索引。 */
    public abstract int angleIndex(Edge e1, Edge e2);

    public abstract int angleIndex(Node n);

    /** 返回此元素的边e1和e2之间的角度。 */
    public abstract double angle(Edge e1, Edge e2);

    /** @return true if the元素已经反转 */
    public abstract boolean inverted();

    /** @return true if the元素已经反转或其面积为零。 */
    public abstract boolean invertedOrZeroArea();

    /** @return true if the元素在其节点n处有凹凸。 */
    public abstract boolean concavityAt(Node n);

    /** 用替换边替换指定边e。 */
    public abstract void replaceEdge(Edge e, Edge replacement);

    /** 使edgeList中每个Edge的元素指针指向此元素。 */
    public abstract void connectEdges();

    /**
     * 使edgeList中每个Edge的元素指针指向null。
     */
    public abstract void disconnectEdges();

    /** 为测试目的创建一个简单的元素。 */
    public abstract Element elementWithExchangedNodes(Node original, Node replacement);

    /**
     * @return true if the quad becomes inverted when node n1 is relocated to pos.
     *         n2. Else return false.
     */
    public abstract boolean invertedWhenNodeRelocated(Node n1, Node n2);

    /**
     * 根据文章“An approach to Combined Laplacian and Optimization-Based Smoothing for
     * Triangular, Quadrilateral and Quad-Dominant Meshes”
     * 更新畸变度量。
     */
    public abstract void updateDistortionMetric();

    /** 返回最长边的长度。 */
    public abstract double longestEdgeLength();

    /** 返回最大角度的大小。 */
    public abstract double largestAngle();

    /** 返回最大角度处的节点。 */
    public abstract Node nodeAtLargestAngle();

    /** 将边标记为红色。 */
    public abstract void markEdgesIllegal();

    /** 将边的颜色设置为绿色。 */
    public abstract void markEdgesLegal();

    /**
     * 一个用于快速计算两个向量叉积的方法。
     *
     * @param o1 第一个向量的原点
     * @param p1 第一个向量的终点
     * @param o2 第二个向量的原点
     * @param p2 第二个向量的终点
     * @return 两个向量的叉积
     */
    protected double cross(Node o1, Node p1, Node o2, Node p2) {
        double x1 = p1.x - o1.x;
        double x2 = p2.x - o2.x;
        double y1 = p1.y - o1.y;
        double y2 = p2.y - o2.y;
        return x1 * y2 - x2 * y1;
    }
}
