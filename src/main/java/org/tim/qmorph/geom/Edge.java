package org.tim.qmorph.geom;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.tim.qmorph.meshing.Constants;
import org.tim.qmorph.viewer.Msg;

/**
 * 表示二维网格中的一条边。
 * 
 * <p>
 * 这个类继承自Constants，用于存储和处理与边相关的信息。
 * 每条边由两个节点定义，可以属于一个或两个元素（三角形或四边形）。
 * 边可以具有前边属性，表示它是网格的前沿。
 * 
 * <p>
 * 主要功能包括：
 * <ul>
 * <li>边的创建和管理</li>
 * <li>计算边的长度和角度</li>
 * <li>处理边与节点、元素的连接关系</li>
 * <li>支持网格操作如边的交换和缝合</li>
 * </ul>
 * 
 * @see Node
 * @see Element
 * @see Triangle
 * @see Quad
 */
public class Edge extends Constants {

    public Node leftNode, rightNode; // 这条边有两个节点
    public Element element1 = null, element2 = null; // 属于这些元素（四边形/三角形）
    public Edge leftFrontNeighbor, rightFrontNeighbor; // 左前邻居和右前邻居
    public int level; // 级别

    public static List<List<Edge>> stateList = new ArrayList<>(3);

    public boolean frontEdge = false; // 是否是前边
    public boolean swappable = true; // 是否可交换
    public boolean selectable = true; // 是否可选择
    // Edge leftSide= null, rightSide= null; // Side edges when building a quad
    public boolean leftSide = false, rightSide = false; // 指示前邻居是否作为四边形的边
    public double len; // 这条边的长度
    public Color color = Color.green;

    public Edge(Node node1, Node node2) {
        if ((node1.x < node2.x) || (node1.x == node2.x && node1.y > node2.y)) {
            leftNode = node1;
            rightNode = node2;
        } else {
            leftNode = node2;
            rightNode = node1;
        }

        len = computeLength();
    }

    // 创建一个克隆的Edge e，所有重要的字段
    private Edge(Edge e) {
        leftNode = e.leftNode;
        rightNode = e.rightNode;
        len = e.len;
        e.element1 = element1;
        e.element2 = element2;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge e = (Edge) o;
            if (leftNode.equals(e.leftNode) && rightNode.equals(e.rightNode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建并返回当前边的副本。
     *
     * @return 当前边的副本。
     */
    public Edge copy() {
        return new Edge(this);
    }

    public static void clearStateList() {
        stateList.add(new ArrayList<>());
        stateList.add(new ArrayList<>());
        stateList.add(new ArrayList<>());
    }

    /**
     * 从状态列表中删除一条边。
     *
     * @return 如果边成功删除，则返回true，否则返回false。
     */
    public boolean removeFromStateList() {
        int i;
        int state = getState();
        i = stateList.get(state).indexOf(this);
        if (i == -1) {
            return false;
        }
        stateList.get(state).remove(i);
        return true;
    }

    // Removes an Edge from the stateLists
    // Returns true if the Edge was successfully removed, else false.
    /**
     * 从状态列表中移除当前对象。
     *
     * @param state 状态值
     * @return 如果成功移除返回 true，否则返回 false
     */
    public boolean removeFromStateList(int state) {
        int i;
        i = stateList.get(state).indexOf(this);
        if (i == -1) {
            return false;
        }
        stateList.get(state).remove(i);
        return true;
    }

    /**
     * 获取当前边的状态值。
     * 状态值由leftSide和rightSide两个布尔值决定：
     * - 如果leftSide为true，状态值+1
     * - 如果rightSide为true，状态值+1
     * 因此状态值可能为0、1或2，表示边的不同状态。
     * 
     * @return 返回边的状态值(0-2)
     */
    public int getState() {
        int ret = 0;
        if (leftSide) {
            ret++;
        }
        if (rightSide) {
            ret++;
        }
        return ret;
    }

    /**
     * 获取当前状态的值
     *
     * @return 返回状态值，可能的值有0, 1, 2
     */
    public int getTrueState() {
        return getState();
    }

    /**
     * 修改左边的状态
     *
     * @param newLeftState 新的左状态
     * @return 如果状态修改成功，则返回true，否则返回false
     */
    public boolean alterLeftState(boolean newLeftState) {
        int state = getState();
        int i = stateList.get(state).indexOf(this);
        if (i == -1) {
            return false;
        }
        leftSide = newLeftState;
        int newState = getState();
        if (state != newState) {
            stateList.get(state).remove(i);
            stateList.get(newState).add(this);
        }
        return true;
    }

    /**
     * 修改右边的状态
     *
     * @param newRightState 新的右状态
     * @return 如果状态修改成功，则返回true，否则返回false
     */
    public boolean alterRightState(boolean newRightState) {
        int state = getState();
        int i = stateList.get(state).indexOf(this);
        if (i == -1) {
            return false;
        }
        rightSide = newRightState;
        int newState = getState();
        if (state != newState) {
            stateList.get(state).remove(i);
            stateList.get(newState).add(this);
        }
        return true;
    }

    // Determine whether the frontNeighbor is an appropriate side Edge for a future
    // Quad with Edge e as base Edge. Return the frontNeighbor if so, else null.
    // elem== null if n.boundaryNode()== true
    /**
     * 计算并返回当前边的潜在侧面边。
     *
     * @param frontNeighbor 当前边的前邻边
     * @param n             当前边的节点
     * @return 如果角度小于 PI/2 + EPSILON，则返回前邻边，否则返回 null
     */
    public Edge evalPotSideEdge(Edge frontNeighbor, Node n) {
        Msg.debug("Entering Edge.evalPotSideEdge(..)");
        Element tri = getTriangleElement(), quad = getQuadElement();
        double ang;

        if (tri != null) {
            ang = sumAngle(tri, n, frontNeighbor);
        } else {
            ang = PIx2 - sumAngle(quad, n, frontNeighbor);
        }

        Msg.debug("sumAngle(..) between " + descr() + " and " + frontNeighbor.descr() + ": " + Math.toDegrees(ang));

        Msg.debug("Leaving Edge.evalPotSideEdge(..)");
        if (ang < PIx3div4) { // if (ang< PIdiv2+EPSILON) // Could this be better?
            return frontNeighbor;
        } else {
            return null;
        }
    }

    /**
     * 确定前邻居是否是未来四边形的适当边。
     *
     * @param frontNeighbor 前邻居
     * @param n             节点
     * @return 如果前邻居是适当的边，则返回前邻居，否则返回null
     */
    // Determine the state bit at both Nodes and set the left and right side Edges.
    // If a state bit is set, then the corresponding front neighbor Edge must get
    // Edge this as a side Edge at that Node. If it is not set, then it must get a
    // null
    // value instead.

    // this: the edge to be classified (gets a state value, and is added to a
    // statelist)
    public void classifyStateOfFrontEdge() {
        Msg.debug("Entering Edge.classifyStateOfFrontEdge()");
        Msg.debug("this: " + descr());
        final Edge lfn = leftFrontNeighbor, rfn = rightFrontNeighbor;

        Edge l, r;

        // Alter states and side Edges on left side:
        l = evalPotSideEdge(lfn, leftNode);
        if (l != null) {
            leftSide = true;
            if (leftNode == lfn.leftNode) {
                lfn.alterLeftState(true);
            } else {
                lfn.alterRightState(true);
            }
        } else {
            leftSide = false;
            if (leftNode == lfn.leftNode) {
                lfn.alterLeftState(false);
            } else {
                lfn.alterRightState(false);
            }
        }

        // Alter states and side Edges on right side:
        r = evalPotSideEdge(rfn, rightNode);
        if (r != null) {
            rightSide = true;
            if (rightNode == rfn.leftNode) {
                rfn.alterLeftState(true);
            } else {
                rfn.alterRightState(true);
            }
        } else {
            rightSide = false;
            if (rightNode == rfn.leftNode) {
                rfn.alterLeftState(false);
            } else {
                rfn.alterRightState(false);
            }
        }

        // Add this to a stateList:
        stateList.get(getState()).add(this);
        Msg.debug("Leaving Edge.classifyStateOfFrontEdge()");
    }

    /**
     * 判断当前边是否是较大的过渡边。
     *
     * @param e 要比较的边
     * @return 如果当前边是较大的过渡边，则返回true，否则返回false
     */
    public boolean isLargeTransition(Edge e) {
        double ratio;
        double e1Len = length();
        double e2Len = e.length();

        if (e1Len > e2Len) {
            ratio = e1Len / e2Len;
        } else {
            ratio = e2Len / e1Len;
        }

        if (ratio > 2.5) {
            return true;
        } else {
            return false;
        }
    }

    // Select the next front to be processed. The selection criteria is:
    // Primary: the edge state
    // Secondary: the edge level
    // If the candidate edge is part of a large transition on the front where
    // longest - shortest length ratio > 2.5, and the candidate edge is not in
    // state 1-1, then the shorter edge is selected.
    /**
     * 获取下一个要处理的边。选择标准如下：
     * 1. 边状态
     * 2. 边级别
     * 如果候选边是较大过渡边的一部分，且候选边不在状态1-1，则选择较短的边。
     * 
     * @return 下一个要处理的边
     */
    public static Edge getNextFront(/* ArrayList frontList, */) {
        Edge current, selected = null;
        int selState, curState = 2, i;

        // Select a front preferrably in stateList[2]

        while (curState >= 0 && selected == null) {
            for (i = 0; i < stateList.get(curState).size(); i++) {
                current = stateList.get(curState).get(i);
                if (current.selectable) {
                    selected = current;
                    break;
                }
            }
            curState--;
        }
        if (selected == null) {
            Msg.warning("getNextFront(): no selectable fronts found in stateLists.");
            return null;
        }

        selState = selected.getState();

        for (i = 0; i < stateList.get(selState).size(); i++) {
            current = stateList.get(selState).get(i);

            if (current.selectable && (current.level < selected.level
                    || (current.level == selected.level && current.length() < selected.length()))) {
                selected = current;
            }
        }

        if (selState != 2) {
            if (selected.isLargeTransition(selected.leftFrontNeighbor)) {
                if (selected.length() > selected.leftFrontNeighbor.length() && selected.leftFrontNeighbor.selectable) {
                    return selected.leftFrontNeighbor;
                }
            }

            if (selected.isLargeTransition(selected.rightFrontNeighbor)) {
                if (selected.length() > selected.rightFrontNeighbor.length()
                        && selected.rightFrontNeighbor.selectable) {
                    return selected.rightFrontNeighbor;
                }
            }
        }
        return selected;
    }

    /**
     * 将所有可选择的元素标记为可选择的。
     *
     * <p>
     * 该方法遍历嵌套列表中的所有元素，并将每个元素的 {@code selectable} 属性设置为 {@code true}。
     */
    public static void markAllSelectable() {
        stateList.forEach(l -> {
            l.forEach(e -> {
                e.selectable = true;
            });
        });
    }

    /**
     * 打印所有状态列表中的边信息。
     * 
     * <p>
     * 该方法仅在调试模式下执行，会按照以下顺序打印不同状态下的边信息：
     * <ul>
     * <li>状态2 (1-1) 的边</li>
     * <li>状态1 (0-1 和 1-0) 的边</li>
     * <li>状态0 (0-0) 的边</li>
     * </ul>
     * 
     * <p>
     * 每条边的信息包括其描述（坐标）和状态值。
     */
    public static void printStateLists() {
        if (Msg.debugMode) {
            System.out.println("frontsInState 1-1:");
            for (Edge edge : stateList.get(2)) {
                System.out.println("" + edge.descr() + ", (" + edge.getState() + ")");
            }
            System.out.println("frontsInState 0-1 and 1-0:");
            for (Edge edge : stateList.get(1)) {
                System.out.println("" + edge.descr() + ", (" + edge.getState() + ")");
            }
            System.out.println("frontsInState 0-0:");
            for (Edge edge : stateList.get(0)) {
                System.out.println("" + edge.descr() + ", (" + edge.getState() + ")");
            }
        }
    }

    /**
     * 判断当前边是否在给定边的左侧。
     *
     * @param e 要比较的边
     * @return 如果当前边的左侧节点在给定边的左侧节点之前，则返回true，否则返回false
     */
    public boolean leftTo(Edge e) {
        if ((leftNode.x < e.leftNode.x) || (leftNode.x == e.leftNode.x && leftNode.y < e.leftNode.y)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断当前边是否为前沿边。
     * 
     * <p>
     * 前沿边的定义是：当边的一侧是三角形元素而另一侧不是三角形元素时，该边为前沿边。
     * 具体来说，满足以下任一条件时返回true：
     * <ul>
     * <li>element1是三角形且element2不是三角形</li>
     * <li>element2是三角形且element1不是三角形</li>
     * </ul>
     * 
     * @return 如果当前边是前沿边则返回true，否则返回false
     */
    public boolean isFrontEdge() {
        if ((element1 instanceof Triangle && !(element2 instanceof Triangle))
                || (element2 instanceof Triangle && !(element1 instanceof Triangle))) {
            return true;
        } else {
            return false;
        }

        /*
         * if ((element1 instanceof Quad && element2 instanceof Quad) || (element1
         * instanceof Triangle && element2 instanceof Triangle)) return false; else
         * return true;
         */
    }

    /**
     * 获取当前边的描述信息。
     * 
     * <p>
     * 返回一个字符串，表示当前边的两个端点坐标。
     * 
     * @return 当前边的描述信息
     */
    public String descr() {
        return "(" + leftNode.x + ", " + leftNode.y + "), (" + rightNode.x + ", " + rightNode.y + ")";
    }

    /**
     * 打印当前边的描述信息。
     * 
     * <p>
     * 打印当前边的描述信息。
     */
    public void printMe() {
        System.out.println(descr());
    }

    /**
     * 获取当前边的长度。
     *
     * @return 返回当前边的长度
     */
    public double length() {
        return len;
    }

    // Replace this edge's node n1 with the node n2:
    /**
     * 替换当前边的节点n1为节点n2。
     * 
     * @param n1 要替换的节点
     * @param n2 替换后的节点
     * @return 如果替换成功则返回true，否则返回false
     */
    public boolean replaceNode(Node n1, Node n2) {
        if (leftNode.equals(n1)) {
            leftNode = n2;
        } else if (rightNode.equals(n1)) {
            rightNode = n2;
        } else {
            return false;
        }
        len = computeLength();
        return true;
    }

    // Seam two edges together. The method assumes that they already have one common
    // node. For each edge in the edgeList of the otherNode (otherE) of edge e:
    // Provided that otherThis or the otherNode of otherE is not found in any edge
    // in
    // the edgeList of the otherNode of this edge (otherThis), then the edge is
    // added
    // to this' edgeList.

    // Assumes that the quad area defined by the three distinct nodes of the two
    // edges,
    // and the node in the top of the triangle adjacent the otherNodes (of the
    // common
    // node of the two edges), is empty.

    /**
     * 将当前边与另一条边缝合在一起。
     * 该方法假设两条边已经有一个共同的节点。
     * 对于另一条边(e)的另一个节点(nKm1)的边列表中的每条边(eI):
     * 如果eI的另一个节点不是当前边的另一个节点(nKp1)，则:
     * 1. 如果nKp1的边列表中存在一条边(eJ)与eI共享同一个节点，则:
     * - 移除eI
     * - 更新相关元素的连接关系
     * 2. 否则:
     * - 将eI的nKm1节点替换为nKp1
     * - 将eI添加到nKp1的边列表中
     * 如果eI的另一个节点是nKp1，则从nKp1的边列表中移除eI
     *
     * @param e 要与当前边缝合的另一条边
     */
    public void seamWith(Edge e) {
        // 获取两条边的共同节点
        Node nK = commonNode(e);
        // 获取当前边的另一个节点
        Node nKp1 = otherNode(nK);
        // 获取另一条边的另一个节点
        Node nKm1 = e.otherNode(nK);
        Node other;
        boolean found = false;
        Edge eI, eJ;

        // 遍历另一条边的另一个节点的所有边
        for (int i = 0; i < nKm1.edgeList.size(); i++) {
            eI = nKm1.edgeList.get(i);
            other = eI.otherNode(nKm1);

            if (other != nKp1) {
                // 检查nKp1的边列表中是否存在与eI共享节点的边
                for (int j = 0; j < nKp1.edgeList.size(); j++) {
                    eJ = nKp1.edgeList.get(j);

                    if (other == eJ.otherNode(nKp1)) {
                        found = true;

                        // 移除eI
                        other.edgeList.remove(other.edgeList.indexOf(eI));

                        // 更新元素的firstNode
                        if (eI.element1.firstNode == nKm1) {
                            eI.element1.firstNode = nKp1;
                        }
                        // 更新元素的边连接关系
                        eI.element1.replaceEdge(eI, eJ);
                        eJ.connectToElement(eI.element1);
                        break;
                    }
                }
                if (!found) {
                    // 更新两个元素的firstNode
                    if (eI.element1.firstNode == nKm1) {
                        eI.element1.firstNode = nKp1;
                    }
                    if (eI.element2.firstNode == nKm1) {
                        eI.element2.firstNode = nKp1;
                    }

                    // 替换节点并更新边列表
                    eI.replaceNode(nKm1, nKp1);
                    nKp1.edgeList.add(eI);
                } else {
                    found = false;
                }
            } else {
                // 如果eI连接nKm1和nKp1，则从nKp1的边列表中移除eI
                nKp1.edgeList.remove(nKp1.edgeList.indexOf(eI));
            }
        }
    }

    /**
     * 计算并返回这条边的中点
     * 
     * @return 表示中点的新Node对象
     */
    public Node midPoint() {
        double xDiff = rightNode.x - leftNode.x;
        double yDiff = rightNode.y - leftNode.y;

        return new Node(leftNode.x + xDiff * 0.5, leftNode.y + yDiff * 0.5);
    }

    /**
     * 计算并返回这条边的长度
     * 
     * @return 表示长度的double值
     */
    public double computeLength() {
        double xdiff = rightNode.x - leftNode.x;
        double ydiff = rightNode.y - leftNode.y;
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    /**
     * 计算并返回两个点之间的距离
     * 
     * @param x1 第一个点的x坐标
     * @param y1 第一个点的y坐标
     * @param x2 第二个点的x坐标
     * @param y2 第二个点的y坐标
     * @return 两个点之间的距离
     */
    public double length(double x1, double y1, double x2, double y2) {
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    public double length(Node node1, Node node2) {
        double xdiff = node2.x - node1.x;
        double ydiff = node2.y - node1.y;
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    /**
     * 计算并返回相对于x轴的角度
     * 
     * @param n 要计算角度的节点
     * @return 相对于x轴的角度
     */
    // Returns angle relative to the x-axis (which is directed from the origin (0,0)
    // to
    // the right, btw) at node n (which is leftNode or rightNode).
    // Range: <-180, 180>
    public double angleAt(Node n) {
        // Math.acos returns values in the range 0.0 through pi, avoiding neg numbers.
        // If this is CW to x-axis, then return pos acos, else return neg acos
        // double x= leftNode.x-rightNode.x;
        // double y= leftNode.y-rightNode.y;
        Node other = otherNode(n);

        double x = n.x - other.x;
        double y = n.y - other.y;

        if (x == 0 && y > 0) {
            return -PIdiv2;
        } else if (x == 0 && y < 0) {
            return PIdiv2;
        } else {
            double hyp = Math.sqrt(x * x + y * y);

            if (x > 0) {
                if (y > 0) {
                    return Math.PI + Math.acos(x / hyp);
                } else {
                    return Math.PI - Math.acos(x / hyp);
                }
            } else {
                if (y > 0) {
                    return Math.PI + Math.PI - Math.acos(-x / hyp);
                } else {
                    return Math.acos(-x / hyp);
                }
            }

            // double cLen= Math.sqrt(x*x + y*y);
            /*
             * double aLen= Math.sqrt(x*x + y*y); if (y> 0) return -Math.acos((aLen*aLen +
             * x*x -y*y)/(2*aLen*Math.abs(x))); else return Math.acos((aLen*aLen + x*x
             * -y*y)/(2*aLen*Math.abs(x)));
             */
        }
    }

    /**
     * 计算并返回从当前边到另一条边之间的角度
     * 
     * @param sElem 当前边所在的元素
     * @param n     要计算角度的节点
     * @param eEdge 另一条边
     * @return 从当前边到另一条边之间的角度
     */
    public double sumAngle(Element sElem, Node n, Edge eEdge) {
        Msg.debug("Entering sumAngle(..)");
        Msg.debug("this: " + descr());
        if (sElem != null) {
            Msg.debug("sElem: " + sElem.descr());
        }
        if (n != null) {
            Msg.debug("n: " + n.descr());
        }
        if (eEdge != null) {
            Msg.debug("eEdge: " + eEdge.descr());
        }

        Element curElem = sElem;
        Edge curEdge = this;
        double ang = 0, iang = 0;
        double d;

        while (curEdge != eEdge && curElem != null) {
            d = curElem.angle(curEdge, n);
            // Msg.debug("curEdge= "+curEdge.descr());
            // Msg.debug("curElem.angle(..) returns "+Math.toDegrees(d));
            ang += d;
            curEdge = curElem.neighborEdge(n, curEdge);
            curElem = curElem.neighbor(curEdge);
        }

        // If n is a boundaryNode, the situation gets more complicated:
        if (curEdge != eEdge) {
            // Sum the "internal" angles:
            curEdge = this;
            curElem = sElem.neighbor(curEdge);

            while (curEdge != eEdge && curElem != null) {
                d = curElem.angle(curEdge, n);
                // Msg.debug("curEdge= "+curEdge.descr());
                // Msg.debug("curElem.angle(..) returns "+Math.toDegrees(d));
                iang += d;
                curEdge = curElem.neighborEdge(n, curEdge);
                curElem = curElem.neighbor(curEdge);
            }
            ang = PIx2 - iang;
        }
        Msg.debug("Leaving sumAngle(..), returning " + Math.toDegrees(ang));
        return ang;
    }

    // Return the first front Edge adjacent Node n starting check at
    // this Edge in Element sElem.
    public Edge firstFrontEdgeAt(Element sElem, Node n) {
        Element curElem = sElem;
        Edge curEdge = this;

        while (!curEdge.frontEdge) {
            curEdge = curElem.neighborEdge(n, curEdge);
            curElem = curElem.neighbor(curEdge);
        }
        return curEdge;
    }

    /**
     * 计算当前边与给定边在指定节点处的内角。
     * 返回一个正值。
     * 
     * @param edge 要计算角度的另一条边
     * @param n    计算角度的节点
     * @return 两条边之间的内角（弧度）
     */
    public double computePosAngle(Edge edge, Node n) {
        double a, b, c;
        if (edge == this) {
            Msg.warning("Edge.computePosAngle(..): 参数Edge与当前边相同。");
            return 2 * Math.PI;
        }

        if (leftNode.equals(n)) {
            if (n.equals(edge.leftNode)) {
                c = length(rightNode, edge.rightNode);
            } else if (n.equals(edge.rightNode)) {
                c = length(rightNode, edge.leftNode);
            } else {
                Msg.error("Edge::computePosAngle(..): 这些边不相连。");
                return 0;
            }
        } else if (rightNode.equals(n)) {
            if (n.equals(edge.leftNode)) {
                c = length(leftNode, edge.rightNode);
            } else if (n.equals(edge.rightNode)) {
                c = length(leftNode, edge.leftNode);
            } else {
                Msg.error("Edge::computePosAngle(..): 这些边不相连。");
                return 0;
            }
        } else {
            Msg.error("Edge::computePosAngle(..): 这些边不相连。");
            return 0;
        }
        a = computeLength(); // len; // 稍后再试...!
        b = edge.computeLength(); // edge.len; // 稍后再试...!

        // Math.acos返回[0, PI]范围内的值，
        // 输入值*必须严格*在[-1, 1]范围内 !!!!!!!!
        // ^^^^^^^^
        double itemp = (a * a + b * b - c * c) / (2 * a * b);
        if (itemp > 1.0) {
            return 0;
        } else if (itemp < -1.0) {
            return Math.PI;
        } else {
            return Math.acos(itemp);
        }
    }

    /**
     * 计算并返回当前边与给定边在指定节点处的逆时针方向角度。
     * 返回一个正值，范围为[0, 2*PI>。
     * 
     * @param edge 要计算角度的另一条边
     * @return 逆时针方向角度
     */
    public double computeCCWAngle(Edge edge) {
        Node n = commonNode(edge);
        double temp = computePosAngle(edge, n);

        MyVector thisVector = getVector(n);
        MyVector edgeVector = edge.getVector(n);

        if (thisVector.isCWto(edgeVector)) {
            return temp;
        } else {
            return PIx2 - temp;
        }
    }

    /**
     * 获取当前边与给定边的共同节点。
     * 
     * <p>
     * 该方法检查给定边的左右节点是否与当前边共享。
     * 如果找到共同节点则返回该节点，否则返回null。
     * 
     * @param e 要检查的另一条边
     * @return 如果存在共同节点则返回该节点，否则返回null
     */
    public Node commonNode(Edge e) {
        if (hasNode(e.leftNode)) {
            return e.leftNode;
        } else if (hasNode(e.rightNode)) {
            return e.rightNode;
        } else {
            return null;
        }
    }

    /**
     * 获取当前边与给定边的共同元素。
     * 
     * <p>
     * 该方法检查给定边的两个元素是否与当前边共享。
     * 如果找到共同元素则返回该元素，否则返回null。
     * 
     * @param e 要检查的另一条边
     * @return 如果存在共同元素则返回该元素，否则返回null
     */
    public Element commonElement(Edge e) {
        if (hasElement(e.element1)) {
            return e.element1;
        } else if (e.element2 != null && hasElement(e.element2)) {
            return e.element2;
        } else {
            return null;
        }
    }

    /**
     * 将当前边连接到其左右节点。
     * 
     * <p>
     * 该方法将当前边添加到其左右节点的边列表中。
     * 注意：此方法没有进行安全检查，调用前需确保边的节点已正确初始化。
     */
    public void connectNodes() {
        leftNode.edgeList.add(this);
        rightNode.edgeList.add(this);
    }

    /**
     * 从节点的边列表中移除当前边。
     * 
     * <p>
     * 该方法从当前边的左右节点的边列表中移除该边。
     * 注意：此方法没有进行安全检查，调用前需确保边的节点已正确初始化。
     */
    public void disconnectNodes() {
        leftNode.edgeList.remove(leftNode.edgeList.indexOf(this));
        rightNode.edgeList.remove(rightNode.edgeList.indexOf(this));
    }

    /**
     * 尝试从节点的边列表中移除当前边。
     * 
     * <p>
     * 该方法从当前边的左右节点的边列表中移除该边。
     * 注意：此方法没有进行安全检查，调用前需确保边的节点已正确初始化。
     */
    public void tryToDisconnectNodes() {
        int i;
        i = leftNode.edgeList.indexOf(this);
        if (i != -1) {
            leftNode.edgeList.remove(i);
        }
        i = rightNode.edgeList.indexOf(this);
        if (i != -1) {
            rightNode.edgeList.remove(i);
        }
    }

    /**
     * 将当前边连接到给定的三角形元素。
     * 
     * <p>
     * 该方法将当前边连接到给定的三角形元素。
     * 如果当前边已经连接到两个元素，则输出错误信息。
     * 
     * @param triangle 要连接的三角形元素
     */
    public void connectToTriangle(Triangle triangle) {
        if (hasElement(triangle)) {
            return;
        }
        if (element1 == null) {
            element1 = triangle;
        } else if (element2 == null) {
            element2 = triangle;
        } else {
            Msg.error("Edge.connectToTriangle(..): An edge cannot be connected to more than two elements. edge= "
                    + descr());
        }
    }

    /**
     * 将当前边连接到给定的四边形元素。
     * 
     * <p>
     * 该方法将当前边连接到给定的四边形元素。
     * 如果当前边已经连接到两个元素，则输出错误信息。
     * 
     * @param q 要连接的四边形元素
     */
    public void connectToQuad(Quad q) {
        if (hasElement(q)) {
            return;
        }
        if (element1 == null) {
            element1 = q;
        } else if (element2 == null) {
            element2 = q;
        } else {
            Msg.error("Edge.connectToQuad(..):An edge cannot be connected to more than two elements.");
        }
    }

    /**
     * 将当前边连接到给定的元素。
     * 
     * <p>
     * 该方法将当前边连接到给定的元素。
     * 如果当前边已经连接到两个元素，则输出错误信息。
     * 
     * @param elem 要连接的元素
     */
    public void connectToElement(Element elem) {
        if (hasElement(elem)) {
            return;
        }
        if (element1 == null) {
            element1 = elem;
        } else if (element2 == null) {
            element2 = elem;
        } else {
            Msg.error("Edge.connectToElement(..):An edge cannot be connected to more than two elements.");
        }
    }

    // element1 should never be null:
    /**
     * 将当前边从给定的元素断开连接。
     * 
     * <p>
     * 该方法将当前边从给定的元素断开连接。
     * 如果给定的元素是element1，则将element2移动到element1的位置，并将element2设为null。
     * 如果给定的元素是element2，则直接将element2设为null。
     * 如果给定的元素不是当前边连接的元素，则输出错误信息。
     * 
     * @param elem 要断开连接的元素
     */
    public void disconnectFromElement(Element elem) {
        if (element1 == elem) {
            element1 = element2;
            element2 = null;
        } else if (element2 == elem) {
            element2 = null;
        } else {
            Msg.error("Edge " + descr() + " is not connected to element " + elem.descr() + ".");
        }
    }

    /**
     * 获取与给定节点相对的节点。
     * 
     * <p>
     * 该方法在相邻三角形中查找与给定节点相对的节点。
     * 首先在element1中查找，如果找到的节点与wrongNode相同，
     * 则在element2中继续查找。
     * 
     * @param wrongNode 不需要返回的节点
     * @return 在相邻三角形中与wrongNode相对的节点
     */
    public Node oppositeNode(Node wrongNode) {
        Node candidate;
        Edge otherEdge;

        // Pick one of the other edges in element1
        int ind = element1.indexOf(this);
        if (ind == 0 || ind == 1) {
            otherEdge = element1.edgeList[2];
        } else {
            otherEdge = element1.edgeList[1];
        }

        // This edge contains an opposite node... get this node....
        if (otherEdge.leftNode != leftNode && otherEdge.leftNode != rightNode) {
            candidate = otherEdge.leftNode;
        } else {
            candidate = otherEdge.rightNode;
        }

        // Damn, it's the wrong node! Then we must go look in element2.
        if (candidate == wrongNode) {

            // Pick one of the other edges in element2
            ind = element2.indexOf(this);
            if (ind == 0 || ind == 1) {
                otherEdge = element2.edgeList[2];
            } else if (ind == 2) {
                otherEdge = element2.edgeList[1];
            }

            // This edge contains an opposite node
            // get this node....
            if (otherEdge.leftNode != leftNode && otherEdge.leftNode != rightNode) {
                candidate = otherEdge.leftNode;
            } else if (otherEdge.rightNode != leftNode && otherEdge.rightNode != rightNode) {
                candidate = otherEdge.rightNode;
            }
        }
        return candidate;
    }

    // Construct an Edge that is a unit normal to this Edge.
    // (Remember to add the new Node to nodeList if you want to keep it.)
    // nB: one of the nodes on this edge (leftNode or rightNode)

    /*
     * C b ____----x ___---- | b=1 ___---- | x----------------------x A c B
     *
     * angle(B)= PI/2
     *
     */
    // xdiff= xB - xA
    // ydiff= yB - yA
    // a= 1, c= sqrt(xdiff^2 + ydiff^2)
    // xC = xA + b* cos(alpha +ang.A)
    // = xA + xdiff - ydiff*a/c
    // = xB - ydiff*a/c
    // = xB - ydiff/c
    //
    // yC = yA + b* sin(alpha + ang.A)
    // = yA + ydiff + xdiff*a/c
    // = yB + xdiff*a/c
    // = yB + xdiff/c
    //
    /**
     * 获取与给定节点相对的单位法向量。
     * 
     * <p>
     * 该方法返回一个与给定节点相对的单位法向量。
     * 
     * @param n 给定的节点
     * @return 与给定节点相对的单位法向量
     */
    public Edge unitNormalAt(Node n) {
        Msg.debug("Entering Edge.unitNormalAt(..)");

        double xdiff = rightNode.x - leftNode.x;
        double ydiff = rightNode.y - leftNode.y;

        Msg.debug("this: " + descr() + ", n: " + n.descr());

        double c = Math.sqrt(xdiff * xdiff + ydiff * ydiff);

        double xn = n.x - ydiff / c;
        double yn = n.y + xdiff / c;

        Node newNode = new Node(xn, yn);

        Msg.debug("Leaving Edge.unitNormalAt(..)");
        return new Edge(n, newNode);
    }

    /**
     * 获取交换后的对角线边。
     * 
     * <p>
     * 该方法用于获取当前边交换对角线后的新边。
     * 如果当前边是边界边或连接四边形，则无法交换并返回null。
     * 
     * @return 交换后的新边，如果无法交换则返回null
     */
    public Edge getSwappedEdge() {
        if (element2 == null) {
            Msg.warning("getSwappedEdge: Cannot swap a boundary edge.");
            return null;
        }
        if (element1 instanceof Quad || element2 instanceof Quad) {
            Msg.warning("getSwappedEdge: Edge must lie between two triangles.");
            return null;
        }

        Node n = this.oppositeNode(null);
        Node m = this.oppositeNode(n);
        Edge swappedEdge = new Edge(n, m);

        return swappedEdge;
    }

    /**
     * Swap diagonal between the edge's two triangles and update locally (To be used
     * with getSwappedEdge())
     */
    /**
     * 交换对角线并更新局部元素。
     * 
     * <p>
     * 该方法用于交换两个三角形之间的对角线，并更新相关的连接关系。
     * 首先断开原有三角形的边连接，然后创建新的三角形并建立连接。
     * 最后更新节点的边列表。
     * 
     * @param e 要交换的对角线边
     * @throws IllegalStateException 如果边的两个元素未设置
     */
    public void swapToAndSetElementsFor(Edge e) {
        Msg.debug("Entering Edge.swapToAndSetElementsFor(..)");
        if (element1 == null || element2 == null) {
            Msg.error("Edge.swapToAndSetElementsFor(..): both elements not set");
        }

        Msg.debug("element1: " + element1.descr());
        Msg.debug("element2: " + element2.descr());

        Msg.debug("...this: " + descr());

        Edge e1 = element1.neighborEdge(leftNode, this);
        Edge e2 = element1.neighborEdge(e1.otherNode(leftNode), e1);
        Edge e3 = element2.neighborEdge(rightNode, this);
        Edge e4 = element2.neighborEdge(e3.otherNode(rightNode), e3);

        element2.disconnectEdges(); // important: element2 *first*, then element1
        element1.disconnectEdges();

        Triangle t1 = new Triangle(e, e2, e3);
        Triangle t2 = new Triangle(e, e4, e1);

        t1.connectEdges();
        t2.connectEdges();

        // Update edgeLists at this.leftNode and this.rightNode
        // and at e.leftNode and e.rightNode:
        disconnectNodes();
        e.connectNodes();

        Msg.debug("Leaving Edge.swapToAndSetElementsFor(..)");
    }

    /**
     * 获取从指定节点出发的向量。
     * 
     * <p>
     * 该方法根据给定的起始节点返回一个向量。如果起始节点是边的左端点，
     * 则返回从左端点到右端点的向量；如果起始节点是右端点，则返回从右端点到左端点的向量。
     *
     * @return 从指定节点出发的向量
     */
    public MyVector getVector() {
        return new MyVector(leftNode, rightNode);
    }

    /**
     * 获取从指定节点出发的向量。
     * 
     * <p>
     * 该方法根据给定的起始节点返回一个向量。如果起始节点是边的左端点，
     * 则返回从左端点到右端点的向量；如果起始节点是右端点，则返回从右端点到左端点的向量。
     * 如果给定的节点不是边的端点，则返回null并输出错误信息。
     * 
     * @param origin 向量的起始节点
     * @return 从指定节点出发的向量，如果节点不是边的端点则返回null
     */
    public MyVector getVector(Node origin) {
        if (origin.equals(leftNode)) {
            return new MyVector(leftNode, rightNode);
        } else if (origin.equals(rightNode)) {
            return new MyVector(rightNode, leftNode);
        } else {
            Msg.error("Edge::getVector(Node): Node not an endpoint in this edge.");
            return null;
        }
    }

    /**
     * 检查当前边是否与三角形相邻。
     * 
     * <p>
     * 该方法检查当前边的两个元素是否包含三角形。
     * 如果任一元素是三角形，则返回true，否则返回false。
     * 
     * @return 如果与三角形相邻则返回true，否则返回false
     */
    public boolean bordersToTriangle() {
        if (element1 instanceof Triangle) {
            return true;
        } else if (element2 != null && element2 instanceof Triangle) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查当前边是否为边界边。
     * 
     * <p>
     * 该方法检查当前边的两个元素是否为空。
     * 如果任一元素为空，则返回true，否则返回false。
     * 
     * @return 如果是边界边则返回true，否则返回false
     */
    public boolean boundaryEdge() {
        if (element1 == null || element2 == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查当前边是否为边界边或三角形边。
     * 
     * <p>
     * 该方法检查当前边的两个元素是否为空或是否为三角形。
     * 如果任一元素为空或为三角形，则返回true，否则返回false。
     * 
     * @return 如果是边界边或三角形边则返回true，否则返回false
     */
    public boolean boundaryOrTriangleEdge() {
        if (element1 == null || element2 == null || element1 instanceof Triangle || element2 instanceof Triangle) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查当前边是否包含给定的节点。
     * 
     * <p>
     * 该方法检查当前边的两个端点是否与给定的节点相同。
     * 如果找到相同节点则返回true，否则返回false。
     * 
     * @param n 要检查的节点
     * @return 如果包含给定节点则返回true，否则返回false
     */
    public boolean hasNode(Node n) {
        if (leftNode == n || rightNode == n) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查当前边是否包含给定的元素。
     * 
     * <p>
     * 该方法检查当前边的两个元素是否与给定的元素相同。
     * 如果找到相同元素则返回true，否则返回false。
     * 
     * @param elem 要检查的元素
     * @return 如果包含给定元素则返回true，否则返回false
     */
    public boolean hasElement(Element elem) {
        if (element1 == elem || element2 == elem) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断前方邻居是否有为false的
     *
     * @return 如果前方邻居存在且为false，返回true；否则返回false
     */
    public boolean hasFalseFrontNeighbor() {
        if (leftFrontNeighbor == null || !leftFrontNeighbor.frontEdge || rightFrontNeighbor == null
                || !rightFrontNeighbor.frontEdge) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查当前边是否具有给定的前邻居边。
     * 
     * <p>
     * 该方法检查给定的边是否与当前边的左前邻居或右前邻居相同。
     * 
     * @param e 要检查的前邻居边
     * @return 如果给定的边是当前边的左前邻居或右前邻居则返回true，否则返回false
     */
    public boolean hasFrontNeighbor(Edge e) {
        if (leftFrontNeighbor == e || rightFrontNeighbor == e) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前边的另一个节点。
     * 
     * <p>
     * 该方法返回当前边上除给定节点外的另一个节点。
     * 
     * @param n 当前边上的一个节点
     * @return 当前边的另一个节点
     */
    public Node otherNode(Node n) {
        if (n.equals(leftNode)) {
            return rightNode;
        } else if (n.equals(rightNode)) {
            return leftNode;
        } else {
            Msg.debug("this: " + descr());
            Msg.debug("n: " + n.descr());
            Msg.error("Edge.otherNode(Node): n is not on this edge");
            return null;
        }
    }

    /**
     * 在给定节点处延长当前边到给定的长度。
     *
     * <p>
     * 该方法在给定节点处延长当前边到给定的长度，并返回另一个节点。
     * 
     * @param length 新长度
     * @param nJ     从该节点开始延长边
     * @return 新边的另一个节点
     */
    public Node otherNodeGivenNewLength(double length, Node nJ) {
        // First find the angle between the existing edge and the x-axis:
        // Use a triangle containing one 90 degrees angle:
        MyVector v = new MyVector(nJ, this.otherNode(nJ));
        v.setLengthAndAngle(length, v.angle());

        // Use this to create the new node:
        return new Node(v.origin.x + v.x, v.origin.y + v.y);
    }

    /**
     * 如果两个节点在同一水平位置，则优先选择左节点。
     *
     * <p>
     * 该方法返回当前边上较高（y坐标较大）的节点。
     * 
     * @return 较高（y坐标较大）的节点
     */
    public Node upperNode() {
        if (leftNode.y >= rightNode.y) {
            return leftNode;
        } else {
            return rightNode;
        }
    }

    /**
     * 如果两个节点在同一水平位置，则优先选择右节点。
     * 
     * @return 较低（y坐标较小）的节点
     */
    public Node lowerNode() {
        if (rightNode.y <= leftNode.y) {
            return rightNode;
        } else {
            return leftNode;
        }
    }

    /**
     * 
     * <p>
     * 该方法检查从当前边到给定边e的1-轨道是否不包含任何三角形元素。
     * 如果节点n位于边界上，并且轨道包含一个孔，则轨道简单地跳过孔并继续在另一侧。
     * 
     * @param e      要检查的边
     * @param startQ 起始四边形
     * @return 如果1-轨道中没有三角形元素则返回true，否则返回false
     */
    public boolean noTrianglesInOrbit(Edge e, Quad startQ) {
        Msg.debug("Entering Edge.noTrianglesInOrbit(..)");
        Edge curEdge = this;
        Element curElem = startQ;
        Node n = commonNode(e);
        if (n == null) {
            Msg.debug("Leaving Edge.noTrianglesInOrbit(..), returns false");
            return false;
        }
        if (curEdge.boundaryEdge()) {
            curEdge = n.anotherBoundaryEdge(curEdge);
            curElem = curEdge.element1;
        }
        do {
            if (curElem instanceof Triangle) {
                Msg.debug("Leaving Edge.noTrianglesInOrbit(..), returns false");
                return false;
            }
            curEdge = curElem.neighborEdge(n, curEdge);
            if (curEdge.boundaryEdge()) {
                curEdge = n.anotherBoundaryEdge(curEdge);
                curElem = curEdge.element1;
            } else {
                curElem = curElem.neighbor(curEdge);
            }
        } while (curEdge != e);

        Msg.debug("Leaving Edge.noTrianglesInOrbit(..), returns true");
        return true;
    }

    /**
     * 查找当前边的左前邻居。
     * 
     * <p>
     * 该方法查找当前边的左前邻居，并返回找到的邻居边。
     * 
     * @param frontList2 前邻居列表
     * @return 左前邻居边
     */
    public Edge findLeftFrontNeighbor(List<Edge> frontList2) {
        List<Edge> list = new ArrayList<>();
        Edge candidate = null;
        double candAng = Double.POSITIVE_INFINITY, curAng;
        Triangle t;

        for (int j = 0; j < leftNode.edgeList.size(); j++) {
            Edge leftEdge = leftNode.edgeList.get(j);
            if (leftEdge != this && leftEdge.isFrontEdge() /* leftEdge.frontEdge */) {
                list.add(leftEdge);
            }
        }
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 0) {

            // Choose the front edge with the smallest angle
            t = getTriangleElement();

            for (Edge leftEdge : list) {
                curAng = sumAngle(t, leftNode, leftEdge);
                if (curAng < candAng) {
                    candAng = curAng;
                    candidate = leftEdge;
                }

                // if (noTrianglesInOrbit(leftEdge, q))
                // return leftEdge;
            }
            return candidate;
        }
        Msg.warning("findLeftFrontNeighbor(..): Returning null");
        return null;
    }

    /**
     * 查找当前边的右前邻居。
     * 
     * <p>
     * 该方法查找当前边的右前邻居，并返回找到的邻居边。
     * 
     * @param frontList2 前邻居列表
     * @return 右前邻居边
     */
    public Edge findRightFrontNeighbor(List<Edge> frontList2) {
        List<Edge> list = new ArrayList<>();
        Edge candidate = null;
        double candAng = Double.POSITIVE_INFINITY, curAng;
        Triangle t;

        for (int j = 0; j < rightNode.edgeList.size(); j++) {
            Edge rightEdge = rightNode.edgeList.get(j);
            if (rightEdge != this && rightEdge.isFrontEdge()/* frontList.contains(rightEdge) */) {
                list.add(rightEdge);
            }
        }
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 0) {
            t = getTriangleElement();
            for (Edge rightEdge : list) {
                curAng = sumAngle(t, rightNode, rightEdge);

                Msg.debug("findRightFrontNeighbor(): Angle between edge this: " + descr() + " and edge "
                        + rightEdge.descr() + ": " + curAng);
                if (curAng < candAng) {
                    candAng = curAng;
                    candidate = rightEdge;
                }

                // if (noTrianglesInOrbit(rightEdge, q))
                // return rightEdge;
            }
            Msg.debug("findRightFrontNeighbor(): Returning candidate " + candidate.descr());
            return candidate;
        }
        Msg.warning("findRightFrontNeighbor(..): List.size== " + list.size() + ". Returning null");
        return null;
    }

    /**
     * 设置适当的邻居边。
     * 
     * <p>
     * 该方法设置适当的邻居边，并更新当前边的左前邻居和右前邻居。
     * 
     * @param e 要设置的邻居边
     */
    public void setFrontNeighbor(Edge e) {
        if (e.hasNode(leftNode)) {
            leftFrontNeighbor = e;
        } else if (e.hasNode(rightNode)) {
            rightFrontNeighbor = e;
        } else {
            Msg.warning("Edge.setFrontNeighbor(..): Could not set.");
        }
    }

    /** Returns true if the frontEdgeNeighbors are changed. */
    /**
     * 设置当前边的左前邻居和右前邻居。
     * 
     * <p>
     * 该方法根据给定的前边列表，查找并设置当前边的左前邻居和右前邻居。
     * 同时确保邻居边也正确设置当前边作为其邻居。
     * 
     * @param frontList2 前边列表，用于查找邻居边
     * @return 如果邻居边发生变化则返回true，否则返回false
     */
    public boolean setFrontNeighbors(List<Edge> frontList2) {
        Edge lFront = findLeftFrontNeighbor(frontList2);
        Edge rFront = findRightFrontNeighbor(frontList2);
        boolean res = false;
        if (lFront != leftFrontNeighbor || rFront != rightFrontNeighbor) {
            res = true;
        }
        leftFrontNeighbor = lFront;
        rightFrontNeighbor = rFront;

        if (lFront != null && !lFront.hasFrontNeighbor(this)) {
            // res= true;
            lFront.setFrontNeighbor(this);
        }
        if (rFront != null && !rFront.hasFrontNeighbor(this)) {
            // res= true;
            rFront.setFrontNeighbor(this);
        }
        return res;
    }

    /**
     * 将当前边提升为前边。
     * 
     * <p>
     * 如果当前边还不是前边，则将其添加到前边列表中，并设置其级别和前边标志。
     * 
     * @param level     要设置的级别
     * @param frontList 前边列表，用于添加当前边
     */
    public void promoteToFront(int level, List<Edge> frontList) {
        if (!frontEdge) {
            frontList.add(this);
            this.level = level;
            frontEdge = true;
        }
    }

    /**
     * 将当前边从前边列表中移除。
     * 
     * <p>
     * 该方法将当前边从前边列表中移除，并设置其前边标志为false。
     * 如果当前边在前边列表中，则移除并返回true；否则返回false。
     * 
     * @param frontList2 要从中移除当前边的前边列表
     * @return 如果成功移除则返回true，否则返回false
     */
    public boolean removeFromFront(List<Edge> frontList2) {
        int i = frontList2.indexOf(this);
        frontEdge = false;
        if (i != -1) {
            frontList2.remove(i);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 在指定节点处分割三角形。
     * 
     * <p>
     * 该方法通过引入新节点nN来分割当前边，并创建新的边和三角形。
     * 具体步骤：
     * 1. 创建从当前边的左右节点到nN的新边
     * 2. 创建从nN到两个相邻三角形对边的对角线
     * 3. 创建四个新的三角形
     * 4. 更新节点和边的连接关系
     * 5. 更新全局列表
     * 
     * @param nN           要插入的新节点
     * @param ben          基准节点，用于确定返回哪条新边
     * @param triangleList 三角形列表，用于更新
     * @param edgeList     边列表，用于更新
     * @param nodeList     节点列表，用于更新
     * @return 与基准节点ben相连的新边，如果未找到则返回null
     */
    public Edge splitTrianglesAt(Node nN, Node ben, List<Triangle> triangleList, List<Edge> edgeList,
            List<Node> nodeList) {
        Msg.debug("Entering Edge.splitTrianglesAt(..)");
        Edge eK1 = new Edge(leftNode, nN);
        Edge eK2 = new Edge(rightNode, nN);

        Triangle tri1 = (Triangle) element1;
        Triangle tri2 = (Triangle) element2;

        Node n1 = tri1.oppositeOfEdge(this);
        Node n2 = tri2.oppositeOfEdge(this);
        Edge diagonal1 = new Edge(nN, n1);
        Edge diagonal2 = new Edge(nN, n2);

        Edge e12 = tri1.neighborEdge(leftNode, this);
        Edge e13 = tri1.neighborEdge(rightNode, this);
        Edge e22 = tri2.neighborEdge(leftNode, this);
        Edge e23 = tri2.neighborEdge(rightNode, this);

        Triangle t11 = new Triangle(diagonal1, e12, eK1);
        Triangle t12 = new Triangle(diagonal1, e13, eK2);
        Triangle t21 = new Triangle(diagonal2, e22, eK1);
        Triangle t22 = new Triangle(diagonal2, e23, eK2);

        // Update the nodes' edgeLists
        disconnectNodes();
        eK1.connectNodes();
        eK2.connectNodes();
        diagonal1.connectNodes();
        diagonal2.connectNodes();

        // Disconnect old Triangles
        tri1.disconnectEdges();
        tri2.disconnectEdges();

        // Connect Edges to new Triangles
        t11.connectEdges();
        t12.connectEdges();
        t21.connectEdges();
        t22.connectEdges();

        // Update "global" lists
        edgeList.remove(edgeList.indexOf(this));
        edgeList.add(eK1);
        edgeList.add(eK2);
        edgeList.add(diagonal1);
        edgeList.add(diagonal2);

        triangleList.remove(triangleList.indexOf(tri1));
        triangleList.remove(triangleList.indexOf(tri2));
        triangleList.add(t11);
        triangleList.add(t12);
        triangleList.add(t21);
        triangleList.add(t22);

        Msg.debug("...Created triangle " + t11.descr());
        Msg.debug("...Created triangle " + t12.descr());
        Msg.debug("...Created triangle " + t21.descr());
        Msg.debug("...Created triangle " + t22.descr());

        Msg.debug("Leaving Edge.splitTrianglesAt(..)");
        if (eK1.hasNode(ben)) {
            return eK1;
        } else if (eK2.hasNode(ben)) {
            return eK2;
        } else {
            Msg.error("");
            return null;
        }
    }

    /**
     * 在当前边的中点处分割相邻的三角形。
     * 
     * <p>
     * 该方法在当前边的中点创建一个新节点，并使用该节点将相邻的三角形分割成新的三角形。
     * 新创建的节点会被添加到节点列表中，并设置为蓝色。
     * 
     * @param triangleList 三角形列表，用于存储新创建的三角形
     * @param edgeList     边列表，用于存储新创建的边
     * @param nodeList     节点列表，用于存储新创建的节点
     * @param baseEdge     基准边，用于确定分割方向
     * @return 与基准边相邻的新创建的边
     */
    public Edge splitTrianglesAtMyMidPoint(List<Triangle> triangleList, List<Edge> edgeList, List<Node> nodeList,
            Edge baseEdge) {
        Msg.debug("Entering Edge.splitTrianglesAtMyMidPoint(..).");

        Edge lowerEdge;
        Node ben = baseEdge.commonNode(this);
        Node mid = this.midPoint();
        nodeList.add(mid);
        mid.color = java.awt.Color.blue;

        Msg.debug("Splitting edge " + descr());
        Msg.debug("Creating new Node: " + mid.descr());

        lowerEdge = splitTrianglesAt(mid, ben, triangleList, edgeList, nodeList);

        Msg.debug("Leaving Edge.splitTrianglesAtMyMidPoint(..).");
        return lowerEdge;
    }

    /**
     * 查找与四边形元素相邻的下一条边，从当前边开始，该边是给定元素的一部分且与给定节点相邻。
     * 注意：如果遇到边界，该方法将停止。
     *
     * @param n         节点
     * @param startElem 起始元素
     * @return 在节点n周围遍历时找到的第一条四边形边，从元素startElem中的边e开始，
     *         沿着从e到startElem中n处e的相邻边的方向移动。如果startElem恰好是一个四边形，
     *         该方法将不会考虑该特定四边形。如果未找到，则返回null。
     */
    public Edge nextQuadEdgeAt(Node n, Element startElem) {
        Msg.debug("Entering Edge.nextQuadEdgeAt(..)");
        Element elem;
        Edge e;
        int i = 3;

        e = startElem.neighborEdge(n, this);
        elem = startElem.neighbor(e);

        while (elem != null && !(elem instanceof Quad) && elem != startElem) {
            e = elem.neighborEdge(n, e);
            Msg.debug("..." + i);
            i++;
            elem = elem.neighbor(e);
        }
        Msg.debug("Leaving Edge.nextQuadEdgeAt(..)");
        if (elem != null && elem instanceof Quad && elem != startElem) {
            return e;
        } else {
            return null;
        }
    }

    // 返回一个相邻的四边形元素。当应用于内部前边时，
    // 当然只有一个可能的四边形可以返回。
    /**
     * 获取与当前边相邻的四边形元素。
     * 
     * <p>
     * 该方法检查当前边的两个相邻元素，如果其中一个是四边形则返回该四边形。
     * 如果两个元素都不是四边形，则返回null。
     * 
     * @return 如果存在相邻的四边形元素则返回该四边形，否则返回null
     */
    public Quad getQuadElement() {
        if (element1 instanceof Quad) {
            return (Quad) element1;
        } else if (element2 instanceof Quad) {
            return (Quad) element2;
        } else {
            return null;
        }
    }

    /**
     * 返回一个相邻的元素，该元素是一个三角形。该方法应该在任何前边上都能正常工作。
     * 
     * <p>
     * 该方法返回一个相邻的元素，该元素是一个三角形。该方法应该在任何前边上都能正常工作。
     * 
     * @return 相邻的三角形元素
     */
    public Triangle getTriangleElement() {
        if (element1 instanceof Triangle) {
            return (Triangle) element1;
        } else if (element2 instanceof Triangle) {
            return (Triangle) element2;
        } else {
            return null;
        }
    }

    /** Return the neighboring quad that is also a neighbor of edge e. */
    /**
     * 获取与当前边和指定边都相邻的四边形元素。
     * 
     * <p>
     * 该方法检查当前边的两个相邻元素，如果其中一个是四边形且包含指定的边，
     * 则返回该四边形。如果不存在这样的四边形，则返回null。
     * 
     * @param e 要检查的另一条边
     * @return 如果存在同时包含当前边和指定边的四边形元素则返回该四边形，否则返回null
     */
    public Quad getQuadWithEdge(Edge e) {
        if (element1 instanceof Quad && element1.hasEdge(e)) {
            return (Quad) element1;
        } else if (element2 instanceof Quad && element2.hasEdge(e)) {
            return (Quad) element2;
        } else {
            return null;
        }
    }

    /** Return the front neighbor edge at node n. */
    /**
     * 获取与指定节点相邻的前边。
     * 
     * <p>
     * 该方法检查当前边的两个前邻居，如果其中一个邻居的公共节点是给定的节点n，
     * 则返回该邻居。否则返回null。
     * 
     * @param n 要检查的节点
     * @return 与指定节点相邻的前边，如果存在则返回该前边，否则返回null
     */
    public Edge frontNeighborAt(Node n) {
        if (leftFrontNeighbor != null && commonNode(leftFrontNeighbor) == n) {
            return leftFrontNeighbor;
        } else if (rightFrontNeighbor != null && commonNode(rightFrontNeighbor) == n) {
            return rightFrontNeighbor;
        } else {
            return null;
        }
    }

    /** @return the front neighbor next to this (not the prev edge). */
    /**
     * 获取当前边的下一个前邻居边。
     * 
     * <p>
     * 该方法返回当前边的另一个前邻居边，该邻居边不是给定的前一个边。
     * 如果找不到合适的前邻居边，则返回null并输出错误信息。
     * 
     * @param prev 前一个前邻居边
     * @return 下一个前邻居边，如果找不到则返回null
     */
    public Edge nextFrontNeighbor(Edge prev) {
        if (leftFrontNeighbor != prev) {
            return leftFrontNeighbor;
        } else if (rightFrontNeighbor != prev) {
            return rightFrontNeighbor;
        } else {
            Msg.error("Edge.nextFrontNeighbor(Edge): Cannot find a suitable next edge.");
            return null;
        }
    }

    // Return a neighbor edge at node n, that is front edge according to the
    // definition,
    // and that is part of the same loop as this edge.
    // Assumes that this is a true front edge.
    /**
     * 获取与指定节点相邻的真实前边。
     * 
     * <p>
     * 该方法在当前边所在的三角形网格中，沿着与指定节点相邻的边进行遍历，
     * 直到找到一个真实的前边。该方法假设当前边是一个真实的前边。
     * 
     * @param n 要检查的节点
     * @return 与指定节点相邻的真实前边
     * @throws IllegalStateException 如果当前边不包含指定的节点
     */
    public Edge trueFrontNeighborAt(Node n) {
        Element curElem = getTriangleElement();
        Edge curEdge = this;

        if (!hasNode(n)) {
            Msg.error("trueFrontNeighborAt(..): this Edge hasn't got Node " + n.descr());
        }

        do {
            curEdge = curElem.neighborEdge(n, curEdge);
            curElem = curElem.neighbor(curEdge);
        } while (!curEdge.isFrontEdge());

        return curEdge;
    }

    @Override
    public String toString() {
        return descr();
    }
}
