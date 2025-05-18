package org.tim.qmorph.meshing;

import org.tim.qmorph.geom.Node;

/**
 * 这个类保存程序的"常量"。也就是说，它们作为
 * Q-Morph实现的参数。
 */

public class Constants {
    /** 一个布尔值，表示是否应该运行三角形到四边形的转换。 */
    public static boolean doTri2QuadConversion = true;
    /** 一个布尔值，表示是否应该运行拓扑清理。 */
    public static boolean doCleanUp = true;
    /** 一个布尔值，表示是否应该运行全局平滑。 */
    public static boolean doSmooth = true;

    /* 一些常见的常量 */
    public static final double sqrt3x2 = 2.0 * Math.sqrt(3.0);

    /* 四边形的常量 */
    public static final int base = 0;
    public static final int left = 1;
    public static final int right = 2;
    public static final int top = 3;

    // Some useful constants involving PI:
    /** PI/6 or 30 degrees */
    public static final double PIdiv6 = java.lang.Math.PI / 6.0;
    /** PI/2 or 90 degrees */
    public static final double PIdiv2 = java.lang.Math.PI / 2.0;
    /** 3*PI/4 or 135 degrees */
    public static final double PIx3div4 = 3 * java.lang.Math.PI / 4.0;
    /** 5*PI/4 or 225 degrees */
    public static final double PIx5div4 = 5 * java.lang.Math.PI / 4.0;
    /** 3*PI/2 or 270 degrees */
    public static final double PIx3div2 = 3 * java.lang.Math.PI / 2.0;
    /** 2*PI or 360 degrees */
    public static final double PIx2 = java.lang.Math.PI * 2.0;

    /* 一些有用的常量，保存常见角度的弧度值 */
    /** 6度对应的弧度 */
    public static final double DEG_6 = Math.toRadians(6);
    /** 150度对应的弧度 */
    public static final double DEG_150 = Math.toRadians(150);
    /** 160度对应的弧度 */
    public static final double DEG_160 = Math.toRadians(160);
    /** 179度对应的弧度 */
    public static final double DEG_179 = Math.toRadians(179);
    /** 180度对应的弧度 */
    public static final double DEG_180 = Math.toRadians(180);
    /** 200度对应的弧度 */
    public static final double DEG_200 = Math.toRadians(200);

    /* 用于接缝、过渡接缝和过渡分割操作的常量 */
    /* 注意我们必须有(EPSILON1 < EPSILON2) */
    public static double EPSILON1 = java.lang.Math.PI * 0.04;
    public static double EPSILON2 = java.lang.Math.PI * 0.09;

    /* 三角形中最大角的最小大小 */
    public static double CHEVRONMIN = DEG_200;

    /* 用于边选择（EPSILON < EPSILONLARGER）的常量 */
    public static final double sqrt3div2 = Math.sqrt(3.0) / 2.0;

    public static final double EPSILON = java.lang.Math.PI / 6.0;
    public static final double EPSILONLARGER = java.lang.Math.PI;

    /* 后处理平滑的常量 */
    /** 节点重合公差 */
    public static double COINCTOL = 0.01;
    /** 移动公差。我不知道它是否有效。 */
    public static double MOVETOLERANCE = 0.01;
    /** OBS公差。应该为0.1，但可能调整为触发OBS。 */
    public static double OBSTOL = 0.1;
    public static double DELTAFACTOR = 0.00001;
    public static double MYMIN = 0.05;
    /** 元素中允许的最大角度（未在论文中给出） */
    public static double THETAMAX = Math.toRadians(200);
    public static double TOL = 0.00001;
    public static double GAMMA = 0.8; // 0.8
    public static int MAXITER = 5;

    /* 现在一些双精度变量来保存上述一些的默认值 */
    public static final double defaultE1Factor = 0.04;
    public static final double defaultE2Factor = 0.09;
    public static final double defaultCHEVRONMIN = DEG_200;
    public static final double defaultCOINCTOL = 0.01; // The node coincidence tolerance
    public static final double defaultMOVETOLERANCE = 0.01; // dunno if it's a good choice
    public static final double defaultOBSTOL = 0.1;// should be 0.1, adjust to trigger OBS
    public static final double defaultDELTAFACTOR = 0.00001;
    public static final double defaultMYMIN = 0.05;
    public static final double defaultTHETAMAX = DEG_200;
    public static final double defaultTOL = 0.00001;
    public static final double defaultGAMMA = 0.8; // 0.8
    public static final int defaultMAXITER = 5;

    /** 原点，仅用作参考。 */
    public static final Node origin = new Node(0, 0);
}
