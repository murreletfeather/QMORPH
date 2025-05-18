package org.tim.qmorph.geom;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.tim.qmorph.meshing.Constants;
import org.tim.qmorph.viewer.Msg;

/**
 * 该类用于保存节点（Node）相关的信息，并提供节点管理和操作的方法。
 * 包括节点的坐标、相连边、颜色、以及各种网格操作和几何计算。
 */
public class Node extends Constants {

    /**
     * 指示该节点是否被OBS平滑器移动过。
     */
    public boolean movedByOBS = false; // Used by the smoother
    /**
     * 节点的x坐标。
     */
    public double x;
    /**
     * 节点的y坐标。
     */
    public double y;
    /**
     * 该节点的价模式（邻接边数模式）。
     */
    public byte[] pattern;
    // byte state= 0; // For front Nodes only
    /**
     * 与该节点相连的所有边的列表。
     */
    public List<Edge> edgeList;
    /**
     * 节点的颜色。
     */
    public Color color = Color.cyan;

    /**
     * 创建一个指定坐标的新节点。
     * 
     * @param x x坐标
     * @param y y坐标
     */
    public Node(double x, double y) {
        this.x = x;
        this.y = y;
        edgeList = new ArrayList<>();
    }

    /**
     * 判断当前节点与另一个对象是否相等（坐标相同即为相等）。
     * 
     * @param elem 另一个对象
     * @return 如果是Node且坐标相同返回true，否则返回false
     */
    @Override
    public boolean equals(Object elem) {
        if (!(elem instanceof Node)) {
            return false;
        }
        Node node = (Node) elem;
        if (x == node.x && y == node.y) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回该节点的一个"浅拷贝"，edgeList也会被浅拷贝。
     * 
     * @return 新的节点对象
     */
    public Node copy() {
        Node n = new Node(x, y);
        n.edgeList = new ArrayList<>(edgeList);
        return n;
    }

    /**
     * 返回一个具有相同坐标的新节点。
     * 
     * @return 新的节点对象
     */
    public Node copyXY() {
        return new Node(x, y);
    }

    /**
     * 将当前节点移动到另一个节点的位置。
     * 
     * @param n 目标节点
     */
    public void setXY(Node n) {
        x = n.x;
        y = n.y;
    }

    /**
     * 将当前节点移动到指定坐标。
     * 
     * @param x 新的x坐标
     * @param y 新的y坐标
     */
    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 更新与该节点相关的左右节点、边长度和角度信息。
     */
    public void update() {
        updateLRinEdgeList();
        updateEdgeLengths();
        updateAngles();
    }

    /**
     * 更新edgeList中所有边的左右节点信息。
     */
    public void updateLRinEdgeList() {
        boolean btemp;
        Edge temp;
        Node node;
        Quad q;
        for (Edge e : edgeList) {
            if ((e.leftNode.x > e.rightNode.x) || (e.leftNode.x == e.rightNode.x && e.leftNode.y < e.rightNode.y)) {
                node = e.leftNode;
                e.leftNode = e.rightNode;
                e.rightNode = node;

                if (e.frontEdge) {
                    temp = e.leftFrontNeighbor;
                    e.leftFrontNeighbor = e.rightFrontNeighbor;
                    e.rightFrontNeighbor = temp;
                    btemp = e.leftSide;
                    e.leftSide = e.rightSide;
                    e.rightSide = btemp;
                }

                if (e.element1 instanceof Quad) {
                    q = (Quad) e.element1;
                    if (e == q.edgeList[base]) {
                        q.updateLR();
                    }
                }

                if (e.element2 instanceof Quad) {
                    q = (Quad) e.element2;
                    if (e == q.edgeList[base]) {
                        q.updateLR();
                    }
                }
            }
        }
    }

    /**
     * 将节点移动到指定坐标，并更新相关边的左右节点信息。
     * 
     * @param x 新的x坐标
     * @param y 新的y坐标
     */
    public void moveToPos(double x, double y) {
        this.x = x;
        this.y = y;

        updateLRinEdgeList();
    }

    /**
     * 将节点移动到另一个节点的位置，并更新相关边的左右节点信息。
     * 
     * @param n 目标节点
     */
    public void moveTo(Node n) {
        x = n.x;
        y = n.y;

        updateLRinEdgeList();
    }

    /**
     * 更新与该节点相连的所有边的长度。
     */
    public void updateEdgeLengths() {
        for (Edge e : edgeList) {
            e.len = e.computeLength();
        }
    }

    /**
     * 更新节点的状态（预留方法，当前未实现）。
     */
    public void updateState() {

    }

    /**
     * 更新与该节点相关的所有角度信息（实验性方法）。
     * 遍历所有与该节点相连的边，更新其相邻元素的角度。
     */
    public void updateAngles() {
        Msg.debug("Entering Node.updateAngles()");
        Edge e, ne;
        Node other1, other2;
        Quad q;
        ArrayList<Element> list = new ArrayList<>();

        for (Edge element : edgeList) {
            e = element;
            Msg.debug("...e: " + e.descr());
            if (!list.contains(e.element1)) {
                Msg.debug("...e.element1: " + e.element1.descr());
                list.add(e.element1);
                ne = e.element1.neighborEdge(this, e);
                Msg.debug("...getting other1:  (elem1)");
                other1 = e.otherNode(this);
                Msg.debug("...getting other2:  (elem1)");

                other2 = ne.otherNode(this);

                if (e.element1 instanceof Triangle) {
                    e.element1.updateAngles();
                } else {
                    q = (Quad) e.element1;
                    q.updateAnglesExcept(q.oppositeNode(this));
                }
            }
            if (e.element2 != null && !list.contains(e.element2)) {
                list.add(e.element2);
                ne = e.element2.neighborEdge(this, e);
                Msg.debug("...getting other1:  (elem2)");
                other1 = e.otherNode(this);
                Msg.debug("...getting other2:  (elem2)");

                other2 = ne.otherNode(this);

                if (e.element2 instanceof Triangle) {
                    e.element2.updateAngles();
                } else {
                    q = (Quad) e.element2;
                    q.updateAnglesExcept(q.oppositeNode(this));
                }
            }
        }
        Msg.debug("Leaving Node.updateAngles()");
    }

    /**
     * 更新与该节点相连的所有边的长度和角度（旧版方法，已弃用）。
     *
     * @deprecated 建议使用 updateEdgeLengths 和 updateAngles 替代。
     */
    @Deprecated
    public void oldupdateEdgeLengthsAndAngles() {
        Edge curEdge = edgeList.get(0);
        Element curElem = curEdge.element1;
        Edge nextEdge = curElem.neighborEdge(this, curEdge);
        Edge otherEdge;
        Node otherNode;

        // Parse all edges connected to this node:
        do {
            // Set computed lenghts:
            curEdge.len = curEdge.computeLength();
            nextEdge.len = nextEdge.computeLength();

            // Set computed angle between current and next edge:
            curElem.ang[curElem.angleIndex(curEdge, nextEdge)] = curEdge.computePosAngle(nextEdge, this);

            // Set the two other affected angles in current element:
            otherNode = curEdge.otherNode(this);
            otherEdge = curElem.neighborEdge(otherNode, curEdge);
            curElem.ang[curElem.angleIndex(curEdge, otherEdge)] = curEdge.computePosAngle(otherEdge, otherNode);

            otherNode = nextEdge.otherNode(this);
            otherEdge = curElem.neighborEdge(otherNode, nextEdge);
            curElem.ang[curElem.angleIndex(nextEdge, otherEdge)] = nextEdge.computePosAngle(otherEdge, otherNode);

            // Prepare for next element:
            curElem = curElem.neighbor(nextEdge);
            curEdge = nextEdge;
            nextEdge = curElem.neighborEdge(this, curEdge);
        } while (curElem != null && curEdge != edgeList.get(0));
    }

    /**
     * 计算当前节点与另一个节点的叉积。
     * 
     * @param n 另一个节点
     * @return 叉积结果
     */
    public double cross(Node n) {
        return x * n.y - n.x * y;
    }

    /**
     * 将一条边连接到该节点（如果尚未连接）。
     * 
     * @param edge 要连接的边
     */
    public void connectToEdge(Edge edge) {
        if (!edgeList.contains(edge)) {
            edgeList.add(edge);
        }
    }

    /**
     * 获取以当前节点为起点的、逆时针排序的向量列表。
     * 
     * @return 逆时针排序的向量列表
     */
    public ArrayList<MyVector> ccwSortedVectorList() {
        Element elem, start;
        MyVector v, v0, v1;
        Edge e;
        ArrayList<MyVector> boundaryVectors = new ArrayList<>();
        ArrayList<MyVector> vectors = new ArrayList<>();
        double ang;
        for (Edge element : edgeList) {
            e = element;
            v = e.getVector(this);
            v.edge = e;

            if (e.boundaryEdge()) {
                boundaryVectors.add(v);
            } else {
                vectors.add(v);
            }
        }

        // If the edgeList contains boundary edges, then select the vector of most CW
        // of these.
        // Else select the vector of an arbitrary edge.
        // The selected vector is put into v0.
        // Sets elem to the element that is ccw to v0 around this Node

        if (boundaryVectors.size() > 0) { // this size is always 0 or 2
            Msg.debug("...boundaryVectors yeah!");
            v0 = boundaryVectors.get(0);
            v1 = boundaryVectors.get(1);

            elem = v0.edge.element1;
            e = elem.neighborEdge(this, v0.edge);
            v = e.getVector(this);
            v.edge = e;

            if (v0.isCWto(v)) {
                if (elem.concavityAt(this)) {
                    v0 = v1;
                    elem = v1.edge.element1;
                }
            } else if (!elem.concavityAt(this)) {
                v0 = v1;
                elem = v1.edge.element1;
            }
        } else {
            Msg.debug("...boundaryVectors noooo!");
            v0 = vectors.get(0);
            elem = v0.edge.element1;
            e = elem.neighborEdge(this, v0.edge);
            v1 = e.getVector(this);
            v1.edge = e;

            if (v0.isCWto(v1)) {
                if (elem.concavityAt(this)) {
                    v0 = v1;
                }
            } else if (!elem.concavityAt(this)) {
                v0 = v1;
            }
        }

        Msg.debug("Node.ccwSortedVectorList(..): 0: " + v0.edge.descr());

        // Sort vectors in ccw order starting with v0.
        // Uses the fact that elem initially is the element ccw to v0 around this Node.
        ArrayList<MyVector> VS = new ArrayList<>();
        e = v0.edge;

        start = elem;
        do {
            v = e.getVector(this);
            v.edge = e;
            Msg.debug("... VS.add(" + v.descr() + ")");
            VS.add(v);

            e = elem.neighborEdge(this, e);
            elem = elem.neighbor(e);
        } while (elem != start && elem != null);

        if (elem == null) {
            v = e.getVector(this);
            v.edge = e;
            Msg.debug("... VS.add(" + v.descr() + ")");
            VS.add(v);
        }

        return VS;
    }

    /**
     * 计算以两个边为边界的、逆时针排序的边列表。
     * 
     * @param b0 第一个边界边
     * @param b1 第二个边界边
     * @return 逆时针排序的边列表
     */
    public List<Edge> calcCCWSortedEdgeList(Edge b0, Edge b1) {
        MyVector v, v0, v1;
        Edge e;
        ArrayList<MyVector> vectors = new ArrayList<>();

        for (Edge element : edgeList) {
            e = element;
            if (e != b0 && e != b1) {
                v = e.getVector(this);
                v.edge = e;
                vectors.add(v);
            }
        }

        // Initially put the two vectors of b0 and b1 in list.
        // Select the most CW boundary edge to be first in list.

        List<MyVector> VS = new ArrayList<>();
        v0 = b0.getVector(this);
        v0.edge = b0;
        v1 = b1.getVector(this);
        v1.edge = b1;

        if (vectors.size() > 0) {
            v = vectors.get(0);
        } else {
            v = v1;
        }

        if (v0.isCWto(v)) {
            VS.add(v0);
            VS.add(v1);
        } else {
            VS.add(v1);
            VS.add(v0);
        }

        Msg.debug("Node.calcCCWSortedEdgeList(..): 0: " + v0.edge.descr());
        Msg.debug("Node.calcCCWSortedEdgeList(..): 1: " + v1.edge.descr());

        // Sort vectors in ccw order. I will not move the vector that lies first in VS.
        Msg.debug("...vectors.size()= " + vectors.size());
        for (MyVector vector : vectors) {
            v = vector;

            for (int j = 0; j < VS.size(); j++) {
                v0 = VS.get(j);
                if (j + 1 == VS.size()) {
                    v1 = VS.get(0);
                } else {
                    v1 = VS.get(j + 1);
                }

                if (!v.isCWto(v0) && v.isCWto(v1)) {
                    VS.add(j + 1, v);
                    Msg.debug("Node.calcCCWSortedEdgeList(..):" + (j + 1) + ": " + v.edge.descr());
                    break;
                }
            }
        }

        List<Edge> edges = new ArrayList<>(VS.size());
        for (int i = 0; i < VS.size(); i++) {
            v = VS.get(i);
            edges.add(v.edge);
        }
        return edges;
    }

    /**
     * 获取以当前节点为中心、逆时针排序的相邻节点列表。
     * 如果节点属于三角形则返回null。
     * 
     * @return 逆时针排序的相邻节点数组，或null
     */
    public Node[] ccwSortedNeighbors() {
        Msg.debug("Entering Node.ccwSortedNeighbors(..)");
        Element elem;
        MyVector v, v0, v1;

        // First try to find two boundary edges
        int j = 0;
        MyVector[] b = new MyVector[2];
        for (Edge e : edgeList) {
            if (e.boundaryEdge()) {
                b[j] = e.getVector(this);
                b[j++].edge = e;
                if (j == 2) {
                    break;
                }
            }
        }

        Edge e;
        // If these are found, then v0 is the vector of the most cw edge.
        if (j == 2) {
            elem = b[0].edge.element1;
            e = elem.neighborEdge(this, b[0].edge);
            v1 = e.getVector(this);
            v1.edge = e;

            if (b[0].isCWto(v1)) {
                v0 = b[0];
            } else {
                v0 = b[1]; // that is, the other boundary vector
                elem = b[1].edge.element1;
            }
        } else {
            // Failing to find any boundary edges, we
            // select the vector of an arbitrary edge to be v0.
            // Sets elem to the element that is ccw to v0 around this Node
            e = edgeList.get(0);
            v0 = e.getVector(this);
            v0.edge = e;
            elem = e.element1;
            e = elem.neighborEdge(this, e);
            v1 = e.getVector(this);
            v1.edge = e;

            if (v0.isCWto(v1)) {
                v0 = v0;
            } else {
                v0 = v1;
            }
        }

        // Sort nodes in ccw order starting with otherNode of v0 edge.
        // Uses the fact that elem initially is the element ccw to v0 around this Node.
        Node[] ccwNodeList = new Node[edgeList.size() * 2];
        Element start = elem;
        Quad q;
        e = v0.edge;
        Msg.debug("... 1st node: " + e.otherNode(this).descr());

        int i = 0;
        do {
            ccwNodeList[i++] = e.otherNode(this);
            if (!(elem instanceof Quad)) {
                return null;
            }
            q = (Quad) elem;
            ccwNodeList[i++] = q.oppositeNode(this);
            e = elem.neighborEdge(this, e);
            elem = elem.neighbor(e);
        } while (elem != start && elem != null);

        if (elem == null) {
            ccwNodeList[i++] = e.otherNode(this);
        }

        Msg.debug("Leaving Node.ccwSortedNeighbors(..): # nodes: " + i);
        return ccwNodeList;
    }

    /**
     * 计算与该节点相连的所有边的平均长度。
     * 
     * @return 平均边长
     */
    public double meanNeighborEdgeLength() {
        double sumLengths = 0.0, len, j = 0;

        for (Edge e : edgeList) {

            len = e.length();
            if (len != 0) {
                j++;
                sumLengths += len;
            }
        }
        return sumLengths / j;
    }

    /**
     * 获取与该节点相邻的元素（包括三角形和四边形）的数量。
     * 
     * @return 相邻元素数量
     */
    public int nrOfAdjElements() {
        List<Element> list = adjElements();
        return list.size();
    }

    /**
     * 获取与该节点相邻的所有元素（包括三角形和四边形）。
     * 
     * @return 相邻元素列表
     */
    public List<Element> adjElements() {
        Edge e;
        ArrayList<Element> list = new ArrayList<>();

        for (Edge element : edgeList) {
            e = element;
            if (!list.contains(e.element1)) {
                list.add(e.element1);
            }
            if (e.element2 != null && !list.contains(e.element2)) {
                list.add(e.element2);
            }
        }
        return list;
    }

    /**
     * 获取与该节点相邻的四边形数量。
     * 
     * @return 相邻四边形数量
     */
    public int nrOfAdjQuads() {
        List<Element> list = adjQuads();
        return list.size();
    }

    /**
     * 获取与该节点相邻的所有四边形元素。
     * 
     * @return 相邻四边形元素列表
     */
    public List<Element> adjQuads() {
        Edge e;
        ArrayList<Element> list = new ArrayList<>();

        for (Edge element : edgeList) {
            e = element;
            if (e.element1 instanceof Quad && !list.contains(e.element1)) {
                list.add(e.element1);
            } else if (e.element2 != null && e.element2 instanceof Quad && !list.contains(e.element2)) {
                list.add(e.element2);
            }
        }
        return list;
    }

    /**
     * 获取与该节点相邻的三角形数量。
     * 
     * @return 相邻三角形数量
     */
    public int nrOfAdjTriangles() {
        List<Triangle> list = adjTriangles();
        return list.size();
    }

    /**
     * 获取与该节点相邻的所有三角形元素。
     * 
     * @return 相邻三角形元素列表
     */
    public List<Triangle> adjTriangles() {
        Edge e;
        List<Triangle> list = new ArrayList<>();

        for (int i = 0; i < edgeList.size(); i++) {
            e = edgeList.get(i);
            if (e.element1 instanceof Triangle && !list.contains(e.element1)) {
                list.add((Triangle) e.element1);
            } else if (e.element2 != null && e.element2 instanceof Triangle && !list.contains(e.element2)) {
                list.add((Triangle) e.element2);
            }
        }
        return list;
    }

    /**
     * 对内部节点执行经典的Laplacian平滑操作。
     * 
     * @return 从旧位置到新位置的向量
     */
    public MyVector laplacianMoveVector() {
        MyVector c, cJSum = new MyVector(origin, origin);
        Edge e;
        Node nJ;

        int n = edgeList.size();
        for (int i = 0; i < n; i++) {
            e = edgeList.get(i);
            nJ = e.otherNode(this);
            c = new MyVector(this, nJ);
            cJSum = cJSum.plus(c);
        }
        cJSum = cJSum.div(n);
        return cJSum;
    }

    /**
     * 对内部节点执行经典的Laplacian平滑操作。
     * 
     * @return 新节点的位置
     */
    public Node laplacianSmooth() {
        MyVector c, cJSum = new MyVector(origin, origin);
        Edge e;
        Node nJ;

        int n = edgeList.size();
        for (int i = 0; i < n; i++) {
            e = edgeList.get(i);
            nJ = e.otherNode(this);
            c = new MyVector(this, nJ);
            cJSum = cJSum.plus(c);
        }
        cJSum = cJSum.div(n);
        return new Node(x + cJSum.x, y + cJSum.y);
    }

    /**
     * 对内部节点执行经典的Laplacian平滑操作，但排除指定的邻居节点。
     * 
     * @param node 要排除的邻居节点
     * @return 新节点的位置
     */
    public Node laplacianSmoothExclude(Node node) {
        MyVector c, cJSum = new MyVector(origin, origin);
        Edge e;
        Node nJ;

        int n = edgeList.size();
        for (int i = 0; i < n; i++) {
            e = edgeList.get(i);
            nJ = e.otherNode(this);
            if (nJ != node) {
                c = new MyVector(this, nJ);
                cJSum = cJSum.plus(c);
            }
        }
        cJSum = cJSum.div(n - 1); // -1 because node is excluded
        return new Node(x + cJSum.x, y + cJSum.y);
    }

    /**
     * 对内部节点执行加权Laplacian平滑操作。
     * 
     * @return 新节点的位置
     */
    public Node modifiedLWLaplacianSmooth() {
        Msg.debug("Entering Node.modifiedLWLaplacianSmooth()...");
        Msg.debug("this= " + this.descr());

        Node nJ;
        double cJLengthSum = 0, len;
        Edge e, bEdge1, bEdge2;
        MyVector c, cJLengthMulcJSum = new MyVector(origin, origin), deltaCj, deltaI;
        int n = edgeList.size();
        if (n == 0) {
            Msg.error("...edgeList.size()== 0");
        }
        for (int i = 0; i < n; i++) {
            e = edgeList.get(i);
            Msg.debug("e= " + e.descr());
            nJ = e.otherNode(this);
            c = new MyVector(this, nJ);
            if (nJ.boundaryNode()) {
                bEdge1 = nJ.anotherBoundaryEdge(null);
                bEdge2 = nJ.anotherBoundaryEdge(bEdge1);
                if (bEdge1 == null) {
                    Msg.debug("bEdge1==null");
                } else {
                    Msg.debug("bEdge1: " + bEdge1.descr());
                }
                if (bEdge2 == null) {
                    Msg.debug("bEdge2==null");
                } else {
                    Msg.debug("bEdge2: " + bEdge2.descr());
                }

                // This should be correct:
                deltaCj = nJ.angularSmoothnessAdjustment(this, bEdge1, bEdge2, e.length());
                Msg.debug("c= " + c.descr());
                c = c.plus(deltaCj);
                Msg.debug("c+deltaCj= " + c.descr());
            }

            len = c.length();
            c = c.mul(len);
            cJLengthMulcJSum = cJLengthMulcJSum.plus(c);
            cJLengthSum += len;
        }
        Msg.debug("...cJLengthSum: " + cJLengthSum);
        Msg.debug("...cJLengthMulcJSum: x: " + cJLengthMulcJSum.x + ", y: " + cJLengthMulcJSum.y);

        deltaI = cJLengthMulcJSum.div(cJLengthSum);

        Node node = new Node(x + deltaI.x, y + deltaI.y);
        Msg.debug("Leaving Node.modifiedLWLaplacianSmooth()... returns node= " + node.descr());
        return node;
    }

    /**
     * 统计该节点作为前边的数量。
     * 
     * @return 前边数量
     */
    public int nrOfFrontEdges() {
        int fronts = 0;
        for (Edge e : edgeList) {
            if (e.frontEdge) {
                fronts++;
            }
        }
        return fronts;
    }

    /**
     * Blacker-Stephenson算法的实现，用于节点平滑。
     * 
     * @param nJ     后方节点
     * @param ld     距离
     * @param front1 前/边界邻边1
     * @param front2 前/边界邻边2
     * @return 新节点
     */
    public Node blackerSmooth(Node nJ, Edge front1, Edge front2, double ld) {
        Msg.debug("Entering blackerSmooth(..)...");

        Node nI = this;
        Node origin = new Node(0, 0);
        Node n1, n2, n3, n4;
        Quad q;
        List<Element> adjQuads = adjQuads();

        // Step 1, the isoparametric smooth:
        Msg.debug("...step 1...");
        MyVector vI = new MyVector(origin, nI);
        MyVector vMXsum = new MyVector(origin, origin);
        MyVector vMJ;
        MyVector vMK;
        MyVector vML;

        for (Element adjQuad : adjQuads) {
            q = (Quad) adjQuad;

            n1 = q.edgeList[base].leftNode;
            n2 = q.edgeList[base].rightNode;
            n3 = q.edgeList[left].otherNode(q.edgeList[base].leftNode);
            n4 = q.edgeList[right].otherNode(q.edgeList[base].rightNode);

            // Sorting vMJ, vMK, and vML in ccw order:
            if (nI == n1) {
                vMJ = new MyVector(origin, n2);
                vMK = new MyVector(origin, n4);
                vML = new MyVector(origin, n3);
            } else if (nI == n2) {
                vMJ = new MyVector(origin, n4);
                vMK = new MyVector(origin, n3);
                vML = new MyVector(origin, n1);
            } else if (nI == n3) {
                vMJ = new MyVector(origin, n1);
                vMK = new MyVector(origin, n2);
                vML = new MyVector(origin, n4);
            } else { // if (nI==n4) {
                vMJ = new MyVector(origin, n3);
                vMK = new MyVector(origin, n1);
                vML = new MyVector(origin, n2);
            }

            vMXsum = vMXsum.plus(vMJ);
            vMXsum = vMXsum.plus(vML);
            vMXsum = vMXsum.minus(vMK);
        }

        MyVector vImarked = vMXsum.div(adjQuads.size());
        MyVector deltaA = vImarked.minus(vI);

        if (adjQuads.size() != 2 || nrOfFrontEdges() > 2) {
            Msg.debug("Leaving blackerSmooth(..)...");
            return new Node(x + deltaA.x, y + deltaA.y);
        }
        // Step 2, length adjustment:
        else {
            Msg.debug("...step 2...");
            MyVector vJ = new MyVector(origin, nJ);
            MyVector vIJ = new MyVector(nJ, vImarked.x, vImarked.y);
            double la = vIJ.length();

            MyVector deltaB = deltaA.plus(vI);
            deltaB = deltaB.minus(vJ);
            deltaB = deltaB.mul(ld / la);
            deltaB = deltaB.plus(vJ);
            deltaB = deltaB.minus(vI);

            // Step 3, angular smoothness:
            Msg.debug("...step 3...");
            MyVector deltaC = angularSmoothnessAdjustment(nJ, front1, front2, ld);
            MyVector deltaI = deltaB.plus(deltaC);
            deltaI = deltaI.mul(0.5);
            Msg.debug("Leaving blackerSmooth(..)...");
            return new Node(x + deltaI.x, y + deltaI.y);
        }
    }

    /**
     * 按照Blacker-Stephenson算法进行角度平滑调整。
     * 
     * @param nJ 后方节点
     * @param f1 前/边界邻边1
     * @param f2 前/边界邻边2
     * @param ld 距离
     * @return 应用于该节点的平滑向量
     */
    public MyVector angularSmoothnessAdjustment(Node nJ, Edge f1, Edge f2, double ld) {
        Msg.debug("Entering angularSmoothnessAdjustment(..) ...");
        Node nI = this;
        Msg.debug("nI= " + nI.descr());
        Msg.debug("nJ= " + nJ.descr());

        if (Double.isNaN(ld)) {
            Msg.error("ld is NaN!!!");
        }

        if (f2.length() == 0) {
            Msg.error("f2.length()== 0");
        }

        Msg.debug("f1= " + f1.descr());
        Msg.debug("f2= " + f2.descr());

        Node nIm1 = f1.otherNode(nI);
        Node nIp1 = f2.otherNode(nI);

        Msg.debug("nIp1= " + nIp1.descr());

        if (nIm1.equals(nI)) {
            Msg.error("nIm1.equals(nI)");
        }

        if (nIp1.equals(nI)) {
            Msg.error("nIp1.equals(nI)");
        }

        // if (nIm1.equals(nIp1))
        // this should be okay, in fact...
        // Msg.error("nIm1.equals(nIp1)");

        MyVector pI1 = new MyVector(nJ, nIm1);
        MyVector pI = new MyVector(nJ, nI);
        MyVector pI2 = new MyVector(nJ, nIp1);

        double pI1Angle = pI1.posAngle();
        double pI2Angle = pI2.posAngle();
        double pIp1Angle = Math.max(pI1Angle, pI2Angle);
        double pIm1Angle = Math.min(pI1Angle, pI2Angle);
        double pIAngle = pI.posAngle();
        double pIm1p1Angle = 0;
        if (pIAngle < pIm1Angle || pIAngle > pIp1Angle) {
            pIm1p1Angle = PIx2 - pIp1Angle + pIm1Angle;
        } else {
            pIm1p1Angle = pIp1Angle - pIm1Angle;
        }

        Msg.debug("pIAngle= " + Math.toDegrees(pIAngle));
        Msg.debug("pIp1Angle= " + Math.toDegrees(pIp1Angle));
        Msg.debug("pIm1Angle= " + Math.toDegrees(pIm1Angle));

        // Check if the sum of angles between pIp1 and pI and the angle between pIm1 and
        // PI is greater or equal to 180 degrees. If so, I choose ld as the length of
        // pB2.
        if (pIm1p1Angle > Math.PI) {
            Msg.debug("okei, we're in there..");
            double pB1Angle = pIm1p1Angle * 0.5 + pIp1Angle;
            if (pB1Angle >= PIx2) {
                pB1Angle = Math.IEEEremainder(pB1Angle, PIx2);
            }
            Msg.debug("pB1Angle= " + Math.toDegrees(pB1Angle));
            double pB1pIMax = Math.max(pB1Angle, pIAngle);
            double pB1pIMin = Math.min(pB1Angle, pIAngle);
            Msg.debug("pB1pIMax= " + Math.toDegrees(pB1pIMax));
            Msg.debug("pB1pIMin= " + Math.toDegrees(pB1pIMin));
            double pB2Angle = pB1pIMin + 0.5 * (pB1pIMax - pB1pIMin);
            if (pB1pIMax - pB1pIMin > Math.PI) {
                pB2Angle += Math.PI;
            }

            MyVector pB2 = new MyVector(pB2Angle, ld, nJ);
            MyVector deltaC = pB2.minus(pI);
            Msg.debug("Leaving angularSmoothnessAdjustment(..) returns " + deltaC.descr());
            return deltaC;
        }

        Msg.debug("pI1= " + pI1.descr());
        Msg.debug("pI2= " + pI2.descr());

        MyVector line = new MyVector(nIp1, nIm1);
        Msg.debug("line= " + line.descr());

        // pB1 should be the halved angle between pIp1 and pIm1, in the direction of pI:
        double pB1Angle = pIm1Angle + 0.5 * (pIp1Angle - pIm1Angle);
        if (pIp1Angle - pIm1Angle > Math.PI) {
            pB1Angle += Math.PI;
        }

        if (Double.isNaN(pB1Angle)) {
            Msg.error("pB1Angle is NaN!!!");
        }
        Msg.debug("pB1Angle= " + Math.toDegrees(pB1Angle));

        double pB1pIMax = Math.max(pB1Angle, pIAngle);
        double pB1pIMin = Math.min(pB1Angle, pIAngle);
        Msg.debug("pB1pIMax= " + Math.toDegrees(pB1pIMax));
        Msg.debug("pB1pIMin= " + Math.toDegrees(pB1pIMin));

        double pB2Angle = pB1pIMin + 0.5 * (pB1pIMax - pB1pIMin);
        if (pB1pIMax - pB1pIMin > Math.PI) {
            pB2Angle += Math.PI;
        }

        if (Double.isNaN(pB2Angle)) {
            Msg.error("pB2Angle is NaN!!!");
        }
        Msg.debug("pB2Angle= " + Math.toDegrees(pB2Angle));

        Ray pB2Ray = new Ray(nJ, pB2Angle);
        // MyVector pB2= new MyVector(pB2Angle, 100.0, nJ);

        Msg.debug("pB2Ray= " + pB2Ray.descr());
        Msg.debug("pB2Ray= " + pB2Ray.values());
        Node q = pB2Ray.pointIntersectsAt(line);
        double lq = q.length(nJ);
        if (Double.isNaN(lq)) {
            Msg.error("lq is NaN!!!");
        }

        MyVector pB2;
        if (ld > lq) {
            pB2 = new MyVector(pB2Ray, (lq + ld) * 0.5);
            // pB2.setLengthAndAngle((lq+ld)*0.5, pB2Angle);
        } else {
            pB2 = new MyVector(pB2Ray, ld);
            // pB2.setLengthAndAngle(ld, pB2Angle);
        }

        MyVector deltaC = pB2.minus(pI);
        Msg.debug("Leaving angularSmoothnessAdjustment(..) returns " + deltaC.descr());
        return deltaC;
    }

    /**
     * 检查节点移动后，是否有相邻元素变为反转或面积为零。
     * 
     * @param elements 要检查的元素列表
     * @return 存在反转或零面积元素返回true，否则返回false
     */
    public boolean invertedOrZeroAreaElements(List<Element> elements) {
        for (Element elem : elements) {
            if (elem.invertedOrZeroArea()) {
                Msg.debug("Node.invertedOrZeroAreaElements(..): invertedOrZeroArea: " + elem.descr());
                return true;
            }
        }
        return false;
    }

    /**
     * 增量调整节点位置，直到所有相邻元素都不是反转或零面积。
     * 
     * @param old      原始节点
     * @param elements 相邻元素列表
     * @return 成功返回true，否则返回false
     */
    public boolean incrAdjustUntilNotInvertedOrZeroArea(Node old, List<Element> elements) {
        Msg.debug("Entering incrAdjustUntilNotInvertedOrZeroArea(..)");
        Msg.debug("..this: " + descr());
        Msg.debug("..old: " + old.descr());

        MyVector back = new MyVector(this, old);
        double startX = x, startY = y;
        double xstep = back.x / 50.0, ystep = back.y / 50.0;
        double xinc, yinc;
        int steps, i;

        if (Math.abs(xstep) < COINCTOL || Math.abs(ystep) < COINCTOL) {

            if (COINCTOL < Math.abs(back.x) && COINCTOL < Math.abs(back.y)) {// && or || ?
                Msg.debug("...ok, resorting to use of minimum increment");
                if (Math.abs(back.x) < Math.abs(back.y)) {
                    if (back.x < 0) {
                        xinc = -COINCTOL;
                    } else {
                        xinc = COINCTOL;
                    }

                    yinc = Math.abs(old.y) * COINCTOL / Math.abs(old.x);
                    if (back.y < 0) {
                        yinc = -yinc;
                    }

                    steps = (int) (back.x / xinc);
                } else {
                    if (back.y < 0) {
                        yinc = -COINCTOL;
                    } else {
                        yinc = COINCTOL;
                    }

                    xinc = Math.abs(old.x) * COINCTOL / Math.abs(old.y);
                    if (back.x < 0) {
                        xinc = -xinc;
                    }

                    steps = (int) (back.y / yinc);
                }

                Msg.debug("...back.x is: " + back.x);
                Msg.debug("...back.y is: " + back.y);

                Msg.debug("...xinc is: " + xinc);
                Msg.debug("...yinc is: " + yinc);

                for (i = 1; i <= steps; i++) {
                    x = startX + xinc * i;
                    y = startY + yinc * i;

                    if (!invertedOrZeroAreaElements(elements)) {
                        Msg.debug("Leaving incrAdjustUntilNotInvertedOrZeroArea(..)");
                        return true;
                    }
                }
            }
        } else {
            for (i = 1; i <= 49; i++) {
                x = startX + back.x * i / 50.0;
                y = startY + back.y * i / 50.0;

                if (!invertedOrZeroAreaElements(elements)) {
                    Msg.debug("Leaving incrAdjustUntilNotInvertedOrZeroArea(..)");
                    return true;
                }
            }
        }

        x = old.x;
        y = old.y;
        if (!invertedOrZeroAreaElements(elements)) {
            Msg.debug("Leaving incrAdjustUntilNotInvertedOrZeroArea(..)");
            return true;
        }

        Msg.debug("Leaving incrAdjustUntilNotInvertedOrZeroArea(..)");
        return false;
    }

    /** @return true if the node is part of the boundary of the mesh. */
    public boolean boundaryNode() {
        for (Edge e : edgeList) {
            if (e.boundaryEdge()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the node is part of the boundary of the mesh or a triangle.
     */
    public boolean boundaryOrTriangleNode() {
        for (Edge e : edgeList) {
            if (e.boundaryOrTriangleEdge()) {
                return true;
            }
        }
        return false;
    }

    /** @return true if the node is truely a part of the front. */
    public boolean frontNode() {
        for (Edge e : edgeList) {
            if (e.isFrontEdge()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param known is the front edge that is already known. (Use null if no such
     *              edge is known.)
     * @return a front edge found in this node's edgelist.
     */
    public Edge anotherFrontEdge(Edge known) {
        for (Edge e : edgeList) {
            if (e != known && e.isFrontEdge()) {
                return e;
            }
        }
        return null;
    }

    /**
     * @param known is the boundary edge that is already known. (Use null if no such
     *              edge is known.)
     * @return a boundary edge found in this node's edgelist.
     */
    public Edge anotherBoundaryEdge(Edge known) {
        for (Edge e : edgeList) {
            if (e != known && e.boundaryEdge()) {
                return e;
            }
        }
        return null;
    }

    /**
     * 计算当前节点到另一个节点的距离。
     * 
     * @param n 目标节点
     * @return 距离
     */
    public double length(Node n) {
        double xDiff = x - n.x;
        double yDiff = y - n.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * 计算当前节点到指定坐标的距离。
     * 
     * @param x 目标x坐标
     * @param y 目标y坐标
     * @return 距离
     */
    public double length(double x, double y) {
        double xDiff = this.x - x;
        double yDiff = this.y - y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * 判断该节点是否在指定边所在的直线上。
     * 
     * @param e 边
     * @return 在直线上返回true，否则返回false
     */
    public boolean onLine(Edge e) {
        BigDecimal x1 = new BigDecimal(e.leftNode.x);
        BigDecimal y1 = new BigDecimal(e.leftNode.y);
        BigDecimal x2 = new BigDecimal(e.rightNode.x);
        BigDecimal y2 = new BigDecimal(e.rightNode.y);
        BigDecimal x3 = new BigDecimal(x);
        BigDecimal y3 = new BigDecimal(y);

        BigDecimal zero = new BigDecimal(0.0);
        BigDecimal l_cross_r = (x1.multiply(y2)).subtract(x2.multiply(y1));
        BigDecimal xdiff = x1.subtract(x2);
        BigDecimal ydiff = y1.subtract(y2);
        BigDecimal det1 = l_cross_r.subtract(xdiff.multiply(y3)).add(ydiff.multiply(x3));

        int eval1 = det1.compareTo(zero);
        if (eval1 == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断该节点是否在由三角形和边定义的半平面内。
     * 
     * @param t 三角形
     * @param e 边
     * @return 1在半平面内，0在边上，-1在另一侧
     */
    public int inHalfplane(Triangle t, Edge e) {
        return inHalfplane(e.leftNode, e.rightNode, t.oppositeOfEdge(e));
    }

    // @return 1 if this Node is on the same side of Edge e as Node n is,
    // 0 if this Node is on the line that extends Edge e, and
    // -1 if this Node is on the other side of Edge e than Node n is.
    public int inHalfplane(Edge e, Node n) {
        return inHalfplane(e.leftNode, e.rightNode, n);
    }

    // @return 1 if this Node is on the same side of the line (l1, l2) as Node n is,
    // 0 if this Node is on the line that extends line (l1, l2), and
    // -1 if this Node is on the other side of line (l1, l2) than Node n is.
    public int inHalfplane(Node l1, Node l2, Node n) {
        Msg.debug("Entering Node.inHalfplane(..)");
        Msg.debug("l1: " + l1.descr() + ", l2: " + l2.descr() + ", n:" + n.descr());
        BigDecimal x1 = new BigDecimal(l1.x);
        BigDecimal y1 = new BigDecimal(l1.y);
        BigDecimal x2 = new BigDecimal(l2.x);
        BigDecimal y2 = new BigDecimal(l2.y);
        BigDecimal x3 = new BigDecimal(x);
        BigDecimal y3 = new BigDecimal(y);
        BigDecimal x4 = new BigDecimal(n.x);
        BigDecimal y4 = new BigDecimal(n.y);

        BigDecimal zero = new BigDecimal(0.0);
        BigDecimal l_cross_r = (x1.multiply(y2)).subtract(x2.multiply(y1));
        BigDecimal xdiff = x1.subtract(x2);
        BigDecimal ydiff = y1.subtract(y2);
        BigDecimal det1 = l_cross_r.subtract(xdiff.multiply(y3)).add(ydiff.multiply(x3));

        int eval1 = det1.compareTo(zero);
        if (eval1 == 0) {
            Msg.debug("Leaving Node.inHalfplane(..)");
            return 0;
        }

        BigDecimal det2 = l_cross_r.subtract(xdiff.multiply(y4)).add(ydiff.multiply(x4));
        int eval2 = det2.compareTo(zero);
        Msg.debug("Leaving Node.inHalfplane(..)");
        if ((eval1 < 0 && eval2 < 0) || (eval1 > 0 && eval2 > 0)) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 判断该节点是否在由边的法线限定的有界平面内。
     * 
     * @param e 边
     * @return 在有界平面内返回true，否则返回false
     */
    public boolean inBoundedPlane(Edge e) {
        Edge normal1 = e.unitNormalAt(e.leftNode);
        Edge normal2 = e.unitNormalAt(e.rightNode);

        int a = inHalfplane(normal1, e.rightNode);
        int b = inHalfplane(normal2, e.leftNode);

        Msg.debug("Node.inBoundedPlane(..): a: " + a + ", b: " + b);

        if ((a == 1 || a == 0) && (b == 1 || b == 0)) {
            Msg.debug("Node.inBoundedPlane(..): returns true");
            return true;
        } else {
            Msg.debug("Node.inBoundedPlane(..): returns false");
            return false;
        }
    }

    /**
     * 判断该节点是否在由p1、p2、p3三点确定的圆的内部。
     * 
     * @param p1 圆上一点
     * @param p2 圆上一点
     * @param p3 圆上一点
     * @return 在圆内返回true，否则返回false
     */
    public boolean inCircle(Node p1, Node p2, Node p3) {
        Msg.debug("Entering inCircle(..)");

        /*
         * a * b = a_1*b_1 + a_2*b_2 Scalar product a x b = (a_1*b_2 - b_1*a_2) Vector
         * product
         *
         * e_3 is the unit vector (0,0,1). All the points are defined in 3D space, with
         * z-values equal to 0. The v_i vectors are unit vectors. The points are ccw
         * ordered.
         *
         * sin(alpha) = (v_1 x v_2) * e_3= x3y1 -x3y2 -x2y1 -y3x1 +y3x2 +y2x1 sin(beta)
         * = (v_3 x v_4) * e_3= y3x1 -x1y4 -x4y3 -x3y1 +y1x4 +y4x3 cos(alpha) = v_1 *
         * v_2 = (x3 -x2)(x1 -x2) +(y3 -y2)(y1 -y2) cos(beta) = v_3 * v_4 = (x1 -x4)(x3
         * -x4) +(y1 -y4)(y3 -y4)
         *
         */

        double cosAlpha = (p3.x - p2.x) * (p1.x - p2.x) + (p3.y - p2.y) * (p1.y - p2.y);
        double cosBeta = (p1.x - x) * (p3.x - x) + (p1.y - y) * (p3.y - y);

        if (cosAlpha < 0 && cosBeta < 0) { // (if both angles > than 90 degrees)
            Msg.debug("Leaving inCircle(..), cosAlpha && cosBeta <0, return true");
            return true;
        } else if (cosAlpha > 0 && cosBeta > 0) { // (if both angles < than 90 degrees)
            Msg.debug("Leaving inCircle(..), cosAlpha && cosBeta >0, return false");
            return false;
        } else {
            double sinAlpha = p3.x * p1.y - p3.x * p2.y - p2.x * p1.y - p3.y * p1.x + p3.y * p2.x + p2.y * p1.x;
            double sinBeta = p3.y * p1.x - p1.x * y - x * p3.y - p3.x * p1.y + p1.y * x + y * p3.x;
            if (cosAlpha * sinBeta + sinAlpha * cosBeta < 0) {
                Msg.debug("Leaving inCircle(..), passed last check, returns true");
                return true;
            } else {
                Msg.debug("Leaving inCircle(..), failed last check, returns false");
                return false;
            }
        }
    }

    /**
     * 合并另一个节点的边到当前节点（假设两节点位置相同）。
     * 
     * @param n 另一个节点
     */
    public void merge(Node n) {
        Node oldN = n.copyXY();
        Edge e;
        int ind;
        n.setXY(this);
        for (int i = 0; i < n.edgeList.size(); i++) {
            e = n.edgeList.get(i);
            ind = edgeList.indexOf(e);
            if (ind == -1) {
                e.replaceNode(n, this);
                edgeList.add(e);
            } else { // collapsed edges must be removed
                if (e.leftNode == e.rightNode) {
                    edgeList.remove(ind);
                }
            }
        }
        n.setXY(oldN);
    }

    /**
     * 获取该节点的所有前边列表。
     * 
     * @return 前边列表
     */
    public List<Edge> frontEdgeList() {
        List<Edge> list = new ArrayList<>();
        Edge e;
        for (int i = 0; i < edgeList.size(); i++) {
            e = edgeList.get(i);
            if (e.frontEdge) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 判断该节点是否包含指定的边。
     * 
     * @param e 边
     * @return 包含返回true，否则返回false
     */
    public boolean hasEdge(Edge e) {
        for (Edge curEdge : edgeList) {
            if (e == curEdge) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取该节点的价（相连边数，特殊处理边界节点）。
     * 
     * @return 节点价
     */
    public byte valence() {
        byte temp = (byte) edgeList.size();
        if (!boundaryNode()) {
            return temp;
        } else {
            Edge b1 = anotherBoundaryEdge(null);
            Edge b2 = anotherBoundaryEdge(b1);
            double ang = b1.sumAngle(b1.element1, this, b2);

            // Determine which kind of boundary node we're dealing with
            if (ang <= PIx3div4) { // PIdiv2
                return (byte) (temp + 2);
            } else if (ang < PIx5div4) { // PIx3div2
                return (byte) (temp + 1);
            } else { // PIx3div2
                return (temp);
            }

            /*
             * if (Math.abs(ang- PIdiv2) < Math.abs(ang- Math.PI)) return (byte)(temp+2);
             * else if (Math.abs(ang- Math.PI) < Math.abs(ang- PIx2)) return (byte)(temp+1);
             * else return temp;
             */
        }
    }

    /**
     * 计算该节点及其邻居的价模式。
     * 
     * @param ccwNodes 逆时针排序的邻居节点
     */
    public void createValencePattern(Node[] ccwNodes) {
        Msg.debug("Entering Node.createValencePattern(..)");
        int j = edgeList.size() * 2;
        if (j >= 128) {
            Msg.error("Number of edges adjacent node " + descr() + " was greater than expected (" + edgeList.size()
                    + "-2 >= 64)");
        }
        byte ccwNodesSize = (byte) j;
        pattern = new byte[ccwNodesSize + 2]; // +2 for size and c.valence()
        pattern[0] = (byte) (ccwNodesSize + 2);
        pattern[1] = valence();

        for (int i = 0; i < ccwNodesSize; i++) {
            pattern[i + 2] = ccwNodes[i].valence();
        }
        Msg.debug("Leaving Node.createValencePattern(..)");
    }

    /**
     * 计算该节点及其邻居的价模式。
     * 
     * @param ccwNodesSize 邻居节点数量
     * @param ccwNodes     逆时针排序的邻居节点
     */
    public void createValencePattern(byte ccwNodesSize, Node[] ccwNodes) {
        Msg.debug("Entering Node.createValencePattern(" + ccwNodesSize + ", Node [])");
        pattern = new byte[ccwNodesSize + 2]; // +2 for size and c.valence()
        pattern[0] = (byte) (ccwNodesSize + 2);
        pattern[1] = valence();

        for (int i = 0; i < ccwNodesSize; i++) {
            Msg.debug("...i== " + i);
            pattern[i + 2] = ccwNodes[i].valence();
        }
        Msg.debug("Leaving Node.createValencePattern(byte, Node [])");
    }

    /**
     * 获取价模式中不规则节点的数量（价不等于4）。
     * 
     * @return 不规则节点数量
     */
    public int irregNeighborNodes() {
        int count = 0;
        for (int i = 1; i < pattern[0]; i++) {
            if (pattern[i] != 4) {
                count++;
            }
        }
        return count;
    }

    /**
     * 比较该节点的价模式与给定模式是否匹配。
     * 
     * @param pattern2 目标价模式
     * @return 匹配返回起始索引，不匹配返回-1
     */
    public int patternMatch(byte[] pattern2) {
        Msg.debug("Entering patternMatch(..)");
        if (pattern[0] != pattern2[0] || pattern[1] != pattern2[1]) {
            Msg.debug("Leaving patternMatch(..): mismatch");
            return -1; // Different length or different valence of central node
        }

        int i, j, jstart = 2, matches = 0;

        Msg.debug("pattern[0]==" + pattern[0]);
        Msg.debug("pattern2[0]==" + pattern2[0]);
        Msg.debug("pattern[1]==" + pattern[1]);
        Msg.debug("pattern2[1]==" + pattern2[1]);

        while (jstart < pattern[0]) {
            // Find index of next valence in pattern2 that matches valence of pattern[2]
            for (j = jstart; j < pattern[0]; j += 2) {
                if (pattern[j] == 2 && (pattern2[2] == 2 || pattern2[2] == 14 || pattern2[2] == 0)) {
                    matches = 1;
                    jstart = j;
                    Msg.debug("... rolling pattern...");
                    Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                    break;
                } else if (pattern[j] == 3 && (pattern2[2] == 3 || pattern2[2] == 14 || pattern2[2] == 0)) {

                    matches = 1;
                    jstart = j;
                    Msg.debug("... rolling pattern...");
                    Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                    break;
                } else if (pattern[j] == 4
                        && (pattern2[2] == 4 || pattern2[2] == 14 || pattern2[2] == 24 || pattern2[2] == 0)) {

                    matches = 1;
                    jstart = j;
                    Msg.debug("... rolling pattern...");
                    Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                    break;
                } else if (pattern[j] >= 5 && (pattern2[2] == 5 || pattern2[2] == 24 || pattern2[2] == 0)) {

                    matches = 1;
                    jstart = j;
                    Msg.debug("... rolling pattern...");
                    Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                    break;
                }
            }

            if (matches == 0) {
                Msg.debug("Leaving patternMatch(..): mismatch");
                return -1; // Search completed, patterns don't match
            }

            if (jstart == pattern[0] - 1) {
                j = 1;
            } else {
                j = jstart + 1;
            }
            // Count nr of sequential matches starting at this index
            for (i = 3; matches < pattern2[0] - 2; i++, j++) {
                if (pattern[j] == 2 && (pattern2[i] == 2 || pattern2[i] == 14 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] == 3 && (pattern2[i] == 3 || pattern2[i] == 14 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] == 4
                        && (pattern2[i] == 4 || pattern2[i] == 14 || pattern2[i] == 24 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] >= 5 && (pattern2[i] == 5 || pattern2[i] == 24 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else {
                    matches = 0;
                    break;
                }

                /*
                 * if (pattern2[i]!= pattern[j] && pattern2[i]!= 0 && pattern[j]!= 0) { matches=
                 * 0; break; } else { matches++; Msg.debug("...pattern2["+i+"]: "+pattern2[i]+
                 * ", pattern["+j+"]: "+pattern[j]); }
                 */

                if (j == pattern[0] - 1) {
                    j = 1;
                }
            }
            if (matches == pattern2[0] - 2) {
                Msg.debug("Leaving patternMatch(..): match, returns: " + jstart);
                return jstart; // Search completed, patterns match
            }
            jstart += 2;
        }
        Msg.debug("Leaving patternMatch(..): mismatch");
        return -1;
    }

    /**
     * 比较该节点的价模式与给定模式是否匹配，并检查顶点模式。
     * 
     * @param pattern2  目标价模式
     * @param vertexPat 顶点布尔模式
     * @param angles    角度数组
     * @return 匹配返回起始索引，不匹配返回-1
     */
    public int patternMatch(byte[] pattern2, boolean[] vertexPat, double[] angles) {
        Msg.debug("Entering patternMatch(byte [], boolean [], double [])");
        if (pattern[0] != pattern2[0] || pattern[1] != pattern2[1]) {
            Msg.debug("Leaving patternMatch(byte [], boolean [], double []): mismatch");
            return -1; // Different length or different valence of central node
        }

        int i, j, jstart = 2, matches = 0;

        Msg.debug("pattern[0]==" + pattern[0]);
        Msg.debug("pattern2[0]==" + pattern2[0]);
        Msg.debug("pattern[1]==" + pattern[1]);
        Msg.debug("pattern2[1]==" + pattern2[1]);

        while (jstart < pattern[0]) {
            // Find index of next valence in pattern2 that matches valence of pattern[2]
            for (j = jstart; j < pattern[0]; j += 2) {
                if (pattern[j] == 2 && (pattern2[2] == 2 || pattern2[2] == 14 || pattern2[2] == 0)) {
                    if (fitsVertexPat((byte) (j - 2), angles, vertexPat, pattern[0] - 2)) {
                        matches = 1;
                        jstart = j;
                        Msg.debug("... rolling pattern...");
                        Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                        break;
                    }
                } else if (pattern[j] == 3 && (pattern2[2] == 3 || pattern2[2] == 14 || pattern2[2] == 0)) {
                    if (fitsVertexPat((byte) (j - 2), angles, vertexPat, pattern[0] - 2)) {
                        matches = 1;
                        jstart = j;
                        Msg.debug("... rolling pattern...");
                        Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                        break;
                    }
                } else if (pattern[j] == 4
                        && (pattern2[2] == 4 || pattern2[2] == 14 || pattern2[2] == 24 || pattern2[2] == 0)) {
                    if (fitsVertexPat((byte) (j - 2), angles, vertexPat, pattern[0] - 2)) {
                        matches = 1;
                        jstart = j;
                        Msg.debug("... rolling pattern...");
                        Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                        break;
                    }
                } else if (pattern[j] >= 5 && (pattern2[2] == 5 || pattern2[2] == 24 || pattern2[2] == 0)) {
                    if (fitsVertexPat((byte) (j - 2), angles, vertexPat, pattern[0] - 2)) {
                        matches = 1;
                        jstart = j;
                        Msg.debug("... rolling pattern...");
                        Msg.debug("...pattern2[2]: " + pattern2[2] + ", pattern[" + j + "]: " + pattern[j]);
                        break;
                    }
                }
            }

            if (matches == 0) {
                Msg.debug("Leaving patternMatch(byte [], boolean [], double []): mismatch");
                return -1; // Search completed, patterns don't match
            }
            Msg.debug("...broken out of loop!!");
            if (jstart == pattern[0] - 1) {
                j = 1; // Shouldn't it be 2???
            } else {
                j = jstart + 1;
            }
            // Count nr of sequential matches starting at this index
            for (i = 3; matches < pattern2[0] - 2; i++, j++) {
                Msg.debug("i== " + i);
                if (pattern[j] == 2 && (pattern2[i] == 2 || pattern2[i] == 14 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] == 3 && (pattern2[i] == 3 || pattern2[i] == 14 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] == 4
                        && (pattern2[i] == 4 || pattern2[i] == 14 || pattern2[i] == 24 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] >= 5 && (pattern2[i] == 5 || pattern2[i] == 24 || pattern2[i] == 0)) {
                    matches++;
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else {
                    matches = 0;
                    break;
                }

                if (j == pattern[0] - 1) {
                    j = 1;
                }
            }
            if (matches == pattern2[0] - 2) {
                Msg.debug("Leaving patternMatch(byte [], boolean [], double []): match, returns: " + jstart);
                return jstart; // Search completed, patterns match
            }
            jstart += 2;
        }
        Msg.debug("Leaving patternMatch(byte [], boolean [], double []): mismatch");
        return -1;
    }

    /**
     * 检查给定角度数组和顶点布尔模式是否匹配。
     * 
     * @param start     起始索引
     * @param ang       角度数组
     * @param vertexPat 顶点布尔模式
     * @param len       长度
     * @return 匹配返回true，否则返回false
     */
    public boolean fitsVertexPat(byte start, double[] ang, boolean[] vertexPat, int len) {
        Msg.debug("Entering Node.fitsVertexPat(..)");
        int i, j = start, k = 0, l;
        do {
            // Check the corresponding boolean in vertexPat
            if (vertexPat[k]) {
                // Compare ang[j] to all other angles at non-vertex nodes
                i = j + 1;
                if (i == len) {
                    i = 0;
                }
                l = k + 1;
                if (l == len) {
                    l = 0;
                }

                while (i != j) {
                    if (!vertexPat[l] && ang[i] < ang[j]) {
                        Msg.debug("ang[" + i + "] < ang[" + j + "]");
                        Msg.debug("ang[" + i + "]== " + Math.toDegrees(ang[i]));
                        Msg.debug("ang[" + j + "]== " + Math.toDegrees(ang[j]));
                        Msg.debug("Leaving Node.fitsVertexPat(..): false");
                        return false;
                    }
                    i++;
                    if (i == len) {
                        i = 0;
                    }
                    l++;
                    if (l == len) {
                        l = 0;
                    }
                }
            }

            j++;
            if (j == len) {
                j = 0;
            }
            k++;
        } while (j != start);

        Msg.debug("Leaving Node.fitsVertexPat(..): true");
        return true;
    }

    /**
     * 填充角度数组，获取相邻节点的对顶角。
     * 
     * @param ccwNeighbors 逆时针排序的邻居节点
     * @param len          长度
     * @return 角度数组
     */
    public double[] surroundingAngles(Node[] ccwNeighbors, int len) {
        Msg.debug("Entering Node.surroundingAngles(..)");
        Quad q, qa, qb;
        Edge e, ep, en;
        Node n, np, nn, no;
        double[] angles = new double[len];
        for (int i = 0; i < len; i++) {
            n = ccwNeighbors[i];
            e = commonEdge(n);
            if (e == null) {
                if (i - 1 >= 0) {
                    np = ccwNeighbors[i - 1];
                } else {
                    np = ccwNeighbors[len - 1];
                }

                if (i + 1 < len) {
                    nn = ccwNeighbors[i + 1];
                } else {
                    nn = ccwNeighbors[0];
                }

                ep = commonEdge(np);
                en = commonEdge(nn);
                q = (Quad) ep.commonElement(en);

                no = q.oppositeNode(this);
                angles[i] = q.ang[q.angleIndex(no)];
            } else {
                no = e.otherNode(this);
                qa = (Quad) e.element1;
                qb = (Quad) e.element2;

                angles[i] = qa.ang[qa.angleIndex(no)] + qb.ang[qb.angleIndex(no)];
            }
        }
        Msg.debug("Leaving Node.surroundingAngles(..)");
        return angles;
    }

    /**
     * 比较该边界节点的价模式与给定模式是否匹配。
     * 
     * @param pattern2     目标价模式
     * @param bpat         边界布尔模式
     * @param ccwNeighbors 逆时针排序的邻居节点
     * @return 匹配返回true，否则返回false
     */
    public boolean boundaryPatternMatch(byte[] pattern2, boolean[] bpat, Node[] ccwNeighbors) {
        Msg.debug("Entering boundaryPatternMatch(..)");

        if (pattern[0] != pattern2[0] || pattern[1] != pattern2[1] || bpat[0] != boundaryNode()) {
            Msg.debug("Leaving boundaryPatternMatch(..): mismatch");
            return false;
        }
        int i;

        for (i = 2; i < pattern[0]; i++) {
            if (pattern[i] == 2 && (pattern2[i] == 2 || pattern2[i] == 14 || pattern2[i] == 0)) {
                if (bpat[i - 1] && !ccwNeighbors[i - 2].boundaryNode()) {
                    return false;
                }

                Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + i + "]: " + pattern[i]);
            } else if (pattern[i] == 3 && (pattern2[i] == 3 || pattern2[i] == 14 || pattern2[i] == 0)) {
                if (bpat[i - 1] && !ccwNeighbors[i - 2].boundaryNode()) {
                    return false;
                }
                Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + i + "]: " + pattern[i]);
            } else if (pattern[i] == 4
                    && (pattern2[i] == 4 || pattern2[i] == 14 || pattern2[i] == 24 || pattern2[i] == 0)) {
                if (bpat[i - 1] && !ccwNeighbors[i - 2].boundaryNode()) {
                    return false;
                }
                Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + i + "]: " + pattern[i]);
            } else if (pattern[i] >= 5 && (pattern2[i] == 5 || pattern2[i] == 24 || pattern2[i] == 0)) {
                if (bpat[i - 1] && !ccwNeighbors[i - 2].boundaryNode()) {
                    return false;
                }
                Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + i + "]: " + pattern[i]);
            } else {
                Msg.debug("Leaving boundaryPatternMatch(..): mismatch");
                return false;
            }
        }
        Msg.debug("Leaving boundaryPatternMatch(..): match");
        return true;
    }

    /**
     * 比较该内部节点的价模式与给定模式是否匹配，并检查边界模式。
     * 
     * @param pattern2     目标价模式
     * @param bpat         边界布尔模式
     * @param ccwNeighbors 逆时针排序的邻居节点
     * @return 匹配返回起始索引，不匹配返回-1
     */
    public int boundaryPatternMatchSpecial(byte[] pattern2, boolean[] bpat, Node[] ccwNeighbors) {

        Msg.debug("Entering boundaryPatternMatchSpecial(..)");

        if (pattern[0] != pattern2[0] || pattern[1] != pattern2[1] || bpat[0] != boundaryNode()) {
            Msg.debug("Leaving boundaryPatternMatchSpecial(..): mismatch");
            return -1;
        }
        int i, j, k;
        boolean match;

        Msg.debug("...entering the for loop");

        for (k = 2; k < pattern[0]; k++) {

            Msg.debug("...k== " + k);
            j = k;
            match = true;

            for (i = 2; i < pattern[0]; i++) {
                Msg.debug("...i== " + i);

                Msg.debug("...pattern[" + j + "]== " + pattern[j]);
                Msg.debug("...pattern2[" + i + "]== " + pattern2[i]);

                if (pattern[j] == 2 && (pattern2[i] == 2 || pattern2[i] == 14 || pattern2[i] == 0)) {
                    if (bpat[i - 1] && !ccwNeighbors[j - 2].boundaryNode()) {
                        match = false;
                        break;
                    }
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] == 3 && (pattern2[i] == 3 || pattern2[i] == 14 || pattern2[i] == 0)) {
                    if (bpat[i - 1] && !ccwNeighbors[j - 2].boundaryNode()) {
                        match = false;
                        break;
                    }
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] == 4
                        && (pattern2[i] == 4 || pattern2[i] == 14 || pattern2[i] == 24 || pattern2[i] == 0)) {
                    if (bpat[i - 1] && !ccwNeighbors[j - 2].boundaryNode()) {
                        match = false;
                        break;
                    }
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else if (pattern[j] >= 5 && (pattern2[i] == 5 || pattern2[i] == 24 || pattern2[i] == 0)) {
                    if (bpat[i - 1]) {
                        Msg.debug("bpat[" + (i - 1) + "] is true");
                    } else {
                        Msg.debug("bpat[" + (i - 1) + "] is false");
                    }

                    if (ccwNeighbors[j - 2].boundaryNode()) {
                        Msg.debug("ccwNeighbors[" + (j - 2) + "].boundaryNode()]) is true");
                    } else {
                        Msg.debug("ccwNeighbors[" + (j - 2) + "].boundaryNode()]) is false");
                    }

                    if (bpat[i - 1] && !ccwNeighbors[j - 2].boundaryNode()) {
                        match = false;
                        break;
                    }
                    Msg.debug("...pattern2[" + i + "]: " + pattern2[i] + ", pattern[" + j + "]: " + pattern[j]);
                } else {
                    match = false;
                    break;
                }

                j++;
                if (j == pattern[0]) {
                    j = 2;
                }
            }
            if (match) {
                Msg.debug("Leaving boundaryPatternMatchSpecial(..): match");
                return k;
            }
        }
        Msg.debug("Leaving boundaryPatternMatchSpecial(..): mismatch");
        return -1;
    }

    /**
     * 获取与指定节点相连的公共边。
     * 
     * @param n 目标节点
     * @return 公共边，若无则返回null
     */
    public Edge commonEdge(Node n) {
        Node other;
        for (Edge e : edgeList) {
            other = e.otherNode(this);
            if (other == n) {
                return e;
            }
        }
        return null;
    }

    /**
     * 用标准网格替换该节点（预留方法）。
     * 
     * @return 总是返回true
     */
    public boolean replaceWithStdMesh() {
        Msg.debug("Entering replaceWithStdMesh(..)");
        Msg.debug("Leaving replaceWithStdMesh(..)");
        return true;
    }

    /**
     * 获取节点的字符串描述。
     * 
     * @return 节点描述字符串
     */
    public String descr() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * 获取节点的价模式描述字符串。
     * 
     * @return 价模式描述字符串
     */
    public String valDescr() {
        String s = "" + pattern[1] + "-";
        for (int i = 2; i < pattern[0]; i++) {
            s = s + pattern[i];
        }

        return s;
    }

    /**
     * 打印节点的字符串描述。
     */
    public void printMe() {
        System.out.println(descr());
    }

    /**
     * 获取节点的字符串描述。
     * 
     * @return 节点描述字符串
     */
    @Override
    public String toString() {
        return descr();
    }
}
