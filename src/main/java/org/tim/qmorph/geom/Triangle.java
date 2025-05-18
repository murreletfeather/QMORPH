package org.tim.qmorph.geom;

import org.tim.qmorph.viewer.Msg;

/**
 * 三角形类，保存三角形的相关信息，并提供处理三角形相关操作的方法。
 * 继承自Element类。
 */
public class Triangle extends Element {

    /**
     * 创建一个三角形（顶点按逆时针排列）。
     *
     * @param edge1      边1
     * @param edge2      边2
     * @param edge3      边3
     * @param len1       边1长度
     * @param len2       边2长度
     * @param len3       边3长度
     * @param ang1       角1
     * @param ang2       角2
     * @param ang3       角3
     * @param lengthsOpt 是否跳过长度更新
     * @param anglesOpt  是否跳过角度更新
     */
    public Triangle(Edge edge1, Edge edge2, Edge edge3, double len1, double len2, double len3, double ang1, double ang2,
            double ang3, boolean lengthsOpt,
            boolean anglesOpt) {
        edgeList = new Edge[3];

        edgeList[0] = edge1;
        edgeList[1] = edge2;
        edgeList[2] = edge3;

        edgeList[0].len = len1;
        edgeList[1].len = len2;
        edgeList[2].len = len3;

        // Make a pointer to the base node that is the origin of vector(edgeList[a])
        // so that the cross product vector(edgeList[a]) x vector(edgeList[b]) >= 0
        // where a,b in {1,2}
        firstNode = edgeList[0].leftNode;
        if (inverted()) {
            firstNode = edgeList[0].rightNode;
        }

        Edge temp;
        if (firstNode == edgeList[0].commonNode(edgeList[2])) {
            temp = edgeList[1];
            edgeList[1] = edgeList[2];
            edgeList[2] = temp;
        }

        ang = new double[3];
        ang[0] = ang1;
        ang[1] = ang2;
        ang[2] = ang3;

        if (!lengthsOpt) {
            updateLengths();
        }
        if (!anglesOpt) {
            updateAngles();
        }
    }

    /**
     * 创建一个三角形（顶点按逆时针排列）。
     *
     * @param edge1 边1
     * @param edge2 边2
     * @param edge3 边3
     */
    public Triangle(Edge edge1, Edge edge2, Edge edge3) {
        edgeList = new Edge[3];

        if (edge1 == null || edge2 == null || edge3 == null) {
            Msg.error("Triangle: cannot create Triangle with null Edge.");
        }

        edgeList[0] = edge1;
        edgeList[1] = edge2;
        edgeList[2] = edge3;

        // Make a pointer to the base node that is the origin of vector(edgeList[a])
        // so that the cross product vector(edgeList[a]) x vector(edgeList[b]) >= 0
        // where a,b in {1,2}
        firstNode = edgeList[0].leftNode;
        if (inverted()) {
            firstNode = edgeList[0].rightNode;
        }

        Edge temp;
        if (firstNode == edgeList[0].commonNode(edgeList[2])) {
            temp = edgeList[1];
            edgeList[1] = edgeList[2];
            edgeList[2] = temp;
        }

        ang = new double[3];
        updateAngles();
    }

    /**
     * 复制构造函数，生成三角形的副本。
     * 
     * @param t 要复制的三角形
     */
    private Triangle(Triangle t) {
        edgeList = new Edge[3];

        edgeList[0] = new Edge(t.edgeList[0].leftNode, t.edgeList[0].rightNode);
        // t.edgeList[0].copy();
        edgeList[1] = new Edge(t.edgeList[1].leftNode, t.edgeList[1].rightNode);
        // t.edgeList[1].copy();
        edgeList[2] = new Edge(t.edgeList[2].leftNode, t.edgeList[2].rightNode);
        // t.edgeList[2].copy();

        ang = new double[3];
        ang[0] = t.ang[0];
        ang[1] = t.ang[1];
        ang[2] = t.ang[2];

        firstNode = t.firstNode;
    }

    // Create a simple triangle for testing purposes only
    // (constrainedLaplacianSmooth()
    // and optBasedSmooth(..))
    // The triangle created is a copy of this, and each of the edges are also
    // copies.
    // One of the nodes in the triangle has been replaced.
    // The Edge.repleceNode(..) method updates the edge length.
    // *Not tested!!!*

    /**
     * 用于节点替换，返回替换节点后的新三角形。
     * 
     * @param original    原节点
     * @param replacement 替换节点
     * @return 替换节点后的新三角形
     */
    @Override
    public Element elementWithExchangedNodes(Node original, Node replacement) {
        Node node1 = edgeList[0].leftNode;
        Node node2 = edgeList[0].rightNode;
        Node node3 = edgeList[1].rightNode;
        if (node3 == node1 || node3 == node2) {
            node3 = edgeList[1].leftNode;
        }
        Node common1;

        // Make a copy of the original triangle...
        Triangle t = new Triangle(this);
        Edge edge1 = t.edgeList[0], edge2 = t.edgeList[1], edge3 = t.edgeList[2];

        // ... and then replace the node
        if (node1 == original) {
            common1 = edge1.commonNode(edge2);
            if (original == common1) {
                edge2.replaceNode(original, replacement);
            } else {
                edge3.replaceNode(original, replacement);
            }
            edge1.replaceNode(original, replacement);
        } else if (node2 == original) {
            common1 = edge1.commonNode(edge2);
            if (original == common1) {
                edge2.replaceNode(original, replacement);
            } else {
                edge3.replaceNode(original, replacement);
            }
            edge1.replaceNode(original, replacement);
        } else if (node3 == original) {
            common1 = edge2.commonNode(edge1);
            if (original == common1) {
                edge1.replaceNode(original, replacement);
            } else {
                edge3.replaceNode(original, replacement);
            }
            edge2.replaceNode(original, replacement);
        } else {
            return null;
        }

        if (original == firstNode) {
            t.firstNode = replacement;
        } else {
            t.firstNode = firstNode;
        }
        return t;
    }

    /**
     * 判断将节点n1移动到n2后，三角形是否会反转。
     * 
     * @param n1 被移动的节点
     * @param n2 新位置节点
     * @return 反转返回true，否则返回false
     */
    @Override
    public boolean invertedWhenNodeRelocated(Node n1, Node n2) {
        Msg.debug("Entering Triangle.invertedWhenNodeRelocated(..)");
        Node a, b, c;
        MyVector ac, bc;
        a = firstNode;
        b = edgeList[0].otherNode(a);
        c = edgeList[1].rightNode;
        if (c == a || c == b) {
            c = edgeList[1].leftNode;
        }

        if (a == n1) {
            a = n2;
        } else if (b == n1) {
            b = n2;
        } else if (c == n1) {
            c = n2;
        }

        ac = new MyVector(a, c);
        bc = new MyVector(b, c);

        Msg.debug("Leaving Triangle.invertedWhenNodeRelocated(..)");
        if (ac.cross(bc) <= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Triangle) {
            Triangle t = (Triangle) o;
            Node node1, node2, node3;
            Node tnode1, tnode2, tnode3;
            node1 = edgeList[0].leftNode;
            node2 = edgeList[0].rightNode;
            node3 = edgeList[1].rightNode;
            if (node3 == node1 || node3 == node2) {
                node3 = edgeList[1].leftNode;
            }

            tnode1 = t.edgeList[0].leftNode;
            tnode2 = t.edgeList[0].rightNode;
            tnode3 = t.edgeList[1].rightNode;
            if (tnode3 == tnode1 || tnode3 == tnode2) {
                tnode3 = t.edgeList[1].leftNode;
            }

            if (node1 == tnode1 && node2 == tnode2 && node3 == tnode3) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 获取指定边和节点对应的角度。
     * 
     * @param e 边
     * @param n 节点
     * @return 角度值
     */
    @Override
    public double angle(Edge e, Node n) {
        return ang[angleIndex(e, neighborEdge(n, e))];
    }

    /**
     * 获取两条边之间的角度。
     * 
     * @param e1 边1
     * @param e2 边2
     * @return 角度值
     */
    @Override
    public double angle(Edge e1, Edge e2) {
        return ang[angleIndex(e1, e2)];
    }

    /**
     * 更新三角形的边长和角度。
     */
    public void updateAttributes() {
        updateLengths();
        updateAngles();
    }

    /**
     * 更新三角形的三条边的长度。
     */
    public void updateLengths() {
        edgeList[0].len = edgeList[0].computeLength();
        edgeList[1].len = edgeList[1].computeLength();
        edgeList[2].len = edgeList[2].computeLength();
    }

    /**
     * 更新与指定节点相关的角度。
     * 
     * @param n 节点
     */
    @Override
    public void updateAngle(Node n) {
        int j = angleIndex(n), i;

        i = angleIndex(edgeList[0], edgeList[1]);
        if (j == i) {
            ang[i] = edgeList[0].computeCCWAngle(edgeList[1]);
        } else {
            i = angleIndex(edgeList[1], edgeList[2]);
            if (j == i) {
                ang[i] = edgeList[1].computeCCWAngle(edgeList[2]);
            } else {
                i = angleIndex(edgeList[0], edgeList[2]);
                if (j == i) {
                    ang[i] = edgeList[2].computeCCWAngle(edgeList[0]);
                } else {
                    Msg.error("Quad.updateAngle(Node): Node not found.");
                }
            }
        }
    }

    /**
     * 更新三角形的所有角度。
     */
    @Override
    public void updateAngles() {
        int i;
        i = angleIndex(edgeList[0], edgeList[1]);
        ang[i] = edgeList[0].computeCCWAngle(edgeList[1]);

        i = angleIndex(edgeList[1], edgeList[2]);
        ang[i] = edgeList[1].computeCCWAngle(edgeList[2]);

        i = angleIndex(edgeList[0], edgeList[2]);
        ang[i] = edgeList[2].computeCCWAngle(edgeList[0]);
    }

    /**
     * 判断三角形是否包含指定的边。
     * 
     * @param e 边
     * @return 包含返回true，否则返回false
     */
    @Override
    public boolean hasEdge(Edge e) {
        if (edgeList[0] == e || edgeList[1] == e || edgeList[2] == e) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断三角形是否包含指定的节点。
     * 
     * @param n 节点
     * @return 包含返回true，否则返回false
     */
    @Override
    public boolean hasNode(Node n) {
        if (edgeList[0].hasNode(n) || edgeList[1].hasNode(n) || edgeList[2].hasNode(n)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回三角形中除指定边外的另一条边。
     * 
     * @param e 指定的边
     * @return 另一条边
     */
    public Edge otherEdge(Edge e) {
        if (edgeList[0] != e) {
            return edgeList[0];
        } else if (edgeList[1] != e) {
            return edgeList[1];
        }
        if (edgeList[2] != e) {
            return edgeList[2];
        } else {
            Msg.error("Cannot find an other edge in triangle " + descr() + ".");
            return null;
        }
    }

    /**
     * 返回三角形中除指定两条边外的第三条边。
     * 
     * @param e1 边1
     * @param e2 边2
     * @return 第三条边
     */
    public Edge otherEdge(Edge e1, Edge e2) {
        if ((edgeList[0] == e1 && edgeList[1] == e2) || (edgeList[1] == e1 && edgeList[0] == e2)) {
            return edgeList[2];
        } else if ((edgeList[1] == e1 && edgeList[2] == e2) || (edgeList[2] == e1 && edgeList[1] == e2)) {
            return edgeList[0];
        } else if ((edgeList[0] == e1 && edgeList[2] == e2) || (edgeList[2] == e1 && edgeList[0] == e2)) {
            return edgeList[1];
        } else {
            Msg.error("Cannot find the other edge in triangle " + descr() + ".");
            return null;
        }
    }

    /**
     * 获取与该三角形共享指定边的相邻元素。
     * 
     * @param e 边
     * @return 相邻元素
     */
    @Override
    public Element neighbor(Edge e) {
        if (e.element1 == this) {
            return e.element2;
        } else if (e.element2 == this) {
            return e.element1;
        } else {
            Msg.warning("Triangle.neighbor(Edge): returning null");
            return null;
        }
    }

    /**
     * 获取与指定节点和边相邻的另一条边。
     * 
     * @param n 节点
     * @param e 边
     * @return 邻边
     */
    @Override
    public Edge neighborEdge(Node n, Edge e) {
        if (edgeList[0] != e && edgeList[0].hasNode(n)) {
            return edgeList[0];
        } else if (edgeList[1] != e && edgeList[1].hasNode(n)) {
            return edgeList[1];
        } else if (edgeList[2] != e && edgeList[2].hasNode(n)) {
            return edgeList[2];
        } else {
            Msg.error("Triangle.neighborEdge(Node, Edge): Neighbor not found.");
            return null; // e
        }
    }

    /**
     * 获取指定边在三角形中的索引。
     * 
     * @param e 边
     * @return 索引（0-2），未找到返回-1
     */
    @Override
    public int indexOf(Edge e) {
        if (edgeList[0] == e) {
            return 0;
        } else if (edgeList[1] == e) {
            return 1;
        } else if (edgeList[2] == e) {
            return 2;
        } else {
            Msg.warning("Triangle.indexOf(Edge): Edge not part of triangle.");
            return -1;
        }
    }

    /**
     * 获取两条边在角度数组中的索引。
     * 
     * @param e1Index 边1索引
     * @param e2Index 边2索引
     * @return 角度索引
     */
    public int angleIndex(int e1Index, int e2Index) {
        // angle betw. edges 0 && 1
        if ((e1Index == 0 && e2Index == 1) || (e1Index == 1 && e2Index == 0)) {
            return 2;
        } else if ((e1Index == 1 && e2Index == 2) || (e1Index == 2 && e2Index == 1)) {
            return 0;
        } else if ((e1Index == 0 && e2Index == 2) || (e1Index == 2 && e2Index == 0)) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 获取与指定节点相关的角度索引。
     * 
     * @param n 节点
     * @return 角度索引
     */
    @Override
    public int angleIndex(Node n) {
        // angle betw. edges 0 && 1
        if (edgeList[0].commonNode(edgeList[1]) == n) {
            return 2;
        } else if (edgeList[1].commonNode(edgeList[2]) == n) {
            return 0;
        } else if (edgeList[2].commonNode(edgeList[0]) == n) {
            return 1;
        } else {
            Msg.error("Quad.angleIndex(Node): Node not found");
            return -1;
        }
    }

    /**
     * 获取两条边在角度数组中的索引。
     * 
     * @param e1 边1
     * @param e2 边2
     * @return 角度索引
     */
    @Override
    public int angleIndex(Edge e1, Edge e2) {
        return angleIndex(indexOf(e1), indexOf(e2));
    }

    /**
     * 获取与指定节点相对的边。
     * 
     * @param n 节点
     * @return 对边
     */
    public Edge oppositeOfNode(Node n) {
        if (edgeList[0].hasNode(n) && edgeList[1].hasNode(n)) {
            return edgeList[2];
        } else if (edgeList[0].hasNode(n) && edgeList[2].hasNode(n)) {
            return edgeList[1];
        } else if (edgeList[1].hasNode(n) && edgeList[2].hasNode(n)) {
            return edgeList[0];
        } else {
            Msg.error("Cannot find opposide edge of node.");
            return null;
        }
    }

    /**
     * 获取与指定边相对的节点。
     * 
     * @param e 边
     * @return 对节点
     */
    public Node oppositeOfEdge(Edge e) {
        if (edgeList[0] == e) {
            if (!e.hasNode(edgeList[1].leftNode)) {
                return edgeList[1].leftNode;
            } else {
                return edgeList[1].rightNode;
            }
        } else if (edgeList[1] == e) {
            if (!e.hasNode(edgeList[2].leftNode)) {
                return edgeList[2].leftNode;
            } else {
                return edgeList[2].rightNode;
            }
        } else if (edgeList[2] == e) {
            if (!e.hasNode(edgeList[0].leftNode)) {
                return edgeList[0].leftNode;
            } else {
                return edgeList[0].rightNode;
            }
        } else {
            Msg.error("oppositeOfEdge: Cannot find node opposite of the supplied edge.");
            return null;
        }
    }

    /**
     * 让三角形的三条边都指向该三角形。
     */
    @Override
    public void connectEdges() {
        edgeList[0].connectToTriangle(this);
        edgeList[1].connectToTriangle(this);
        edgeList[2].connectToTriangle(this);
    }

    /**
     * 断开三角形三条边与该三角形的连接。
     */
    @Override
    public void disconnectEdges() {
        edgeList[0].disconnectFromElement(this);
        edgeList[1].disconnectFromElement(this);
        edgeList[2].disconnectFromElement(this);
    }

    /**
     * 获取三角形中指定边的下一个逆时针方向的边。
     * 
     * @param e1 当前边
     * @return 下一个逆时针边
     */
    public Edge nextCCWEdge(Edge e1) {
        Node e1commone2, e1commone3;
        Edge e2, e3;
        MyVector v1Forv2, v1Forv3, v2, v3;
        if (e1 == edgeList[0]) {
            e2 = edgeList[1];
            e3 = edgeList[2];
        } else if (e1 == edgeList[1]) {
            e2 = edgeList[0];
            e3 = edgeList[2];
        } else if (e1 == edgeList[2]) {
            e2 = edgeList[0];
            e3 = edgeList[1];
        } else {
            Msg.error("Edge " + e1.descr() + " is not part of triangle");
            return null;
        }

        e1commone2 = e1.commonNode(e2);
        e1commone3 = e1.commonNode(e3);

        v2 = new MyVector(e1commone2, e2.otherNode(e1commone2));
        v3 = new MyVector(e1commone3, e3.otherNode(e1commone3));
        v1Forv2 = new MyVector(e1commone2, e1.otherNode(e1commone2));
        v1Forv3 = new MyVector(e1commone3, e1.otherNode(e1commone3));

        // Positive angles between e1 and each of the other two edges:
        double ang1, ang2;
        ang1 = ang[angleIndex(e1, e2)];
        ang2 = ang[angleIndex(e1, e3)];

        if (v2.isCWto(v1Forv2)) {
            ang1 += Math.PI;
        }
        if (v3.isCWto(v1Forv3)) {
            ang2 += Math.PI;
        }

        if (ang2 > ang1) {
            return e3;
        } else {
            return e2;
        }
    }

    /**
     * 获取三角形中指定边的下一个顺时针方向的边。
     * 
     * @param e1 当前边
     * @return 下一个顺时针边
     */
    public Edge nextCWEdge(Edge e1) {
        Node e1commone2, e1commone3;
        Edge e2, e3;
        MyVector v1Forv2, v1Forv3, v2, v3;
        if (e1 == edgeList[0]) {
            e2 = edgeList[1];
            e3 = edgeList[2];
        } else if (e1 == edgeList[1]) {
            e2 = edgeList[0];
            e3 = edgeList[2];
        } else if (e1 == edgeList[2]) {
            e2 = edgeList[0];
            e3 = edgeList[1];
        } else {
            Msg.error("Edge " + e1.descr() + " is not part of triangle");
            return null;
        }

        e1commone2 = e1.commonNode(e2);
        e1commone3 = e1.commonNode(e3);

        v2 = new MyVector(e1commone2, e2.otherNode(e1commone2));
        v3 = new MyVector(e1commone3, e3.otherNode(e1commone3));
        v1Forv2 = new MyVector(e1commone2, e1.otherNode(e1commone2));
        v1Forv3 = new MyVector(e1commone3, e1.otherNode(e1commone3));

        // Positive angles between e1 and each of the other two edges:
        double ang1, ang2;
        ang1 = ang[angleIndex(e1, e2)];
        ang2 = ang[angleIndex(e1, e3)];

        // Transform into true angles:
        e1commone2 = e1.commonNode(e2);
        e1commone3 = e1.commonNode(e3);

        v2 = new MyVector(e1commone2, e2.otherNode(e1commone2));
        v3 = new MyVector(e1commone3, e3.otherNode(e1commone3));
        v1Forv2 = new MyVector(e1commone2, e1.otherNode(e1commone2));
        v1Forv3 = new MyVector(e1commone3, e1.otherNode(e1commone3));

        if (v2.isCWto(v1Forv2)) {
            ang1 += Math.PI;
        }
        if (v3.isCWto(v1Forv3)) {
            ang2 += Math.PI;
        }

        if (ang2 > ang1) {
            return e2;
        } else {
            return e3;
        }
    }

    /**
     * 判断三角形面积是否大于0。
     * 
     * @return 面积大于0返回true，否则返回false
     */
    @Override
    public boolean areaLargerThan0() {
        Node na = edgeList[0].leftNode;
        Node nb = edgeList[0].rightNode;
        Node nc = oppositeOfEdge(edgeList[0]);

        if (cross(na, nc, nb, nc) != 0) { // The cross product nanc x nbnc
            return true;
        } else { // The cross product nanc x nbnc
            return false;
        }

        /*
         * Node n= oppositeOfEdge(edgeList[0]); if (n.onLine(edgeList[0])) return false;
         * else return true;
         */
    }

    /**
     * 判断将节点从oldN移动到newN后三角形是否反转。
     * 
     * @param oldN 原节点
     * @param newN 新节点
     * @return 反转返回true，否则返回false
     */
    public boolean inverted(Node oldN, Node newN) {
        Edge e = oppositeOfNode(newN);
        // Check with edge e:
        double oldN_e_det = (e.leftNode.x - oldN.x) * (e.rightNode.y - oldN.y)
                - (e.leftNode.y - oldN.y) * (e.rightNode.x - oldN.x);
        double newN_e_det = (e.leftNode.x - newN.x) * (e.rightNode.y - newN.y)
                - (e.leftNode.y - newN.y) * (e.rightNode.x - newN.x);

        // If different sign, or 0, they are inverted:
        if (oldN_e_det >= 0) {
            if (newN_e_det <= 0 || oldN_e_det == 0) {
                Msg.debug("Triangle.inverted(..): Triangle " + descr() + " is inverted");
                return true;
            }
        } else if (newN_e_det >= 0) {
            Msg.debug("Triangle.inverted(..): Triangle " + descr() + " is inverted");
            return true;
        }
        return false;
    }

    /**
     * 判断三角形是否反转。
     * 
     * @return 反转返回true，否则返回false
     */
    @Override
    public boolean inverted() {
        Node a, b, c;
        a = firstNode;
        b = edgeList[0].otherNode(a);
        c = edgeList[1].rightNode;
        if (c == a || c == b) {
            c = edgeList[1].leftNode;
        }

        if (cross(a, c, b, c) < 0) {
            // if (!ac.newcross(bc))
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断三角形是否反转或面积为零。
     * 
     * @return 反转或零面积返回true，否则返回false
     */
    @Override
    public boolean invertedOrZeroArea() {
        Node a, b, c;
        a = firstNode;
        b = edgeList[0].otherNode(a);
        c = edgeList[1].rightNode;
        if (c == a || c == b) {
            c = edgeList[1].leftNode;
        }

        if (cross(a, c, b, c) <= 0) {
            // if (!ac.newcross(bc))
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断三角形面积是否为零。
     * 
     * @return 零面积返回true，否则返回false
     */
    public boolean zeroArea() {
        Node a, b, c;
        a = firstNode;
        b = edgeList[0].otherNode(a);
        c = edgeList[1].rightNode;
        if (c == a || c == b) {
            c = edgeList[1].leftNode;
        }

        if (cross(a, c, b, c) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 三角形没有凹点，始终返回false。
     * 
     * @param n 节点
     * @return false
     */
    @Override
    public boolean concavityAt(Node n) {
        return false;
    }

    /**
     * 替换三角形中的某条边。
     * 
     * @param e           原边
     * @param replacement 替换边
     */
    @Override
    public void replaceEdge(Edge e, Edge replacement) {
        edgeList[indexOf(e)] = replacement;
    }

    /**
     * 获取三角形中一条边界边（element2为null）。
     * 
     * @return 边界边，若无则返回null
     */
    public Edge getBoundaryEdge() {
        if (edgeList[0].element2 == null) {
            return edgeList[0];
        } else if (edgeList[1].element2 == null) {
            return edgeList[1];
        } else if (edgeList[2].element2 == null) {
            return edgeList[2];
        } else {
            return null;
        }
    }

    /**
     * 更新三角形的畸变度量。
     * 参考文献：An approach to Combined Laplacian and Optimization-Based Smoothing for
     * Triangular, Quadrilateral and Quad-Dominant Meshes
     */
    @Override
    public void updateDistortionMetric() {
        updateDistortionMetric(sqrt3x2);
    }

    /**
     * 更新三角形的畸变度量。
     * 
     * @param factor 畸变因子
     */
    public void updateDistortionMetric(double factor) {
        Msg.debug("Entering Triangle.updateDistortionMetric(..)");
        double AB = edgeList[0].len, CB = edgeList[1].len, CA = edgeList[2].len;
        Node a = edgeList[2].commonNode(edgeList[0]), b = edgeList[0].commonNode(edgeList[1]),
                c = edgeList[2].commonNode(edgeList[1]);
        MyVector vCA = new MyVector(c, a), vCB = new MyVector(c, b);

        double temp = factor * Math.abs(vCA.cross(vCB)) / (CA * CA + AB * AB + CB * CB);
        if (inverted()) {
            distortionMetric = -temp;
        } else {
            distortionMetric = temp;
        }
        Msg.debug("Leaving Triangle.updateDistortionMetric(..): " + distortionMetric);
    }

    /**
     * 获取三角形的最大角度。
     * 
     * @return 最大角度
     */
    @Override
    public double largestAngle() {
        double cand = ang[0];
        if (ang[1] > cand) {
            cand = ang[1];
        }
        if (ang[2] > cand) {
            cand = ang[2];
        }
        return cand;
    }

    /**
     * 获取三角形最大内角对应的节点。
     * 
     * @return 最大内角节点
     */
    @Override
    public Node nodeAtLargestAngle() {
        Node candNode = edgeList[0].leftNode;
        double cand = ang[angleIndex(candNode)], temp;

        temp = ang[angleIndex(edgeList[0].rightNode)];
        if (temp > cand) {
            candNode = edgeList[0].rightNode;
            cand = temp;
        }
        temp = ang[angleIndex(oppositeOfEdge(edgeList[0]))];
        if (temp > cand) {
            candNode = oppositeOfEdge(edgeList[0]);
        }
        return candNode;
    }

    /**
     * 获取三角形最长边的长度。
     * 
     * @return 最长边长度
     */
    @Override
    public double longestEdgeLength() {
        double temp = Math.max(edgeList[0].len, edgeList[1].len);
        return Math.max(temp, edgeList[2].len);
    }

    /**
     * 获取三角形最长的边。
     * 
     * @return 最长边
     */
    public Edge longestEdge() {
        Edge temp;
        if (edgeList[0].len > edgeList[1].len) {
            temp = edgeList[0];
        } else {
            temp = edgeList[1];
        }

        if (edgeList[2].len > temp.len) {
            return edgeList[2];
        } else {
            return temp;
        }
    }

    /**
     * 将三角形的三条边颜色标记为合法（绿色）。
     */
    @Override
    public void markEdgesLegal() {
        edgeList[0].color = java.awt.Color.green;
        edgeList[1].color = java.awt.Color.green;
        edgeList[2].color = java.awt.Color.green;
    }

    /**
     * 将三角形的三条边颜色标记为非法（红色）。
     */
    @Override
    public void markEdgesIllegal() {
        edgeList[0].color = java.awt.Color.red;
        edgeList[1].color = java.awt.Color.red;
        edgeList[2].color = java.awt.Color.red;
    }

    /**
     * 获取三角形的字符串描述。
     * 
     * @return 描述字符串
     */
    @Override
    public String descr() {
        Node node1, node2, node3;
        node1 = edgeList[0].leftNode;
        node2 = edgeList[0].rightNode;
        node3 = edgeList[1].rightNode;
        if (node3 == node1 || node3 == node2) {
            node3 = edgeList[1].leftNode;
        }

        return node1.descr() + ", " + node2.descr() + ", " + node3.descr();
    }

    /**
     * 打印三角形的描述信息。
     */
    @Override
    public void printMe() {
        if (inverted()) {
            System.out.println(descr() + ", inverted");
        } else {
            System.out.println(descr() + ", not inverted");
        }

    }

    /**
     * 获取三角形的字符串描述。
     * 
     * @return 描述字符串
     */
    @Override
    public String toString() {
        return descr();
    }
}
