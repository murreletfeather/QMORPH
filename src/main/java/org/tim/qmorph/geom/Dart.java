package org.tim.qmorph.geom;

/**
 * Dart类表示一个Dart，即一个节点、一条边和一个元素的组合。
 * 
 * @author Tim
 */
public class Dart {
    /**
     * 默认构造函数，初始化Dart对象
     */
    public Dart() {
        n = null;
        e = null;
        elem = null;
    }

    /**
     * 带参数的构造函数，初始化Dart对象
     * 
     * @param n    节点
     * @param e    边
     * @param elem 元素
     */
    public Dart(Node n, Edge e, Element elem) {
        this.n = n;
        this.e = e;
        this.elem = elem;
    }

    /**
     * 描述Dart对象的字符串表示
     * 
     * @return Dart对象的描述字符串
     */
    public String descr() {
        return "(elem: " + elem.descr() + ", e: " + e.descr() + ", n: " + n.descr() + ")";
    }

    public Node n;
    public Edge e;
    public Element elem;

}