package org.tim.qmorph.geom;

import java.util.ArrayList;
import java.util.List;

import org.tim.qmorph.viewer.Msg;

/**
 * 四边形类，保存四边形的相关信息，并提供处理四边形相关操作的方法。
 * 继承自Element类。
 */
public class Quad extends Element {

    /**
     * 是否为假四边形（用于特殊情况）。
     */
    public boolean isFake;

    /**
     * 创建普通四边形。
     * 
     * @param baseEdge  底边
     * @param leftEdge  左边
     * @param rightEdge 右边
     * @param topEdge   顶边
     */
    public Quad(Edge baseEdge, Edge leftEdge, Edge rightEdge, Edge topEdge) {
        Edge e;
        Node b, c;
        edgeList = new Edge[4];
        ang = new double[4];

        if (rightEdge != topEdge) {
            isFake = false;
        } else {
            isFake = true;
        }

        edgeList[base] = baseEdge;
        edgeList[left] = leftEdge;
        edgeList[right] = rightEdge;
        edgeList[top] = topEdge;

        edgeList[base].len = edgeList[base].computeLength();
        edgeList[left].len = edgeList[left].computeLength();
        edgeList[right].len = edgeList[right].computeLength();
        if (!isFake) {
            edgeList[top].len = edgeList[top].computeLength();
        }

        firstNode = baseEdge.leftNode;
        if (inverted()) {
            firstNode = baseEdge.rightNode;
        }

        updateAngles();
    }

    /**
     * 通过一条公共边和其两侧三角形生成普通四边形。
     * 
     * @param e 公共边
     */
    public Quad(Edge e) {
        isFake = false;
        edgeList = new Edge[4];
        ang = new double[4];

        Triangle t1 = (Triangle) e.element1;
        Triangle t2 = (Triangle) e.element2;

        Edge c11 = t1.otherEdge(e);
        Edge c12 = t1.otherEdge(e, c11);
        Edge c21, c22, temp1 = t2.otherEdge(e);
        if (c11.commonNode(temp1) != null) {
            c21 = temp1;
            c22 = t2.otherEdge(e, temp1);
        } else {
            c21 = t2.otherEdge(e, temp1);
            c22 = temp1;
        }

        // The following assignments to the array ang[] is based on a
        // geometric sketch. The correctness of this should be tested.
        if (c11.leftNode.hasEdge(c12)) {
            edgeList[base] = c11;
            edgeList[left] = c12;
            ang[0] = t1.angle(c11, c12);

            if (c11.rightNode.hasEdge(c21)) {
                edgeList[right] = c21;
                edgeList[top] = c22;
                ang[1] = t1.angle(c11, e) + t2.angle(e, c21);
                ang[2] = t1.angle(c12, e) + t2.angle(e, c22);
                ang[3] = t2.angle(c21, c22);
            } else {
                edgeList[right] = c22;
                edgeList[top] = c21;
                ang[1] = t1.angle(c11, e) + t2.angle(e, c22);
                ang[2] = t1.angle(c12, e) + t2.angle(e, c21);
                ang[3] = t2.angle(c21, c22);
            }
        } else { // c11.rightNode.hasEdge(c12)
            edgeList[base] = c11;
            edgeList[right] = c12;
            ang[1] = t1.angle(c11, c12);

            if (c11.leftNode.hasEdge(c21)) {
                edgeList[left] = c21;
                edgeList[top] = c22;
                ang[0] = t1.angle(c11, e) + t2.angle(e, c21);
                ang[2] = t2.angle(c21, c22);
                ang[3] = t1.angle(c12, e) + t2.angle(e, c22);
            } else { // c11.leftNode.hasEdge(c22)
                edgeList[left] = c22;
                edgeList[top] = c21;
                ang[0] = t1.angle(c11, e) + t2.angle(e, c22);
                ang[2] = t2.angle(c21, c22);
                ang[3] = t1.angle(c12, e) + t2.angle(e, c21);
            }
        }

        firstNode = edgeList[base].leftNode;
        if (inverted()) {
            firstNode = edgeList[base].rightNode;
        }
    }

    /**
     * 通过三角形生成假四边形（顶边和右边重合）。
     * 
     * @param t 三角形
     */
    public Quad(Triangle t) {
        Node n;
        isFake = true;
        edgeList = new Edge[4];
        ang = new double[4];

        edgeList[base] = t.edgeList[0];
        if (t.edgeList[1].hasNode(edgeList[base].leftNode)) {
            edgeList[left] = t.edgeList[1];
            edgeList[right] = t.edgeList[2];
        } else {
            edgeList[left] = t.edgeList[2];
            edgeList[right] = t.edgeList[1];
        }

        edgeList[top] = edgeList[right];

        firstNode = t.firstNode;
        ang[0] = t.ang[0];
        ang[1] = t.ang[1];
        ang[2] = t.ang[2];
    }

    /**
     * 用于测试的简单四边形构造函数。
     * 
     * @param e  对角线边
     * @param n1 四边形的一个节点
     * @param n2 四边形的另一个节点
     */
    public Quad(Edge e, Node n1, Node n2) {
        Msg.debug("Entering Quad(Edge, Node, Node)");
        Msg.debug("e= " + e.descr() + ", n1= " + n1.descr() + ", n2= " + n2.descr());
        isFake = false;
        edgeList = new Edge[4];

        edgeList[base] = new Edge(e.leftNode, n1);
        edgeList[top] = new Edge(n2, e.rightNode);

        if (edgeList[base].leftNode == e.leftNode) {
            edgeList[right] = new Edge(edgeList[base].rightNode, e.rightNode);
            edgeList[left] = new Edge(edgeList[base].leftNode, edgeList[top].otherNode(e.rightNode));
        } else {
            edgeList[right] = new Edge(edgeList[base].rightNode, edgeList[top].otherNode(e.rightNode));
            edgeList[left] = new Edge(edgeList[base].leftNode, e.rightNode);
        }

        Msg.debug("New quad is " + descr());
        Msg.debug("Leaving Quad(Edge, Node, Node)");

        firstNode = edgeList[base].leftNode;
        if (inverted()) {
            firstNode = edgeList[base].rightNode;
        }
    }

    /**
     * 用于elementWithExchangedNode的假四边形构造函数（三节点）。
     * 
     * @param n1 节点1
     * @param n2 节点2
     * @param n3 节点3
     * @param f  首节点
     */
    private Quad(Node n1, Node n2, Node n3, Node f) {
        isFake = true;
        edgeList = new Edge[4];
        ang = new double[4];

        edgeList[base] = new Edge(n1, n2);
        if (edgeList[base].leftNode == n1) {
            edgeList[left] = new Edge(n1, n3);
            edgeList[right] = new Edge(n2, n3);
        } else {
            edgeList[left] = new Edge(n2, n3);
            edgeList[right] = new Edge(n1, n3);
        }
        edgeList[top] = edgeList[right];

        firstNode = f;
        updateAngles();
    }

    /**
     * 用于elementWithExchangedNode的普通四边形构造函数（四节点）。
     * 
     * @param n1 节点1
     * @param n2 节点2
     * @param n3 节点3
     * @param n4 节点4
     * @param f  首节点
     */
    private Quad(Node n1, Node n2, Node n3, Node n4, Node f) {
        isFake = false;
        edgeList = new Edge[4];
        ang = new double[4];

        edgeList[base] = new Edge(n1, n2);
        edgeList[top] = new Edge(n3, n4);
        if (edgeList[base].leftNode == n1) {
            edgeList[left] = new Edge(n1, n3);
            edgeList[right] = new Edge(n2, n4);
        } else {
            edgeList[left] = new Edge(n2, n4);
            edgeList[right] = new Edge(n1, n3);
        }

        firstNode = f;
        updateAngles();
    }

    /**
     * 节点替换，返回替换节点后的新四边形。
     * 
     * @param original    原节点
     * @param replacement 替换节点
     * @return 替换节点后的新四边形
     */
    @Override
    public Element elementWithExchangedNodes(Node original, Node replacement) {
        Node node1 = edgeList[base].leftNode;
        Node node2 = edgeList[base].rightNode;
        Node node3 = edgeList[left].otherNode(edgeList[base].leftNode);

        if (isFake) {
            if (node1 == original) {
                if (original == firstNode) {
                    return new Quad(replacement, node2, node3, replacement);
                } else {
                    return new Quad(replacement, node2, node3, firstNode);
                }
            } else if (node2 == original) {
                if (original == firstNode) {
                    return new Quad(node1, replacement, node3, replacement);
                } else {
                    return new Quad(node1, replacement, node3, firstNode);
                }
            } else if (node3 == original) {
                if (original == firstNode) {
                    return new Quad(node1, node2, replacement, replacement);
                } else {
                    return new Quad(node1, node2, replacement, firstNode);
                }
            } else {
                return null;
            }
        }

        Node node4 = edgeList[right].otherNode(edgeList[base].rightNode);

        if (node1 == original) {
            if (original == firstNode) {
                return new Quad(replacement, node2, node3, node4, replacement);
            } else {
                return new Quad(replacement, node2, node3, node4, firstNode);
            }
        } else if (node2 == original) {
            if (original == firstNode) {
                return new Quad(node1, replacement, node3, node4, replacement);
            } else {
                return new Quad(node1, replacement, node3, node4, firstNode);
            }
        } else if (node3 == original) {
            if (original == firstNode) {
                return new Quad(node1, node2, replacement, node4, replacement);
            } else {
                return new Quad(node1, node2, replacement, node4, firstNode);
            }
        } else if (node4 == original) {
            if (original == firstNode) {
                return new Quad(node1, node2, node3, replacement, replacement);
            } else {
                return new Quad(node1, node2, node3, replacement, firstNode);
            }
        } else {
            return null;
        }
    }

    /**
     * 判断将节点n1移动到n2后，四边形是否会反转。
     * 
     * @param n1 被移动的节点
     * @param n2 新位置节点
     * @return 反转返回true，否则返回false
     */
    @Override
    public boolean invertedWhenNodeRelocated(Node n1, Node n2) {
        Msg.debug("Entering Quad.invertedWhenNodeRelocated(..)");
        Node thisFirstNode = firstNode;
        Node a = edgeList[base].leftNode, b = edgeList[base].rightNode, c = edgeList[right].otherNode(b),
                d = edgeList[left].otherNode(a);

        if (a == n1) {
            a = n2;
        } else if (b == n1) {
            b = n2;
        } else if (c == n1) {
            c = n2;
        } else if (d == n1) {
            d = n2;
        }

        if (n1 == firstNode) {
            thisFirstNode = n2;
        }

        // We need at least 3 okays to be certain that this quad is not inverted
        int okays = 0;
        if (cross(a, d, b, d) > 0) { // Was: >=
            okays++;
        }
        if (cross(a, c, b, c) > 0) { // Was: >=
            okays++;
        }
        if (cross(c, a, d, a) > 0) { // Was: >=
            okays++;
        }
        if (cross(c, b, d, b) > 0) { // Was: >=
            okays++;
        }

        if (b == thisFirstNode) {
            okays = 4 - okays;
        }

        Msg.debug("Leaving Quad.invertedWhenNodeRelocated(..), okays: " + okays);
        if (okays >= 3) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断在合并节点时，是否有相邻元素会反转。
     * 
     * @param n     合并后节点
     * @param n1    四边形中的一个节点
     * @param n2    四边形中的另一个节点
     * @param lK    n1的相邻元素列表
     * @param lKOpp n2的相邻元素列表
     * @return 存在反转返回true，否则返回false
     */
    public boolean anyInvertedElementsWhenCollapsed(Node n, Node n1, Node n2, List<Element> lK, List<Element> lKOpp) {
        Msg.debug("Entering Quad.anyInvertedElementsWhenCollapsed(..)");
        Element elem;
        int i;

        for (i = 0; i < lK.size(); i++) {
            elem = lK.get(i);
            if (elem != this && elem.invertedWhenNodeRelocated(n1, n)) {
                Msg.debug("Leaving Quad.anyInvertedElementsWhenCollapsed(..) ret: true");
                return true;
            }
        }

        for (i = 0; i < lKOpp.size(); i++) {
            elem = lKOpp.get(i);
            if (elem != this && elem.invertedWhenNodeRelocated(n2, n)) {
                Msg.debug("Leaving Quad.anyInvertedElementsWhenCollapsed(..) ret: true");
                return true;
            }
        }

        Msg.debug("Leaving Quad.anyInvertedElementsWhenCollapsed(..) ret: false");
        return false;
    }

    /**
     * 判断两个四边形是否相等（节点顺序一致）。
     * 
     * @param o 另一个对象
     * @return 相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Quad) {
            Quad q = (Quad) o;
            Node node1, node2, node3, node4;
            Node qnode1, qnode2, qnode3, qnode4;
            node1 = edgeList[base].leftNode;
            node2 = edgeList[base].rightNode;
            node3 = edgeList[left].otherNode(edgeList[base].leftNode);
            node4 = edgeList[right].otherNode(edgeList[base].rightNode);

            qnode1 = q.edgeList[base].leftNode;
            qnode2 = q.edgeList[base].rightNode;
            qnode3 = q.edgeList[left].otherNode(q.edgeList[base].leftNode);
            qnode4 = q.edgeList[right].otherNode(q.edgeList[base].rightNode);

            if (node1 == qnode1 && node2 == qnode2 && node3 == qnode3 && node4 == qnode4) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 获取指定边在四边形中的索引。
     * 
     * @param e 边
     * @return 索引（0-3），未找到返回-1
     */
    @Override
    public int indexOf(Edge e) {
        if (edgeList[base] == e) {
            return base;
        } else if (edgeList[left] == e) {
            return left;
        } else if (edgeList[right] == e) {
            return right;
        } else if (edgeList[top] == e) {
            return top;
        } else {
            return -1;
        }
    }

    /**
     * Collapse the quad by welding together two neighboring edges. For each edge in
     * the edgeList of the otherNode (otherE2) of edge e2: Provided that otherE1 or
     * the otherNode of otherE2 is not found in any edge in the edgeList of the
     * otherNode of edge e1 (otherE1), then the edge is added to otherE1's edgeList.
     * Assumes that the inner quad area is empty and that the edges of the quad
     * don't have the quad as an element.
     *
     * @param e1 first edge
     * @param e2 second edge, and the neighbor to edge e1 in this quad
     */
    public void closeQuad(Edge e1, Edge e2) {
        Msg.debug("Entering Quad.closeQuad(..)");
        Node nK = e1.commonNode(e2);
        Node nKp1 = e1.otherNode(nK), nKm1 = e2.otherNode(nK), other;
        boolean found = false;
        Edge e, eI, eJ;
        List<Edge> addList = new ArrayList<>();
        List<Quad> quadList = new ArrayList<>();

        Msg.debug("...nKp1: " + nKp1.descr());
        Msg.debug("...nKm1: " + nKm1.descr());

        for (int i = 0; i < nKm1.edgeList.size(); i++) {
            eI = nKm1.edgeList.get(i);
            other = eI.otherNode(nKm1);
            Msg.debug("...eI== " + eI.descr());

            for (int j = 0; j < nKp1.edgeList.size(); j++) {
                eJ = nKp1.edgeList.get(j);

                if (other == eJ.otherNode(nKp1)) {
                    found = true;
                    Msg.debug("...eI is connected to eJ== " + eJ.descr());

                    other.edgeList.remove(other.edgeList.indexOf(eI));

                    if (eI.element1 != null) {
                        if (eI.element1.firstNode == nKm1) {
                            eI.element1.firstNode = nKp1; // Don't forget firstNode!!
                        }
                        Msg.debug("... replacing eI with eJ in eI.element1");

                        eI.element1.replaceEdge(eI, eJ);
                        eJ.connectToElement(eI.element1);
                        if (eI.element1 instanceof Quad) { // Then LR might need updating
                            quadList.add((Quad) eI.element1); // but that must be done later
                        }
                    }
                    break;
                }
            }

            if (!found) {
                Msg.debug("...none of nKp1's edges connected to eI");
                if (eI.element1 != null && eI.element1.firstNode == nKm1) {
                    eI.element1.firstNode = nKp1; // Don't forget firstNode!!
                }
                if (eI.element2 != null && eI.element2.firstNode == nKm1) {
                    eI.element2.firstNode = nKp1; // Don't forget firstNode!!
                }

                Msg.debug("...replacing " + nKm1.descr() + " with " + nKp1.descr() + " on edge " + eI.descr());

                nKm1.edgeList.set(i, null);
                eI.replaceNode(nKm1, nKp1);
                addList.add(eI); // nKp1.edgeList.add(eI);
            } else {
                found = false;
            }
        }

        for (int i = 0; i < addList.size(); i++) {
            e = addList.get(i);
            nKp1.edgeList.add(e);
        }

        for (Quad q : quadList) {
            q.updateLR();
        }

        nKm1.edgeList.clear();

        Msg.debug("Leaving Quad.closeQuad(..)");
    }

    /**
     * Create a new quad by combining this quad with quad q. The two quads must
     * initially share two incident edges:
     *
     * @param e1 first common edge
     * @param e2 second common edge
     */
    public Quad combine(Quad q, Edge e1, Edge e2) {
        Msg.debug("Entering Quad.combine(Quad, ..)");
        Quad quad;
        Edge e, edges[] = new Edge[4];
        int i;

        edges[0] = null;
        for (i = 0; i < 4; i++) {
            e = edgeList[i];

            if (e != e1 && e != e2) {
                if (edges[0] == null) {
                    edges[0] = e;
                } else if (e.hasNode(edges[0].leftNode)) {
                    edges[1] = e;
                } else if (e.hasNode(edges[0].rightNode)) {
                    edges[2] = e;
                }
            }
        }

        for (i = 0; i < 4; i++) {
            if (q.isFake && i == 3) {
                edges[3] = edges[2];
            } else {
                e = q.edgeList[i];

                if (e != e1 && e != e2) {
                    if (e.hasNode(edges[0].leftNode)) {
                        edges[1] = e;
                    } else if (e.hasNode(edges[0].rightNode)) {
                        edges[2] = e;
                    } else {
                        edges[3] = e;
                    }
                }
            }
        }

        // A triangle (fake quad) will have edges[2]== edges[3].
        quad = new Quad(edges[0], edges[1], edges[2], edges[3]);
        Msg.debug("Leaving Quad.combine(Quad, ..)");
        return quad;
    }

    /**
     * 合并四边形和三角形，生成新的三角形。
     * 
     * @param t  三角形
     * @param e1 公共边1
     * @param e2 公共边2
     * @return 合并后的新三角形
     */
    public Triangle combine(Triangle t, Edge e1, Edge e2) {
        Msg.debug("Entering Quad.combine(Triangle, ..)");
        Triangle tri;
        Edge e, edges[] = new Edge[3];
        int i;

        edges[0] = null;
        for (i = 0; i < 4; i++) {
            e = edgeList[i];

            if (e != e1 && e != e2) {
                if (edges[0] == null) {
                    edges[0] = e;
                } else if (e.hasNode(edges[0].leftNode)) {
                    edges[1] = e;
                } else {
                    edges[2] = e;
                }
            }
        }

        for (i = 0; i < 3; i++) {
            e = t.edgeList[i];

            if (e != e1 && e != e2) {
                if (e.hasNode(edges[0].leftNode)) {
                    edges[1] = e;
                } else {
                    edges[2] = e;
                }
            }
        }

        tri = new Triangle(edges[0], edges[1], edges[2]);
        Msg.debug("Leaving Quad.combine(Triangle t, ..)");
        return tri;
    }

    /**
     * 获取与该四边形共享指定边的相邻元素。
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
            Msg.warning("Quad.neighbor(Edge): returning null");
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
        if (edgeList[base].leftNode == n) {
            if (edgeList[base] == e) {
                return edgeList[left];
            } else if (edgeList[left] == e) {
                return edgeList[base];
            } else {
                Msg.warning("Quad.neighborEdge(Node n, Edge e): neighbor not found:");
                Msg.error("quad: " + descr() + ", n: " + n.descr() + ", e: " + e.descr());
                return null;
            }
        } else if (edgeList[base].rightNode == n) {
            if (edgeList[base] == e) {
                return edgeList[right];
            } else if (edgeList[right] == e) {
                return edgeList[base];
            } else {
                Msg.warning("Quad.neighborEdge(Node n, Edge e): neighbor not found:");
                Msg.error("quad: " + descr() + ", n: " + n.descr() + ", e: " + e.descr());
                return null;
            }
        } else if (edgeList[left].otherNode(edgeList[base].leftNode) == n) {
            if (isFake) {
                if (edgeList[right] == e) {
                    return edgeList[left];
                } else if (edgeList[left] == e) {
                    return edgeList[right];
                } else {
                    Msg.warning("Quad.neighborEdge(Node n, Edge e): neighbor not found:");
                    Msg.error("quad: " + descr() + ", n: " + n.descr() + ", e: " + e.descr());
                    return null;
                }
            } else {
                if (edgeList[top] == e) {
                    return edgeList[left];
                } else if (edgeList[left] == e) {
                    return edgeList[top];
                } else {
                    Msg.warning("Quad.neighborEdge(Node n, Edge e): neighbor not found:");
                    Msg.error("quad: " + descr() + ", n: " + n.descr() + ", e: " + e.descr());
                    return null;
                }
            }
        } else if (edgeList[right].otherNode(edgeList[base].rightNode) == n) {
            if (edgeList[top] == e) {
                return edgeList[right];
            } else if (edgeList[right] == e) {
                return edgeList[top];
            } else {
                Msg.warning("Quad.neighborEdge(Node n, Edge e): neighbor not found:");
                Msg.error("quad: " + descr() + ", n: " + n.descr() + ", e: " + e.descr());
                return null;
            }
        } else {
            Msg.warning("Quad.neighborEdge(Node n, Edge e): neighbor not found:");
            Msg.error("quad: " + descr() + ", n: " + n.descr() + ", e: " + e.descr());
            return null;
        }
    }

    /**
     * 获取与指定节点相邻的边。
     * 
     * @param n 节点
     * @return 邻边
     */
    public Edge neighborEdge(Node n) {
        if (edgeList[base].leftNode == n) {
            return edgeList[base];
        } else if (edgeList[base].rightNode == n) {
            return edgeList[right];
        } else if (edgeList[left].otherNode(edgeList[base].leftNode) == n) {
            return edgeList[left];
        } else if (edgeList[right].otherNode(edgeList[base].rightNode) == n) {
            return edgeList[top];
        } else {
            Msg.error("Quad.neighborEdge(Node n): neighbor not found.");
            return null;
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
        // Find this edge's index
        int thisEdgeIndex = indexOf(e);

        // Find other edge's index
        int otherEdgeIndex;
        if (edgeList[base] != e && edgeList[base].hasNode(n)) {
            otherEdgeIndex = base;
        } else if (edgeList[left] != e && edgeList[left].hasNode(n)) {
            otherEdgeIndex = left;
        } else if (edgeList[right] != e && edgeList[right].hasNode(n)) {
            otherEdgeIndex = right;
        } else {
            otherEdgeIndex = top;
        }

        // Return correct angle
        return ang[angleIndex(thisEdgeIndex, otherEdgeIndex)];
    }

    /**
     * 获取两条边在角度数组中的索引。
     * 
     * @param e1Index 边1索引
     * @param e2Index 边2索引
     * @return 角度索引
     */
    public int angleIndex(int e1Index, int e2Index) {
        if ((e1Index == base && e2Index == left) || // angle at base, left
                (e1Index == left && e2Index == base)) {
            return 0;
        } else if ((e1Index == base && e2Index == right) || // angle at base,right
                (e1Index == right && e2Index == base)) {
            return 1;
        } else if ((e1Index == top && e2Index == left) || // angle at top, left
                (e1Index == left && e2Index == top)) {
            return 2;
        } else {
            return 3;
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
     * 获取与指定节点相关的角度索引。
     * 
     * @param n 节点
     * @return 角度索引
     */
    @Override
    public int angleIndex(Node n) {
        if (edgeList[base].leftNode == n) {
            return 0;
        } else if (edgeList[base].rightNode == n) {
            return 1;
        } else if (edgeList[left].otherNode(edgeList[base].leftNode) == n) {
            return 2;
        } else if (edgeList[right].otherNode(edgeList[base].rightNode) == n) {
            return 3;
        } else {
            Msg.error("Quad.angleIndex(Node): Node not found");
            return -1;
        }
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
        return ang[angleIndex(indexOf(e1), indexOf(e2))];
    }

    /**
     * 判断四边形是否包含指定节点。
     * 
     * @param n 节点
     * @return 包含返回true，否则返回false
     */
    @Override
    public boolean hasNode(Node n) {
        if (edgeList[base].leftNode.equals(n) || edgeList[base].rightNode.equals(n) || edgeList[top].leftNode.equals(n)
                || edgeList[top].rightNode.equals(n)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形是否包含指定边。
     * 
     * @param e 边
     * @return 包含返回true，否则返回false
     */
    @Override
    public boolean hasEdge(Edge e) {
        if (edgeList[base] == e || edgeList[left] == e || edgeList[right] == e || edgeList[top] == e) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形是否为边界四边形。
     * 
     * @return 是边界四边形返回true，否则返回false
     */
    public boolean boundaryQuad() {
        if (neighbor(edgeList[base]) instanceof Quad || neighbor(edgeList[left]) instanceof Quad
                || neighbor(edgeList[right]) instanceof Quad
                || neighbor(edgeList[top]) instanceof Quad) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形是否为边界菱形（仅有一个节点在边界上）。
     * 
     * @return 是边界菱形返回true，否则返回false
     */
    public boolean boundaryDiamond() {
        int bnodes = 0;
        if (edgeList[base].leftNode.boundaryNode()) {
            bnodes++;
        }
        if (edgeList[base].rightNode.boundaryNode()) {
            bnodes++;
        }
        if (edgeList[top].leftNode.boundaryNode()) {
            bnodes++;
        }
        if (edgeList[top].rightNode.boundaryNode()) {
            bnodes++;
        }

        if (bnodes == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取属于该四边形的任意一个边界节点。
     * 
     * @return 边界节点，若无则返回null
     */
    public Node getBoundaryNode() {
        if (edgeList[base].leftNode.boundaryNode()) {
            return edgeList[base].leftNode;
        } else if (edgeList[base].rightNode.boundaryNode()) {
            return edgeList[base].rightNode;
        } else if (edgeList[top].leftNode.boundaryNode()) {
            return edgeList[top].leftNode;
        } else if (edgeList[top].rightNode.boundaryNode()) {
            return edgeList[top].rightNode;
        } else {
            return null;
        }
    }

    /**
     * 判断四边形面积是否大于0。
     * 
     * @return 面积大于0返回true，否则返回false
     */
    @Override
    public boolean areaLargerThan0() {
        Node node3 = edgeList[top].leftNode;
        Node node4 = edgeList[top].rightNode;

        if (!node3.onLine(edgeList[base]) || !node4.onLine(edgeList[base])) {
            return true;
        }
        return true;
    }

    /**
     * 判断四边形是否为凸四边形。
     * 
     * @return 是凸四边形返回true，否则返回false
     */
    public boolean isConvex() {
        Node n1 = edgeList[base].leftNode;
        Node n2 = edgeList[base].rightNode;
        Node n3 = edgeList[left].otherNode(n1);
        Node n4 = edgeList[right].otherNode(n2);

        MyVector d1 = new MyVector(n1, n4);
        MyVector d2 = new MyVector(n2, n3);
        if (d1.pointIntersects(d2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形是否为严格凸四边形（无三点共线）。
     * 
     * @return 是严格凸四边形返回true，否则返回false
     */
    public boolean isStrictlyConvex() {
        Node n1 = edgeList[base].leftNode;
        Node n2 = edgeList[base].rightNode;
        Node n3 = edgeList[left].otherNode(n1);
        Node n4 = edgeList[right].otherNode(n2);

        MyVector d1 = new MyVector(n1, n4);
        MyVector d2 = new MyVector(n2, n3);
        if (d1.innerpointIntersects(d2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形是否为蝴蝶形（有两组对边相交）。
     * 
     * @return 是蝴蝶形返回true，否则返回false
     */
    public boolean isBowtie() {
        Node n1 = edgeList[base].leftNode;
        Node n2 = edgeList[base].rightNode;
        Node n3 = edgeList[left].otherNode(n1);
        Node n4 = edgeList[right].otherNode(n2);

        MyVector d1 = new MyVector(n1, n2);
        MyVector d2 = new MyVector(n3, n4);
        MyVector d3 = new MyVector(n1, n3);
        MyVector d4 = new MyVector(n2, n4);
        if (d1.pointIntersects(d2) || d3.pointIntersects(d4)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形是否为"人字形"（最大角大于200度）。
     * 
     * @return 是人字形返回true，否则返回false
     */
    public boolean isChevron() {
        if (largestAngle() >= CHEVRONMIN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断四边形最大角是否大于180度。
     * 
     * @return 大于180度返回true，否则返回false
     */
    public boolean largestAngleGT180() {
        if (largestAngle() > DEG_180) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取下一个逆时针节点。
     * 
     * @param n 当前节点
     * @return 下一个逆时针节点
     */
    public Node nextCCWNodeOld(Node n) {
        MyVector v0, v1;
        Node n0, n1;

        if (n == edgeList[base].leftNode) {
            n0 = edgeList[base].rightNode;
            n1 = edgeList[left].otherNode(n);
        } else if (n == edgeList[base].rightNode) {
            n0 = edgeList[right].otherNode(n);
            n1 = edgeList[base].leftNode;
        } else if (n == edgeList[left].otherNode(edgeList[base].leftNode)) {
            n0 = edgeList[base].leftNode;
            n1 = edgeList[right].otherNode(edgeList[base].rightNode);
        } else if (n == edgeList[right].otherNode(edgeList[base].rightNode)) {
            n0 = edgeList[base].rightNode;
            n1 = edgeList[left].otherNode(edgeList[base].leftNode);
        } else {
            return null;
        }

        v0 = new MyVector(n, n0);
        v1 = new MyVector(n, n1);
        if (v1.isCWto(v0)) {
            return n1;
        } else {
            return n0;
        }
    }

    /**
     * 获取下一个逆时针节点（推荐使用）。
     * 
     * @param n 当前节点
     * @return 下一个逆时针节点
     */
    public Node nextCCWNode(Node n) {
        Node n2, n3, n4;
        Edge e2, e3, e4;
        if (n == firstNode) {
            return edgeList[base].otherNode(firstNode); // n2
        } else {
            n2 = edgeList[base].otherNode(firstNode);
            e2 = neighborEdge(n2, edgeList[base]);
            if (n == n2) {
                return e2.otherNode(n2); // n3
            } else {
                n3 = e2.otherNode(n2);
                e3 = neighborEdge(n3, e2);
                if (n == n3) {
                    return e3.otherNode(n3); // n4
                } else {
                    n4 = e3.otherNode(n3);
                    if (n == n4) {
                        return firstNode;
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * 更新左右边的指向和角度数组顺序。
     */
    public void updateLR() {
        Msg.debug("Entering Quad.updateLR()");
        Edge temp;
        double dt0, dt1, dt2, dt3;
        if (!edgeList[left].hasNode(edgeList[base].leftNode)) {
            Msg.debug("...updating");
            temp = edgeList[left];
            edgeList[left] = edgeList[right];
            edgeList[right] = temp;

            dt0 = ang[0];
            dt1 = ang[1];
            dt2 = ang[2];
            dt3 = ang[3];

            ang[0] = dt1;
            ang[1] = dt0;
            ang[2] = dt3;
            ang[3] = dt2;
        }
        Msg.debug("Leaving Quad.updateLR()");
    }

    /**
     * 更新角度数组的值。
     */
    @Override
    public void updateAngles() {
        if (isFake) {
            if (firstNode == edgeList[base].leftNode) {
                ang[0] = edgeList[base].computeCCWAngle(edgeList[left]);
                ang[1] = edgeList[right].computeCCWAngle(edgeList[base]);
                ang[2] = edgeList[left].computeCCWAngle(edgeList[right]);
            } else {
                ang[0] = edgeList[left].computeCCWAngle(edgeList[base]);
                ang[1] = edgeList[base].computeCCWAngle(edgeList[right]);
                ang[2] = edgeList[right].computeCCWAngle(edgeList[left]);
            }
        } else if (firstNode == edgeList[base].leftNode) {
            ang[0] = edgeList[base].computeCCWAngle(edgeList[left]);
            ang[1] = edgeList[right].computeCCWAngle(edgeList[base]);
            ang[2] = edgeList[left].computeCCWAngle(edgeList[top]);
            ang[3] = edgeList[top].computeCCWAngle(edgeList[right]);
        } else {
            ang[0] = edgeList[left].computeCCWAngle(edgeList[base]);
            ang[1] = edgeList[base].computeCCWAngle(edgeList[right]);
            ang[2] = edgeList[top].computeCCWAngle(edgeList[left]);
            ang[3] = edgeList[right].computeCCWAngle(edgeList[top]);
        }
    }

    /**
     * 更新除指定节点外的角度数组。
     * 
     * @param n 要排除的节点
     */
    public void updateAnglesExcept(Node n) {
        int i = angleIndex(n);
        if (isFake) {
            if (firstNode == edgeList[base].leftNode) {
                if (i != 0) {
                    ang[0] = edgeList[base].computeCCWAngle(edgeList[left]);
                }
                if (i != 1) {
                    ang[1] = edgeList[right].computeCCWAngle(edgeList[base]);
                }
                if (i != 2) {
                    ang[2] = edgeList[left].computeCCWAngle(edgeList[right]);
                }
            } else {
                if (i != 0) {
                    ang[0] = edgeList[left].computeCCWAngle(edgeList[base]);
                }
                if (i != 1) {
                    ang[1] = edgeList[base].computeCCWAngle(edgeList[right]);
                }
                if (i != 2) {
                    ang[2] = edgeList[right].computeCCWAngle(edgeList[left]);
                }
            }
        } else if (firstNode == edgeList[base].leftNode) {
            if (i != 0) {
                ang[0] = edgeList[base].computeCCWAngle(edgeList[left]);
            }
            if (i != 1) {
                ang[1] = edgeList[right].computeCCWAngle(edgeList[base]);
            }
            if (i != 2) {
                ang[2] = edgeList[left].computeCCWAngle(edgeList[top]);
            }
            if (i != 3) {
                ang[3] = edgeList[top].computeCCWAngle(edgeList[right]);
            }
        } else {
            if (i != 0) {
                ang[0] = edgeList[left].computeCCWAngle(edgeList[base]);
            }
            if (i != 1) {
                ang[1] = edgeList[base].computeCCWAngle(edgeList[right]);
            }
            if (i != 2) {
                ang[2] = edgeList[top].computeCCWAngle(edgeList[left]);
            }
            if (i != 3) {
                ang[3] = edgeList[right].computeCCWAngle(edgeList[top]);
            }
        }
    }

    /**
     * 更新与指定节点相关的角度。
     * 
     * @param n 节点
     */
    @Override
    public void updateAngle(Node n) {
        int i = angleIndex(n);

        if (isFake) {
            if (firstNode == edgeList[base].leftNode) {
                if (i == 0) {
                    ang[0] = edgeList[base].computeCCWAngle(edgeList[left]);
                } else if (i == 1) {
                    ang[1] = edgeList[right].computeCCWAngle(edgeList[base]);
                } else if (i == 2) {
                    ang[2] = edgeList[left].computeCCWAngle(edgeList[right]);
                }
            } else {
                if (i == 0) {
                    ang[0] = edgeList[left].computeCCWAngle(edgeList[base]);
                } else if (i == 1) {
                    ang[1] = edgeList[base].computeCCWAngle(edgeList[right]);
                } else if (i == 2) {
                    ang[2] = edgeList[right].computeCCWAngle(edgeList[left]);
                }
            }
        } else if (firstNode == edgeList[base].leftNode) {
            if (i == 0) {
                ang[0] = edgeList[base].computeCCWAngle(edgeList[left]);
            } else if (i == 1) {
                ang[1] = edgeList[right].computeCCWAngle(edgeList[base]);
            } else if (i == 2) {
                ang[2] = edgeList[left].computeCCWAngle(edgeList[top]);
            } else if (i == 3) {
                ang[3] = edgeList[top].computeCCWAngle(edgeList[right]);
            }
        } else {
            if (i == 0) {
                ang[0] = edgeList[left].computeCCWAngle(edgeList[base]);
            } else if (i == 1) {
                ang[1] = edgeList[base].computeCCWAngle(edgeList[right]);
            } else if (i == 2) {
                ang[2] = edgeList[top].computeCCWAngle(edgeList[left]);
            } else if (i == 3) {
                ang[3] = edgeList[right].computeCCWAngle(edgeList[top]);
            }
        }
    }

    /**
     * 判断四边形是否反转。
     * 
     * @return 反转返回true，否则返回false
     */
    @Override
    public boolean inverted() {
        Msg.debug("Entering Quad.inverted()");
        if (isFake) {

            Node a, b, c;
            a = firstNode;
            b = edgeList[base].otherNode(a);
            if (a == edgeList[base].leftNode) {
                c = edgeList[left].otherNode(a);
            } else {
                c = edgeList[right].otherNode(a);
            }

            Msg.debug("Leaving Quad.inverted() (fake)");
            if (cross(a, c, b, c) < 0) {
                return true;
            } else {
                return false;
            }
        }

        Node a = edgeList[base].leftNode, b = edgeList[base].rightNode, c = edgeList[right].otherNode(b),
                d = edgeList[left].otherNode(a);

        // We need at least 3 okays to be certain that this quad is not inverted
        int okays = 0;
        if (cross(a, d, b, d) > 0) {
            okays++;
        }
        if (cross(a, c, b, c) > 0) {
            okays++;
        }
        if (cross(c, a, d, a) > 0) {
            okays++;
        }
        if (cross(c, b, d, b) > 0) {
            okays++;
        }

        if (b == firstNode) {
            okays = 4 - okays;
        }

        Msg.debug("Leaving Quad.inverted(), okays: " + okays);
        if (okays >= 3) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断四边形是否反转或面积为零。
     * 
     * @return 反转或零面积返回true，否则返回false
     */
    @Override
    public boolean invertedOrZeroArea() {
        Msg.debug("Entering Quad.invertedOrZeroArea()");
        if (isFake) {

            Node a, b, c;
            a = firstNode;
            b = edgeList[base].otherNode(a);
            if (a == edgeList[base].leftNode) {
                c = edgeList[left].otherNode(a);
            } else {
                c = edgeList[right].otherNode(a);
            }

            Msg.debug("Leaving Quad.invertedOrZeroArea() (fake)");
            if (cross(a, c, b, c) <= 0) {
                return true;
            } else {
                return false;
            }
        }

        Node a = edgeList[base].leftNode, b = edgeList[base].rightNode, c = edgeList[right].otherNode(b),
                d = edgeList[left].otherNode(a);

        // We need at least 3 okays to be certain that this quad is not inverted
        int okays = 0;
        if (cross(a, d, b, d) >= 0) {
            okays++;
        }
        if (cross(a, c, b, c) >= 0) {
            okays++;
        }
        if (cross(c, a, d, a) >= 0) {
            okays++;
        }
        if (cross(c, b, d, b) >= 0) {
            okays++;
        }

        if (b == firstNode) {
            okays = 4 - okays;
        }

        Msg.debug("Leaving Quad.invertedOrZeroArea(), okays: " + okays);
        if (okays >= 3) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断指定节点处是否为凹点（角度大于180度）。
     * 
     * @param n 节点
     * @return 是凹点返回true，否则返回false
     */
    @Override
    public boolean concavityAt(Node n) {
        Msg.debug("Entering Quad.concavityAt(..)");
        if (ang[angleIndex(n)] >= Math.PI) {
            Msg.debug("Leaving Quad.concavityAt(..), returning true");
            return true;
        } else {
            Msg.debug("Leaving Quad.concavityAt(..), returning false");
            return false;
        }
    }

    /**
     * 获取四边形的中心点。
     * 
     * @return 中心节点
     */
    public Node centroid() {
        double x = 0, y = 0;
        Node bL = edgeList[base].leftNode, bR = edgeList[base].rightNode;
        Node uL = edgeList[left].otherNode(bL), uR = edgeList[right].otherNode(bR);

        if (isFake) {
            x = (bL.x + uL.x + bR.x) / 3.0;
            y = (bL.y + uL.y + bR.y) / 3.0;
            return new Node(x, y);
        } else {
            if (concavityAt(bL)) {
                x = (bL.x + uR.x) / 2.0;
                y = (bL.y + uR.y) / 2.0;
            } else if (concavityAt(bR)) {
                x = (bR.x + uL.x) / 2.0;
                y = (bR.y + uL.y) / 2.0;
            } else if (concavityAt(uL)) {
                x = (uL.x + bR.x) / 2.0;
                y = (uL.y + bR.y) / 2.0;
            } else if (concavityAt(uR)) {
                x = (uR.x + bL.x) / 2.0;
                y = (uR.y + bL.y) / 2.0;
            } else {
                x = (bL.x + uL.x + uR.x + bR.x) / 4.0;
                y = (bL.y + uL.y + uR.y + bR.y) / 4.0;

            }
            return new Node(x, y);
        }
    }

    /**
     * 获取与指定节点相对的节点。
     * 
     * @param n 节点
     * @return 对节点
     */
    public Node oppositeNode(Node n) {
        // 2 out of 4 edges has Node n, so at least 1 out of 3 edge must have it, too:
        Edge startEdge;
        if (edgeList[0].hasNode(n)) {
            startEdge = edgeList[0];
        } else if (edgeList[1].hasNode(n)) {
            startEdge = edgeList[1];
        } else if (edgeList[2].hasNode(n)) {
            startEdge = edgeList[2];
        } else {
            return null; // Most likely, Node n is not part of this Quad.
        }

        Node n2 = startEdge.otherNode(n);
        Edge e = neighborEdge(n2, startEdge);
        return e.otherNode(n2);
    }

    /**
     * 获取顺时针方向上与指定节点相对的边。
     * 
     * @param n 节点
     * @return 对边
     */
    public Edge cwOppositeEdge(Node n) {
        if (n == edgeList[base].leftNode) {
            return edgeList[right];
        } else if (n == edgeList[base].rightNode) {
            return edgeList[top];
        } else if (n == edgeList[left].otherNode(edgeList[base].leftNode)) {
            return edgeList[base];
        } else if (n == edgeList[right].otherNode(edgeList[base].rightNode)) {
            return edgeList[left];
        } else {
            return null;
        }
    }

    /**
     * 获取逆时针方向上与指定节点相对的边。
     * 
     * @param n 节点
     * @return 对边
     */
    public Edge ccwOppositeEdge(Node n) {
        if (n == edgeList[base].leftNode) {
            return edgeList[top];
        } else if (n == edgeList[base].rightNode) {
            return edgeList[left];
        } else if (n == edgeList[left].otherNode(edgeList[base].leftNode)) {
            return edgeList[right];
        } else if (n == edgeList[right].otherNode(edgeList[base].rightNode)) {
            return edgeList[base];
        } else {
            return null;
        }
    }

    /**
     * 获取与指定边相对的边。
     * 
     * @param e 边
     * @return 对边
     */
    public Edge oppositeEdge(Edge e) {
        if (e == edgeList[base]) {
            return edgeList[top];
        } else if (e == edgeList[left]) {
            return edgeList[right];
        } else if (e == edgeList[right]) {
            return edgeList[left];
        } else if (e == edgeList[top]) {
            return edgeList[base];
        } else {
            return null;
        }
    }

    /**
     * 判断相邻四边形是否反转。
     * 
     * @return 存在反转返回true，否则返回false
     */
    public boolean invertedNeighbors() {
        Node uLNode = edgeList[left].otherNode(edgeList[base].leftNode);
        Node uRNode = edgeList[right].otherNode(edgeList[base].rightNode);
        Element curElem;
        Edge curEdge;

        // Parse all adjacent elements at upper left node from neigbor of left edge to,
        // but not including, neighbor of top edge.
        curElem = neighbor(edgeList[left]);
        curEdge = edgeList[left];
        while (curElem != null && curEdge != edgeList[top]) {
            if (curElem.inverted()) {
                return true;
            }
            curEdge = curElem.neighborEdge(uLNode, curEdge);
            curElem = curElem.neighbor(curEdge);
        }

        // Parse all adjacent elements at upper right node from neigbor of top edge to,
        // but not including, neighbor of right edge.
        curElem = neighbor(edgeList[top]);
        curEdge = edgeList[top];
        while (curElem != null && curEdge != edgeList[right]) {
            if (curElem.inverted()) {
                return true;
            }
            curEdge = curElem.neighborEdge(uRNode, curEdge);
            curElem = curElem.neighbor(curEdge);
        }

        // Parse all adjacent elements at lower right node from neigbor of right edge
        // to,
        // but not including, neighbor of base edge.
        curElem = neighbor(edgeList[right]);
        curEdge = edgeList[right];
        while (curElem != null && curEdge != edgeList[base]) {
            if (curElem.inverted()) {
                return true;
            }
            curEdge = curElem.neighborEdge(edgeList[base].rightNode, curEdge);
            curElem = curElem.neighbor(curEdge);
        }

        // Parse all adjacent elements at lower left node from neigbor of base edge to,
        // but not including, neighbor of left edge.
        curElem = neighbor(edgeList[base]);
        curEdge = edgeList[base];
        while (curElem != null && curEdge != edgeList[left]) {
            if (curElem.inverted()) {
                return true;
            }
            curEdge = curElem.neighborEdge(edgeList[base].leftNode, curEdge);
            curElem = curElem.neighbor(curEdge);
        }
        return false;
    }

    /**
     * 获取与该四边形相邻的所有三角形。
     * 
     * @return 相邻三角形列表
     */
    public List<Triangle> getAdjTriangles() {
        List<Triangle> triangleList;
        Node uLNode = edgeList[left].otherNode(edgeList[base].leftNode);
        Node uRNode = edgeList[right].otherNode(edgeList[base].rightNode);
        Node bLNode = edgeList[base].leftNode;
        Node bRNode = edgeList[base].rightNode;

        triangleList = bLNode.adjTriangles();

        // Parse all adjacent elements at upper left node from, but not not including,
        // neigbor of left edge to, but not including, neighbor of top edge.
        Element curElem = neighbor(edgeList[left]);
        Edge curEdge = edgeList[left];
        if (curElem != null) {
            curEdge = curElem.neighborEdge(uLNode, curEdge);
            curElem = curElem.neighbor(curEdge);

            while (curElem != null && curEdge != edgeList[top]) {
                if (curElem instanceof Triangle) {
                    triangleList.add((Triangle) curElem);
                }
                curEdge = curElem.neighborEdge(uLNode, curEdge);
                curElem = curElem.neighbor(curEdge);
            }
        }
        // Parse all adjacent elements at upper right node from neigbor of top edge to,
        // but not including, neighbor of right edge.
        curElem = neighbor(edgeList[top]);
        curEdge = edgeList[top];
        while (curElem != null && curEdge != edgeList[right]) {
            if (curElem instanceof Triangle && !triangleList.contains(curElem)) {
                triangleList.add((Triangle) curElem);
            }
            curEdge = curElem.neighborEdge(uRNode, curEdge);
            curElem = curElem.neighbor(curEdge);
        }

        triangleList.addAll(bRNode.adjTriangles());

        return triangleList;
    }

    /**
     * 获取与该四边形相邻的所有节点。
     * 
     * @return 相邻节点列表
     */
    public List<Node> getAdjNodes() {
        List<Node> nodeList = new ArrayList<>();
        Edge e;
        Node n;
        Node bLNode = edgeList[base].leftNode;
        Node bRNode = edgeList[base].rightNode;
        Node uLNode = edgeList[left].otherNode(bLNode);

        int i;

        for (i = 0; i < bLNode.edgeList.size(); i++) {
            e = bLNode.edgeList.get(i);
            if (e != edgeList[base] && e != edgeList[left] && e != edgeList[right] && e != edgeList[top]) {
                nodeList.add(e.otherNode(bLNode));
            }
        }

        for (i = 0; i < bRNode.edgeList.size(); i++) {
            e = bRNode.edgeList.get(i);
            if (e != edgeList[base] && e != edgeList[left] && e != edgeList[right] && e != edgeList[top]) {
                n = e.otherNode(bRNode);
                if (!nodeList.contains(n)) {
                    nodeList.add(n);
                }
            }
        }

        for (i = 0; i < uLNode.edgeList.size(); i++) {
            e = uLNode.edgeList.get(i);
            if (e != edgeList[base] && e != edgeList[left] && e != edgeList[right] && e != edgeList[top]) {
                n = e.otherNode(uLNode);
                if (!nodeList.contains(n)) {
                    nodeList.add(n);
                }
            }
        }

        if (!isFake) {
            Node uRNode = edgeList[right].otherNode(bRNode);
            for (i = 0; i < uRNode.edgeList.size(); i++) {
                e = uRNode.edgeList.get(i);
                if (e != edgeList[base] && e != edgeList[left] && e != edgeList[right] && e != edgeList[top]) {
                    n = e.otherNode(uRNode);
                    if (!nodeList.contains(n)) {
                        nodeList.add(n);
                    }
                }
            }
        }
        return nodeList;
    }

    /**
     * 替换四边形中的某条边。
     * 
     * @param e           原边
     * @param replacement 替换边
     */
    @Override
    public void replaceEdge(Edge e, Edge replacement) {
        edgeList[indexOf(e)] = replacement;
    }

    /**
     * 让四边形的四条边都指向该四边形。
     */
    @Override
    public void connectEdges() {
        edgeList[base].connectToQuad(this);
        edgeList[left].connectToQuad(this);
        edgeList[right].connectToQuad(this);
        if (!isFake) {
            edgeList[top].connectToQuad(this);
        }
    }

    /**
     * 断开四边形四条边与该四边形的连接。
     */
    @Override
    public void disconnectEdges() {
        edgeList[base].disconnectFromElement(this);
        edgeList[left].disconnectFromElement(this);
        edgeList[right].disconnectFromElement(this);
        if (!isFake) {
            edgeList[top].disconnectFromElement(this);
        }
    }

    /**
     * 获取与另一个四边形在指定节点处的公共边。
     * 
     * @param n 节点
     * @param q 另一个四边形
     * @return 公共边，若无则返回null
     */
    public Edge commonEdgeAt(Node n, Quad q) {
        for (Edge e : n.edgeList) {
            if (hasEdge(e) && q.hasEdge(e)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 获取与另一个四边形的公共边。
     * 
     * @param q 另一个四边形
     * @return 公共边，若无则返回null
     */
    public Edge commonEdge(Quad q) {
        if (q == neighbor(edgeList[base])) {
            return edgeList[base];
        } else if (q == neighbor(edgeList[left])) {
            return edgeList[left];
        } else if (q == neighbor(edgeList[right])) {
            return edgeList[right];
        } else if (q == neighbor(edgeList[top])) {
            return edgeList[top];
        } else {
            return null;
        }
    }

    /**
     * 判断指定节点处是否有前边。
     * 
     * @param n 节点
     * @return 有前边返回true，否则返回false
     */
    public boolean hasFrontEdgeAt(Node n) {
        if (edgeList[left].hasNode(n)) {
            if (edgeList[base].hasNode(n)) {
                if (edgeList[left].isFrontEdge() || edgeList[base].isFrontEdge()) {
                    return true;
                } else {
                    return false;
                }
            } else if (edgeList[top].hasNode(n)) {
                if (edgeList[left].isFrontEdge() || edgeList[top].isFrontEdge()) {
                    return true;
                } else {
                    return false;
                }
            }
        } else if (edgeList[right].hasNode(n)) {
            if (edgeList[base].hasNode(n)) {
                if (edgeList[right].isFrontEdge() || edgeList[base].isFrontEdge()) {
                    return true;
                } else {
                    return false;
                }
            } else if (edgeList[top].hasNode(n)) {
                if (edgeList[right].isFrontEdge() || edgeList[top].isFrontEdge()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 获取在指定节点处与该四边形共享边的四边形数量。
     * 
     * @param n 节点
     * @return 共享边的四边形数量
     */
    public int nrOfQuadsSharingAnEdgeAt(Node n) {
        int count = 0;

        if (edgeList[left].hasNode(n)) {
            if (neighbor(edgeList[left]) instanceof Quad) {
                count++;
            }
            if (edgeList[base].hasNode(n)) {
                if (neighbor(edgeList[base]) instanceof Quad) {
                    count++;
                }
            } else if (neighbor(edgeList[top]) instanceof Quad) {
                count++;
            }
            return count;
        } else if (edgeList[right].hasNode(n)) {
            if (neighbor(edgeList[right]) instanceof Quad) {
                count++;
            }
            if (edgeList[base].hasNode(n)) {
                if (neighbor(edgeList[base]) instanceof Quad) {
                    count++;
                }
            } else if (neighbor(edgeList[top]) instanceof Quad) {
                count++;
            }

            return count;
        }
        return count;
    }

    /**
     * 更新四边形的畸变度量。
     * 参考文献：An approach to Combined Laplacian and Optimization-Based Smoothing for
     * Triangular, Quadrilateral and Quad-Dominant Meshes
     */
    //
    // 这是一个简单的四边形示意图，展示了节点和四个三角形的划分：
    //
    // n3__________n4
    // |\ /|
    // | \ t4 / |
    // | \ / |
    // | t2 X t3 |
    // | / \ |
    // | / t1 \ |
    // |/___________\|
    // n1 n2
    //
    // 另外，我还尝试绘制了一个角度大于180度的四边形情况
    // 注意在图中t3是t1的一部分，t4是t2的一部分
    // t3和t4是倒置的
    //
    // n3
    // |\ \
    // | \ \
    // | \t4 X
    // |t2 \ / \
    // | /\ t3 \
    // | /t1 ---__n2
    // |/______-----=
    // n1
    @Override
    public void updateDistortionMetric() {
        Msg.debug("Entering Quad.updateDistortionMetric()");

        if (isFake) {
            double AB = edgeList[base].len, CB = edgeList[left].len, CA = edgeList[right].len;

            Node a = edgeList[base].commonNode(edgeList[right]), b = edgeList[base].commonNode(edgeList[left]),
                    c = edgeList[left].commonNode(edgeList[right]);
            MyVector vCA = new MyVector(c, a), vCB = new MyVector(c, b);

            double temp = sqrt3x2 * Math.abs(vCA.cross(vCB)) / (CA * CA + AB * AB + CB * CB);
            if (inverted()) {
                distortionMetric = -temp;
            } else {
                distortionMetric = temp;
            }

            Msg.debug("Leaving Quad.updateDistortionMetric(): " + distortionMetric);
            return;
        }

        Node n1 = edgeList[base].leftNode;
        Node n2 = edgeList[base].rightNode;
        Node n3 = edgeList[left].otherNode(n1);
        Node n4 = edgeList[right].otherNode(n2);

        // The two diagonals
        Edge e1 = new Edge(n1, n4);
        Edge e2 = new Edge(n2, n3);

        // The four triangles
        Triangle t1 = new Triangle(edgeList[base], edgeList[left], e2);
        Triangle t2 = new Triangle(edgeList[base], e1, edgeList[right]);
        Triangle t3 = new Triangle(edgeList[top], edgeList[right], e2);
        Triangle t4 = new Triangle(edgeList[top], e1, edgeList[left]);

        // Place the firstNodes correctly
        t1.firstNode = firstNode;
        if (firstNode == n1) {
            t2.firstNode = n1;
            t3.firstNode = n4;
            t4.firstNode = n4;
        } else {
            t2.firstNode = n2;
            t3.firstNode = n3;
            t4.firstNode = n3;
        }

        // Compute and get alpha values for each triangle
        t1.updateDistortionMetric(4.0);
        t2.updateDistortionMetric(4.0);
        t3.updateDistortionMetric(4.0);
        t4.updateDistortionMetric(4.0);

        double alpha1 = t1.distortionMetric, alpha2 = t2.distortionMetric, alpha3 = t3.distortionMetric,
                alpha4 = t4.distortionMetric;

        int invCount = 0;
        if (alpha1 < 0) {
            invCount++;
        }
        if (alpha2 < 0) {
            invCount++;
        }
        if (alpha3 < 0) {
            invCount++;
        }
        if (alpha4 < 0) {
            invCount++;
        }

        double temp12 = Math.min(alpha1, alpha2);
        double temp34 = Math.min(alpha3, alpha4);
        double alphaMin = Math.min(temp12, temp34);
        double negval = 0;

        if (invCount >= 3) {
            if (invCount == 3) {
                negval = 2.0;
            } else {
                negval = 3.0;
            }
        } else if (ang[0] < DEG_6 || ang[1] < DEG_6 || ang[2] < DEG_6 || ang[3] < DEG_6
                || coincidentNodes(n1, n2, n3, n4) || invCount == 2) {
            negval = 1.0;
        }

        distortionMetric = alphaMin - negval;
        Msg.debug("Leaving Quad.updateDistortionMetric(): " + distortionMetric);
    }

    /**
     * 判断四个节点中是否有重合。
     * 
     * @param n1 节点1
     * @param n2 节点2
     * @param n3 节点3
     * @param n4 节点4
     * @return 有重合返回true，否则返回false
     */
    private boolean coincidentNodes(Node n1, Node n2, Node n3, Node n4) {
        Msg.debug("Entering Quad.coincidentNodes(..)");
        double x12diff = n2.x - n1.x;
        double y12diff = n2.y - n1.y;
        double x13diff = n3.x - n1.x;
        double y13diff = n3.y - n1.y;
        double x14diff = n4.x - n1.x;
        double y14diff = n4.y - n1.y;

        double x23diff = n3.x - n2.x;
        double y23diff = n3.y - n2.y;
        double x24diff = n4.x - n2.x;
        double y24diff = n4.y - n2.y;

        double x34diff = n4.x - n3.x;
        double y34diff = n4.y - n3.y;

        // Using Pythagoras: hyp^2= kat1^2 + kat2^2
        double l12 = Math.sqrt(x12diff * x12diff + y12diff * y12diff);
        double l13 = Math.sqrt(x13diff * x13diff + y13diff * y13diff);
        double l14 = Math.sqrt(x14diff * x14diff + y14diff * y14diff);
        double l23 = Math.sqrt(x23diff * x23diff + y23diff * y23diff);
        double l24 = Math.sqrt(x24diff * x24diff + y24diff * y24diff);
        double l34 = Math.sqrt(x34diff * x34diff + y34diff * y34diff);

        if (l12 < COINCTOL || l13 < COINCTOL || l14 < COINCTOL || l23 < COINCTOL || l24 < COINCTOL || l34 < COINCTOL) {
            Msg.debug("Leaving Quad.coincidentNodes(..), returning true");
            return true;
        } else {
            Msg.debug("Leaving Quad.coincidentNodes(..), returning false");
            return false;
        }
    }

    /**
     * 获取四边形的最大内角。
     * 
     * @return 最大内角
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
        if (ang[3] > cand) {
            cand = ang[3];
        }
        return cand;
    }

    /**
     * 获取最大内角对应的节点。
     * 
     * @return 最大内角节点
     */
    @Override
    public Node nodeAtLargestAngle() {
        Node candNode = edgeList[base].leftNode;
        double cand = ang[0];

        if (ang[1] > cand) {
            candNode = edgeList[base].rightNode;
            cand = ang[1];
        }
        if (ang[2] > cand) {
            candNode = edgeList[left].otherNode(edgeList[base].leftNode);
            cand = ang[2];
        }
        if (ang[3] > cand) {
            candNode = edgeList[right].otherNode(edgeList[base].rightNode);
        }
        return candNode;
    }

    /**
     * 获取四边形最长边的长度。
     * 
     * @return 最长边长度
     */
    @Override
    public double longestEdgeLength() {
        double t1 = Math.max(edgeList[base].len, edgeList[left].len);
        double t2 = Math.max(t1, edgeList[right].len);
        return Math.max(t2, edgeList[top].len);
    }

    /**
     * 获取四边形内部包含的所有三角形。
     * 
     * @param first 内部三角形
     * @return 包含的三角形列表
     */
    public ArrayList trianglesContained(Triangle first) {
        Msg.debug("Entering trianglesContained(..)");
        ArrayList tris = new ArrayList();
        Element neighbor;
        Triangle cur;
        Edge e;

        tris.add(first);
        for (int j = 0; j < tris.size(); j++) {
            cur = (Triangle) tris.get(j);
            Msg.debug("...parsing triangle " + cur.descr());

            for (int i = 0; i < 3; i++) {
                e = cur.edgeList[i];
                if (!hasEdge(e)) {
                    neighbor = cur.neighbor(e);
                    if (neighbor != null && !tris.contains(neighbor)) {
                        tris.add(neighbor);
                    }
                }
            }
        }
        Msg.debug("Leaving trianglesContained(..)");
        return tris;
    }

    /**
     * 判断四边形内部是否包含空洞。
     * 
     * @param tris 内部三角形列表
     * @return 存在空洞返回true，否则返回false
     */
    public boolean containsHole(ArrayList tris) {
        Triangle t;

        if (tris.size() == 0) {
            return true; // Corresponds to a quad defined on a quad-shaped hole
        }

        for (Object element : tris) {
            t = (Triangle) element;
            if (t.edgeList[0].boundaryEdge() && !hasEdge(t.edgeList[0])
                    || t.edgeList[1].boundaryEdge() && !hasEdge(t.edgeList[1])
                    || t.edgeList[2].boundaryEdge() && !hasEdge(t.edgeList[2])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将四边形的四条边颜色标记为合法（绿色）。
     */
    @Override
    public void markEdgesLegal() {
        edgeList[base].color = java.awt.Color.green;
        edgeList[left].color = java.awt.Color.green;
        edgeList[right].color = java.awt.Color.green;
        edgeList[top].color = java.awt.Color.green;
    }

    /**
     * 将四边形的四条边颜色标记为非法（红色）。
     */
    @Override
    public void markEdgesIllegal() {
        edgeList[base].color = java.awt.Color.red;
        edgeList[left].color = java.awt.Color.red;
        edgeList[right].color = java.awt.Color.red;
        edgeList[top].color = java.awt.Color.red;
    }

    /**
     * 获取四边形的字符串描述。
     * 
     * @return 描述字符串
     */
    @Override
    public String descr() {
        Node node1, node2, node3, node4;
        node1 = edgeList[base].leftNode;
        node2 = edgeList[base].rightNode;
        node3 = edgeList[left].otherNode(node1);
        node4 = edgeList[right].otherNode(node2);

        return node1.descr() + ", " + node2.descr() + ", " + node3.descr() + ", " + node4.descr();
    }

    /**
     * 打印四边形的描述信息。
     */
    @Override
    public void printMe() {
        System.out.println(descr() + ", inverted(): " + inverted() + ", ang[0]: " + Math.toDegrees(ang[0])
                + ", ang[1]: " + Math.toDegrees(ang[1])
                + ", ang[2]: " + Math.toDegrees(ang[2]) + ", ang[3]: " + Math.toDegrees(ang[3]) + ", firstNode is "
                + firstNode.descr());
    }
}
