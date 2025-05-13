package org.tim.qmorph.viewer;

import javax.swing.JFrame;

/**
 * 给用户输出信息
 */

public class Msg {
    public static boolean debugMode = false;

    /** 输出错误信息，然后关闭程序 */
    public static void error(String err) {
        JFrame f = new JFrame();
        MsgDialog errorDialog;
        Error error = new Error(err);
        error.printStackTrace();

        errorDialog = new MsgDialog(f, "程序错误", "程序发生错误。\n详细信息请查看日志文件。", 40, 2);
        errorDialog.setVisible(true);

        System.exit(1);
    }

    /** 输出警告信息 */
    public static void warning(String warn) {
        if (debugMode) {
            System.out.println("警告: " + warn);
        }
    }

    /** Output a debug message. */
    public static void debug(String msg) {
        if (debugMode) {
            System.out.println("调试: " + msg);
        }
    }
}
