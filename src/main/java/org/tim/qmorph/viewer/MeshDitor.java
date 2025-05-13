package org.tim.qmorph.viewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.tim.qmorph.meshing.GeomBasics;
import org.tim.qmorph.meshing.QMorph;

/**
 * 执行应用的入口，有版本和帮助信息和命令行选项
 * 
 * @author TIM
 * @version 1.0
 */

public class MeshDitor {

    public static void main(String[] args) {
        int count = 0;
        String path, dir, filename;
        GUI gui;
        FileOutputStream fos;
        // Capture Java error messages
        try {
            fos = new FileOutputStream("MeshDitor.log");
            MyFilterOutputStream mfops = new MyFilterOutputStream(fos);
            PrintStream pstream = new PrintStream(mfops, true);
            System.setErr(pstream);
        } catch (Exception e) {
            Msg.error("Can not open file MeshDitor.log (to which errors are logged).");
        }

        // Process command line arguments
        if (args != null) {
            for (count = 0; count < args.length; count++) {
                if (args[count].equals("-help") || args[count].equals("--help")) {
                    outputVersion();
                    outputHelp();
                    System.exit(0);
                } else if (args[count].equals("-version") || args[count].equals("--version")) {
                    outputVersion();
                    System.exit(0);
                } else if (args[count].equals("-noGUI") || args[count].equals("--noGUI")) {
                    if (count < args.length - 1) {
                        filename = args[args.length - 1];
                        GeomBasics.setParams(filename, "", false, false);
                        GeomBasics.loadTriangleMesh();
                        QMorph qm = new QMorph();
                        qm.init();
                        qm.run();
                        GeomBasics.writeQuadMesh("qmesh.dta", GeomBasics.getElementList());
                        System.exit(0);
                    } else {
                        outputVersion();
                        System.out.println("ERROR: You must also supply a filename.");
                        outputHelp();
                        System.exit(0);
                    }
                }
            }
        }

        // Run GUI
        if (count >= 1) {
            path = args[count - 1];

            int i = path.lastIndexOf(File.separator);
            if (i != -1) {
                dir = path.substring(0, i + 1);
                filename = path.substring(i + 1);
            } else {
                dir = "." + File.separator;
                filename = path;
            }
            gui = new GUI(dir, filename);
            gui.startGUI();
        } else {
            gui = new GUI();
            gui.startGUI();
        }
    }

    private static void outputVersion() {
        System.out.println("MeshDitor v2.0");
    }

    private static void outputHelp() {
        System.out.println("用法:");
        System.out.println("  java -jar meshditor.jar {选项} {网格文件名}");
        System.out.println("选项:");
        System.out.println("  -help       输出版本、用法和所有可用选项，然后退出。");
        System.out.println("  -version    输出版本信息并退出。");
        System.out.println("  -noGUI      不启动图形界面，直接加载指定的网格文件，\n" +
                "              并用默认参数运行 QMorph。结果会写入 'qmesh.dta' 文件。");
    }
}
