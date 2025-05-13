package org.tim.qmorph.viewer;

import org.tim.qmorph.geom.Edge;
import org.tim.qmorph.meshing.GeomBasics;
import org.tim.qmorph.meshing.QMorph;

/**
 * 该类实现了命令行用户界面。它读取并解释命令行参数，
 * 设置相应的全局可用变量，并启动主类。
 *
 * 当前实现仅支持二维三角形网格。
 *
 * @author TIM
 */

public class UI {
    public static void main(String[] args) {

        boolean lengthsOpt = false, anglesOpt = false;
        String filename = null;
        int count = 0;
        for (count = 0; count < args.length; count++) {
            if (args[count].equals("-angles")) {
                anglesOpt = true;
            } else if (args[count].equals("-lengths")) {
                lengthsOpt = true;
                // Other options might be:
                // -sf Smaller edges to be processed first
            }
        }

        if (count >= 1) {
            filename = args[count - 1];
        }

        GeomBasics.setParams(filename, ".", lengthsOpt, anglesOpt);
        startMorpher();
    }

    public static void startMorpher() {
        Msg.debug("starting...");

        GeomBasics.loadTriangleMesh();
        QMorph qm = new QMorph();
        GeomBasics.writeQuadMesh("qmesh.dta", GeomBasics.getElementList());

        Msg.debug("frontList:");
        GeomBasics.printEdgeList(qm.getFrontList());
        Msg.debug("edgeList:");
        GeomBasics.printEdgeList(GeomBasics.getEdgeList());
        Edge.printStateLists();
        GeomBasics.printQuads(GeomBasics.getElementList());
        Msg.debug("Done!");
    }
}
