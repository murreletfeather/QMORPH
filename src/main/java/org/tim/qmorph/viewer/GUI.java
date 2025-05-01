package org.tim.qmorph.viewer;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import javax.swing.JFrame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import org.tim.qmorph.geom.Edge;
import org.tim.qmorph.geom.Element;
import org.tim.qmorph.geom.Node;
import org.tim.qmorph.geom.Quad;
import org.tim.qmorph.geom.Triangle;
import org.tim.qmorph.meshing.Constants;
import org.tim.qmorph.meshing.DelaunayMeshGen;
import org.tim.qmorph.meshing.GeomBasics;
import org.tim.qmorph.meshing.GlobalSmooth;
import org.tim.qmorph.meshing.QMorph;
import org.tim.qmorph.meshing.TopoCleanup;

/** This class implements the graphical user interface. */
public class GUI extends Constants implements ActionListener, ItemListener {

    /** Create frame, set font */
    public GUI() {
        f = new JFrame("网格编辑器");
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setIconImage(null);
        GeomBasics.createNewLists();
    }

    /** Create frame, set font, instantiate QMorph */
    public GUI(String dir, String filename) {
        f = new JFrame("网格编辑器: " + filename);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setIconImage(null);

        this.filename = filename;
        GeomBasics.setParams(filename, dir, false, false);
        GeomBasics.loadMesh();
        GeomBasics.findExtremeNodes();
    }

    /** The filename of the current mesh. */
    public String filename;
    /** Boolean indicating that we are currently defining nodes. */
    public boolean nodeMode = false;
    /** Boolean indicating that we are currently defining triangles. */
    public boolean triangleMode = true;
    /** Boolean indicating that we are currently defining quads. */
    public boolean quadMode = false;
    /** Boolean indicating visibility for the grid */
    public boolean grid = true;
    /** Boolean indicating visibility for the axis */
    public boolean axis = true;

    /** Pointer to an instance of the QMorph class. */
    public QMorph qm = null;
    /** Pointer to an instance of the DelaunayMeshGen class. */
    public DelaunayMeshGen tri = null;

    JFrame f;
    private GCanvas cvas;
    public GControls gctrls;
    private ScrollPane sp;
    private MenuBar mb;

    MenuItem mi;
    Menu fileMenu, editMenu, modeMenu, debugMenu, runMenu, helpMenu;
    int width = 1280, height = 720;
    int scale = 100;
    MyMouseListener myMouseListener;

    MenuItem newItem, loadMeshItem, loadNodesItem, saveItem, saveAsItem, saveNodesItem, saveNodesAsItem, saveTriAsItem,
            exportItem, exitItem;
    MenuItem undoItem, clearEdgesItem;
    CheckboxMenuItem nodeModeItem, triModeItem, quadModeItem, debugModeItem, stepModeItem;
    MenuItem consistencyItem, detectInversionItem, printElementsItem, printTrianglesItem, reportMetricsItem,
            printValencesItem, printValPatItem,
            printAngAtSurNodesItem, centroidItem, triCountItem, delauneyItem, qmorphItem, globalCleanUpItem,
            globalSmoothItem, helpItem, aboutItem;

    MenuShortcut qkey;

    /** Start up the GUI. */
    public void startGUI() {
        f.setSize(width, height);
        f.setLocationRelativeTo(null);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0); // Exit the application when the window is closed
            }
        });

        fileMenu = new Menu("File");
        newItem = new MenuItem("New");
        loadMeshItem = new MenuItem("Load mesh");
        loadNodesItem = new MenuItem("Load nodes");
        saveItem = new MenuItem("Save mesh");
        saveAsItem = new MenuItem("Save mesh as...");
        saveNodesItem = new MenuItem("Save nodes");
        saveNodesAsItem = new MenuItem("Save nodes as...");
        saveTriAsItem = new MenuItem("Save triangle mesh as...");
        exportItem = new MenuItem("Export mesh to LaTeX file");
        exitItem = new MenuItem("Exit");
        qkey = new MenuShortcut(KeyEvent.VK_Q, false);
        exitItem.setShortcut(qkey);

        newItem.addActionListener(this);
        loadMeshItem.addActionListener(this);
        loadNodesItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveNodesItem.addActionListener(this);
        saveNodesAsItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        saveTriAsItem.addActionListener(this);
        exportItem.addActionListener(this);
        exitItem.addActionListener(this);

        fileMenu.add(newItem);
        fileMenu.add(loadMeshItem);
        fileMenu.add(loadNodesItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(saveNodesItem);
        fileMenu.add(saveNodesAsItem);
        fileMenu.add(saveTriAsItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        editMenu = new Menu("Edit");
        undoItem = new MenuItem("Undo last node/edge creation or move");
        undoItem.addActionListener(this);
        editMenu.add(undoItem);
        clearEdgesItem = new MenuItem("Clear all edges");
        clearEdgesItem.addActionListener(this);
        editMenu.add(clearEdgesItem);

        modeMenu = new Menu("Mode");

        nodeModeItem = new CheckboxMenuItem("Plot nodes");
        nodeModeItem.setState(false);
        nodeModeItem.addItemListener(this);
        modeMenu.add(nodeModeItem);

        triModeItem = new CheckboxMenuItem("Construct triangles");
        triModeItem.setState(true);
        triModeItem.addItemListener(this);
        modeMenu.add(triModeItem);
        quadModeItem = new CheckboxMenuItem("Construct quads");
        quadModeItem.setState(false);
        quadModeItem.addItemListener(this);
        modeMenu.add(quadModeItem);
        modeMenu.addSeparator();
        debugModeItem = new CheckboxMenuItem("Debug mode");
        debugModeItem.setState(Msg.debugMode);
        debugModeItem.addItemListener(this);
        modeMenu.add(debugModeItem);
        stepModeItem = new CheckboxMenuItem("Step mode");
        stepModeItem.setState(false);
        stepModeItem.addItemListener(this);
        modeMenu.add(stepModeItem);

        debugMenu = new Menu("Debug");
        consistencyItem = new MenuItem("Test consistency of mesh");
        consistencyItem.addActionListener(this);
        detectInversionItem = new MenuItem("Detect inverted elements");
        detectInversionItem.addActionListener(this);
        printTrianglesItem = new MenuItem("Print triangleList");
        printTrianglesItem.addActionListener(this);
        printElementsItem = new MenuItem("Print elementList");
        printElementsItem.addActionListener(this);
        reportMetricsItem = new MenuItem("Report mesh metrics");
        reportMetricsItem.addActionListener(this);
        printValencesItem = new MenuItem("Print valences of all nodes");
        printValencesItem.addActionListener(this);
        printValPatItem = new MenuItem("Print valence patterns of all nodes");
        printValPatItem.addActionListener(this);
        printAngAtSurNodesItem = new MenuItem("Print angles at surrounding nodes");
        printAngAtSurNodesItem.addActionListener(this);

        centroidItem = new MenuItem("Create centroid for last quad");
        centroidItem.addActionListener(this);
        triCountItem = new MenuItem("Count triangles");
        triCountItem.addActionListener(this);

        debugMenu.add(consistencyItem);
        debugMenu.add(detectInversionItem);
        debugMenu.add(printTrianglesItem);
        debugMenu.add(printElementsItem);
        debugMenu.add(reportMetricsItem);
        debugMenu.add(printValencesItem);
        debugMenu.add(printValPatItem);
        debugMenu.add(printAngAtSurNodesItem);
        debugMenu.add(centroidItem);
        debugMenu.add(triCountItem);

        runMenu = new Menu("Run");
        qmorphItem = new MenuItem("Run QMorph");
        delauneyItem = new MenuItem("Run Delauney generator");
        delauneyItem.addActionListener(this);
        qmorphItem.addActionListener(this);

        runMenu.add(qmorphItem);
        runMenu.add(delauneyItem);

        helpMenu = new Menu("Help");
        helpItem = new MenuItem("Help");
        aboutItem = new MenuItem("About");
        helpItem.addActionListener(this);
        aboutItem.addActionListener(this);

        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        mb = new MenuBar();
        mb.add(fileMenu);
        mb.add(editMenu);
        mb.add(modeMenu);
        mb.add(debugMenu);
        mb.add(runMenu);
        mb.setHelpMenu(helpMenu);

        f.setMenuBar(mb);

        f.setBackground(Color.lightGray);
        f.setForeground(Color.black);

        if (GeomBasics.leftmost == null) {
            cvas = new GCanvas(this, scale);
        } else {
            cvas = new GCanvas(this, GeomBasics.leftmost.x, GeomBasics.lowermost.y, GeomBasics.rightmost.x,
                    GeomBasics.uppermost.y, scale);
        }

        myMouseListener = new MyMouseListener();
        cvas.addMouseListener(myMouseListener);
        cvas.addMouseMotionListener(myMouseListener); // 添加鼠标移动事件监听

        // 添加窗口大小改变监听器
        f.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                cvas.autoFit();
            }
        });

        cvas.repaint();

        f.add("South", gctrls = new GControls(this, cvas));
        f.add("Center", sp = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS));
        sp.setForeground(Color.darkGray);
        sp.setBackground(Color.lightGray);

        sp.add(cvas);

        cvas.setForeground(Color.black);
        cvas.setBackground(Color.black);

        // Add mouse wheel listener for zoom
        cvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Get the mouse wheel rotation
                int rotation = e.getWheelRotation();

                // Calculate new scale
                if (rotation < 0) {
                    // Zoom in - increase scale by 10%
                    scale = (int) (scale * 1.1);
                } else {
                    // Zoom out - decrease scale by 10%
                    scale = (int) (scale * 0.9);
                }

                // Ensure scale stays within reasonable bounds
                if (scale < 10)
                    scale = 10;
                if (scale > 400)
                    scale = 400;

                // Update canvas with new scale
                cvas.setScale(scale);

                // Update scale percentage in combo box
                int percentage = (int) ((scale / 100.0) * 100);
                updateScale(percentage);
            }
        });

        f.setVisible(true);
    }

    void commandNew() {
        GeomBasics.clearLists();
        GeomBasics.setParams(null, ".", false, false);
        f.setTitle("网格编辑器:");
        cvas.resize(-10, -10, 10, 10, 100);
        cvas.autoFit();
        cvas.clear();
        qm = null;
    }

    void commandLoadMesh() {
        FileDialog fd = new FileDialog(f, "Load mesh from file", FileDialog.LOAD);
        fd.setDirectory(GeomBasics.meshDirectory);
        fd.show();
        String dir = fd.getDirectory();
        String loadName = fd.getFile();
        if (dir != null && dir != "" && loadName != null && loadName != "") {
            GeomBasics.clearLists();
            cvas.clear();

            GeomBasics.setParams(loadName, dir, false, false);
            GeomBasics.loadMesh();
            f.setTitle("网格编辑器: " + loadName);

            GeomBasics.findExtremeNodes();
            cvas.resize(GeomBasics.leftmost.x, GeomBasics.lowermost.y, GeomBasics.rightmost.x, GeomBasics.uppermost.y,
                    scale);
            cvas.autoFit();
        }
    }

    void commandLoadNodes() {
        FileDialog fd = new FileDialog(f, "Load nodes from file", FileDialog.LOAD);
        fd.setDirectory(GeomBasics.meshDirectory);
        fd.show();
        String dir = fd.getDirectory();
        String loadName = fd.getFile();
        if (dir != null && dir != "" && loadName != null && loadName != "") {
            GeomBasics.clearLists();
            cvas.clear();

            GeomBasics.setParams(loadName, dir, false, false);
            GeomBasics.loadNodes();
            f.setTitle("网格编辑器: " + loadName);

            GeomBasics.findExtremeNodes();
            cvas.resize(GeomBasics.leftmost.x, GeomBasics.lowermost.y, GeomBasics.rightmost.x, GeomBasics.uppermost.y,
                    scale);
            cvas.autoFit();
        }
    }

    void commandSaveMesh() {
        if (filename == null || filename == "") {
            commandSaveMeshAs();
        } else {
            GeomBasics.writeMesh(GeomBasics.meshDirectory + GeomBasics.meshFilename);
        }
    }

    void commandSaveNodes() {
        if (filename == null || filename == "") {
            commandSaveNodesAs();
        } else {
            GeomBasics.writeMesh(GeomBasics.meshDirectory + GeomBasics.meshFilename);
        }
    }

    void commandSaveNodesAs() {
        FileDialog fd = new FileDialog(f, "Save nodes to file", FileDialog.SAVE);
        fd.setDirectory(GeomBasics.meshDirectory);
        fd.show();
        String dir = fd.getDirectory();
        String saveName = fd.getFile();
        if (dir != null && dir != "" && saveName != null && saveName != "") {
            GeomBasics.writeNodes(dir + saveName);
            f.setTitle("网格编辑器: " + saveName);
            filename = dir + saveName;
            GeomBasics.setParams(saveName, dir, false, false);
        }
    }

    void commandSaveMeshAs() {
        FileDialog fd = new FileDialog(f, "Save mesh to file", FileDialog.SAVE);
        fd.setDirectory(GeomBasics.meshDirectory);
        fd.show();
        String dir = fd.getDirectory();
        String saveName = fd.getFile();
        if (dir != null && dir != "" && saveName != null && saveName != "") {
            GeomBasics.writeMesh(dir + saveName);
            f.setTitle("网格编辑器: " + saveName);
            filename = dir + saveName;
            GeomBasics.setParams(saveName, dir, false, false);
        }
    }

    void commandSaveTriangleMeshAs() {
        FileDialog fd = new FileDialog(f, "Save triangle mesh to file", FileDialog.SAVE);
        fd.setDirectory(GeomBasics.meshDirectory);
        fd.show();
        String dir = fd.getDirectory();
        String saveName = fd.getFile();
        if (dir != null && dir != "" && saveName != null && saveName != "") {
            GeomBasics.writeQuadMesh(saveName, GeomBasics.triangleList);
            f.setTitle("网格编辑器: " + saveName);
            filename = saveName;
        }
    }

    void commandExportMeshToLaTeX() {
        int ul;
        double xcorr, ycorr;
        boolean visibleNodes;

        ExportToLaTeXOptionsDialog ed = new ExportToLaTeXOptionsDialog(f, "Set export parameters", true);
        ed.show();

        if (ed.okPressed()) {
            ul = ed.getUnitlength();
            xcorr = ed.getXCorr();
            ycorr = ed.getYCorr();
            visibleNodes = ed.getVisibleNodes();

            FileDialog fd = new FileDialog(f, "Export mesh to file", FileDialog.SAVE);
            fd.setVisible(true);
            String dir = fd.getDirectory();
            String saveName = fd.getFile();
            if (dir != null && dir != "" && saveName != null && saveName != "") {
                GeomBasics.exportMeshToLaTeX(dir + saveName, ul, xcorr, ycorr, visibleNodes);
            }
        }
    }

    void commandUndo() {
        myMouseListener.undo();
        cvas.repaint();
    }

    void commandClearEdges() {
        GeomBasics.clearEdges();
        cvas.repaint();
    }

    void commandNodeMode() {
        nodeMode = true;
        triangleMode = false;
        quadMode = false;
        nodeModeItem.setState(true);
        triModeItem.setState(false);
        quadModeItem.setState(false);
        gctrls.clickStatus.setText("1");
    }

    void commandTriMode() {
        nodeMode = false;
        triangleMode = true;
        quadMode = false;
        nodeModeItem.setState(false);
        triModeItem.setState(true);
        quadModeItem.setState(false);
        gctrls.clickStatus.setText("3");
    }

    void commandQuadMode() {
        nodeMode = false;
        triangleMode = false;
        quadMode = true;
        nodeModeItem.setState(false);
        triModeItem.setState(false);
        quadModeItem.setState(true);
        gctrls.clickStatus.setText("4");
    }

    void commandToggleDebugMode() {
        if (Msg.debugMode) {
            Msg.debugMode = false;
            debugModeItem.setState(false);
        } else {
            Msg.debugMode = true;
            debugModeItem.setState(true);
        }
    }

    void commandToggleStepMode() {
        if (GeomBasics.step) {
            GeomBasics.step = false;
            stepModeItem.setState(false);
        } else {
            GeomBasics.step = true;
            stepModeItem.setState(true);
        }
    }

    void commandQMorph() {
        QMorphOptionsDialog qmod = new QMorphOptionsDialog(f);
        qmod.setSize(qmod.getPreferredSize());
        qmod.show();

        if (qmod.runPressed()) {
            qmod.copyToProgramParameters();
            qm = new QMorph();
            qm.init();

            if (!GeomBasics.step) {
                qm.run();
                GeomBasics.findExtremeNodes();
                cvas.resize(GeomBasics.leftmost.x, GeomBasics.lowermost.y, GeomBasics.rightmost.x,
                        GeomBasics.uppermost.y, scale);
            } else {
                Msg.debug("Running QMorph.run(..) in step mode");
            }
        }
    }

    void commandDelaunay() {
        tri = new DelaunayMeshGen();
        tri.init(true); // false

        if (!GeomBasics.step) {
            /* Straightforwardly run the method */
            tri.run();
            // elementList= tri.getTriangleList(); // tri.incrDelauney(nodeList);
            // edgeList= tri.getEdgeList();
            // nodeList= tri.getNodeList();
            cvas.resize(GeomBasics.leftmost.x, GeomBasics.lowermost.y, GeomBasics.rightmost.x, GeomBasics.uppermost.y,
                    scale);
        } else {
            /* Run method in step mode */
            cvas.resize(GeomBasics.leftmost.x, GeomBasics.lowermost.y, GeomBasics.rightmost.x, GeomBasics.uppermost.y,
                    scale);
        }
    }

    void commandTopoCleanup() {
        if (GeomBasics.topoCleanup == null) {
            GeomBasics.topoCleanup = new TopoCleanup();
        }

        GeomBasics.topoCleanup.init();
        if (!GeomBasics.step) {
            GeomBasics.topoCleanup.run();
            cvas.repaint();
        }
    }

    void commandSmooth() {
        if (GeomBasics.globalSmooth == null) {
            GeomBasics.globalSmooth = new GlobalSmooth();
        }

        GeomBasics.globalSmooth.init();
        GeomBasics.globalSmooth.run();
        cvas.repaint();
    }

    /**
     * Invoked when a registered item change occurs. The item is identified, and the
     * corresponding action is invoked.
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        String command = (String) e.getItem();
        if (command.equals("Plot nodes")) {
            commandNodeMode();
        } else if (command.equals("Construct triangles")) {
            commandTriMode();
        } else if (command.equals("Construct quads")) {
            commandQuadMode();
        } else if (command.equals("Debug mode")) {
            commandToggleDebugMode();
        } else if (command.equals("Step mode")) {
            commandToggleStepMode();
        }
    }

    /**
     * Invoked when a registered action command occurs. The command is identified,
     * and the corresponding action is invoked.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        HelpDialog hd;
        AboutDialog ad;
        MsgDialog rd;

        String command = e.getActionCommand();

        if (command.equals("New")) {
            commandNew();
        } else if (command.equals("Load mesh")) {
            commandLoadMesh();
        } else if (command.equals("Load nodes")) {
            commandLoadNodes();
        } else if (command.equals("Save mesh")) {
            commandSaveMesh();
        } else if (command.equals("Save nodes")) {
            commandSaveNodes();
        } else if (command.equals("Save nodes as...")) {
            commandSaveNodesAs();
        } else if (command.equals("Save mesh as...")) {
            commandSaveMeshAs();
        } else if (command.equals("Save triangle mesh as...")) {
            commandSaveTriangleMeshAs();
        } else if (command.equals("Export mesh to LaTeX file")) {
            commandExportMeshToLaTeX();
        } else if (command.equals("Exit")) {
            System.exit(0);
        } else if (command.equals("Undo last node/edge creation or move")) {
            commandUndo();
        } else if (command.equals("Clear all edges")) {
            commandClearEdges();
        }

        else if (command.equals("Test consistency of mesh")) {
            GeomBasics.consistencyCheck();
        } else if (command.equals("Detect inverted elements")) {
            GeomBasics.detectInvertedElements();
            cvas.repaint();
        } else if (command.equals("Print triangleList")) {
            GeomBasics.printTriangles(GeomBasics.getTriangleList());
        } else if (command.equals("Print elementList")) {
            GeomBasics.printQuads(GeomBasics.getElementList());
        }
        // else if (command.equals("Update mesh metrics")) {
        // GeomBasics.updateMeshMetrics();
        // }
        else if (command.equals("Report mesh metrics")) {
            GeomBasics.updateMeshMetrics();
            rd = new MsgDialog(f, "Mesh Metrics Report", GeomBasics.meshMetricsReport(), 80, 18);
            rd.show();
        } else if (command.equals("Print valences of all nodes")) {
            GeomBasics.printValences();
        } else if (command.equals("Print valence patterns of all nodes")) {
            GeomBasics.printValPatterns();
        } else if (command.equals("Print angles at surrounding nodes")) {
            GeomBasics.printAnglesAtSurrondingNodes();
        } else if (command.equals("Create centroid for last quad")) {

            Node n;
            Quad q;
            Element elem;
            int size = GeomBasics.elementList.size();
            if (size > 0) {
                elem = GeomBasics.elementList.get(size - 1);
                if (elem instanceof Quad) {
                    q = (Quad) elem;
                    n = q.centroid();
                    GeomBasics.nodeList.add(n);
                    cvas.repaint();
                }
            }
        } else if (command.equals("Count triangles")) {
            GeomBasics.countTriangles();
        }

        else if (command.equals("Run QMorph")) {
            commandQMorph();
        } else if (command.equals("Run Delauney generator")) {
            commandDelaunay();
        } else if (command.equals("Run topological cleanup")) {
            commandTopoCleanup();
        } else if (command.equals("Run smooth")) {
            commandSmooth();
        } else if (command.equals("Help")) {
            hd = new HelpDialog(f);
            hd.show();
        } else if (command.equals("About")) {
            ad = new AboutDialog(f);
            ad.show();
        }
    }

    /** A class for handling mouse actions. */
    class MyMouseListener extends MouseAdapter implements MouseMotionListener {
        Node movingNode = null, oldMovingNode = null;
        int nodeCnt = 0;
        Edge edge1, edge2, edge3, edge4;
        Node[] myNodeList = new Node[4];
        Triangle tri;
        Quad q;
        boolean lastActionMergeNodes = false;
        boolean lastActionMoveNode = false;
        boolean lastActionNewNode = false;
        boolean lastActionNewEdge = false;
        boolean lastActionTwoNewEdges = false;
        boolean lastActionNewTriangle = false;
        boolean lastActionNewQuad = false;

        // 添加用于网格平移的变量
        private boolean isPanning = false;
        private int lastMouseX;
        private int lastMouseY;
        private double originalXMin, originalXMax, originalYMin, originalYMax;

        double oldX = 0, oldY = 0;
        int nONewEdges = 0;

        public MyMouseListener() {
        }

        /** Invoked when the mouse has been clicked on a component. */
        @Override
        public void mouseClicked(MouseEvent e) {
            // 如果是右键点击，不执行任何操作
            if (e.getButton() == MouseEvent.BUTTON3) {
                return;
            }

            Msg.debug("Entering mouseClicked(..)");
            Edge b, l, r, t;
            lastActionMoveNode = false;
            lastActionMergeNodes = false;
            lastActionNewNode = false;
            lastActionNewEdge = false;
            lastActionTwoNewEdges = false;
            lastActionNewTriangle = false;
            lastActionNewQuad = false;
            nONewEdges = 0;

            double x = Math.rint(e.getX() / 10.0) * 10;
            double y = Math.rint(e.getY() / 10.0) * 10;
            x = (x - cvas.getYAxisXPos()) / scale;
            y = (y - cvas.getXAxisYPos()) / -scale;
            Node n = new Node(x, y);
            if (!GeomBasics.nodeList.contains(n)) {
                GeomBasics.nodeList.add(n);
                lastActionNewNode = true;
            } else {
                n = GeomBasics.nodeList.get(GeomBasics.nodeList.indexOf(n));
            }

            if (!nodeMode) {
                myNodeList[nodeCnt] = n;
                nodeCnt++;
                if (nodeCnt == 2) {
                    if (myNodeList[0] == myNodeList[1]) {
                        nodeCnt = 1;
                        return;
                    }

                    edge1 = new Edge(myNodeList[0], myNodeList[1]);
                    if (!GeomBasics.edgeList.contains(edge1)) {
                        GeomBasics.edgeList.add(edge1);
                        edge1.connectNodes();
                        nONewEdges++;
                        lastActionNewEdge = true;
                    } else {
                        edge1 = GeomBasics.edgeList.get(GeomBasics.edgeList.indexOf(edge1));
                    }
                } else if (nodeCnt == 3 && triangleMode) {

                    if (myNodeList[2] == myNodeList[0] || myNodeList[2] == myNodeList[1]) {
                        nodeCnt = 2;
                        return;
                    }

                    edge2 = new Edge(myNodeList[1], myNodeList[2]);
                    if (!GeomBasics.edgeList.contains(edge2)) {
                        GeomBasics.edgeList.add(edge2);
                        edge2.connectNodes();
                        nONewEdges++;
                        if (nONewEdges >= 2) {
                            lastActionTwoNewEdges = true;
                            lastActionNewEdge = false;
                        } else {
                            lastActionNewEdge = true;
                        }
                    } else {
                        edge2 = GeomBasics.edgeList.get(GeomBasics.edgeList.indexOf(edge2));
                    }

                    edge3 = new Edge(myNodeList[0], myNodeList[2]);
                    if (!GeomBasics.edgeList.contains(edge3)) {
                        GeomBasics.edgeList.add(edge3);
                        edge3.connectNodes();
                        nONewEdges++;
                        if (nONewEdges >= 2) {
                            lastActionTwoNewEdges = true;
                            lastActionNewEdge = false;
                        } else {
                            lastActionNewEdge = true;
                        }
                    } else {
                        edge3 = GeomBasics.edgeList.get(GeomBasics.edgeList.indexOf(edge3));
                    }

                    tri = new Triangle(edge1, edge2, edge3);

                    if (!GeomBasics.triangleList.contains(tri)) {
                        GeomBasics.triangleList.add(tri);
                        tri.connectEdges();
                        lastActionNewTriangle = true;
                    }
                    nodeCnt = 0;
                } else if (nodeCnt == 3 && quadMode) {
                    if (myNodeList[2] == myNodeList[0] || myNodeList[2] == myNodeList[1]) {
                        nodeCnt = 2;
                        return;
                    }

                    edge2 = new Edge(myNodeList[1], myNodeList[2]);
                    if (!GeomBasics.edgeList.contains(edge2)) {
                        GeomBasics.edgeList.add(edge2);
                        edge2.connectNodes();
                        nONewEdges++;
                        lastActionNewEdge = true;
                    } else {
                        edge2 = GeomBasics.edgeList.get(GeomBasics.edgeList.indexOf(edge2));
                    }
                } else if (nodeCnt == 4 && quadMode) {
                    if (myNodeList[3] == myNodeList[0] || myNodeList[3] == myNodeList[1]
                            || myNodeList[3] == myNodeList[2]) {
                        nodeCnt = 3;
                        return;
                    }

                    edge3 = new Edge(myNodeList[2], myNodeList[3]);
                    if (!GeomBasics.edgeList.contains(edge3)) {
                        GeomBasics.edgeList.add(edge3);
                        edge3.connectNodes();
                        nONewEdges++;
                        if (nONewEdges >= 2) {
                            lastActionTwoNewEdges = true;
                            lastActionNewEdge = false;
                        } else {
                            lastActionNewEdge = true;
                        }
                    } else {
                        edge3 = GeomBasics.edgeList.get(GeomBasics.edgeList.indexOf(edge3));
                    }

                    edge4 = new Edge(myNodeList[0], myNodeList[3]);
                    if (!GeomBasics.edgeList.contains(edge4)) {
                        GeomBasics.edgeList.add(edge4);
                        edge4.connectNodes();
                        nONewEdges++;
                        if (nONewEdges >= 2) {
                            lastActionTwoNewEdges = true;
                            lastActionNewEdge = false;
                        } else {
                            lastActionNewEdge = true;
                        }
                    } else {
                        edge4 = GeomBasics.edgeList.get(GeomBasics.edgeList.indexOf(edge4));
                    }

                    // Decide which is base, left, right, and top:
                    if (edge2.hasNode(edge1.leftNode)) {
                        b = edge1;
                        l = edge2;
                        t = edge3;
                        r = edge4;
                    } else if (edge2.hasNode(edge1.rightNode)) {
                        b = edge1;
                        r = edge2;
                        t = edge3;
                        l = edge4;
                    } else {
                        Msg.error("Weird stuff while creating new quad...");
                        return;
                    }
                    q = new Quad(b, l, r, t);

                    if (!GeomBasics.elementList.contains(q)) {
                        GeomBasics.elementList.add(q);
                        q.connectEdges();
                        /*
                         * b.connectNodes(); l.connectNodes(); r.connectNodes(); t.connectNodes();
                         */
                        lastActionNewQuad = true;
                    }
                    nodeCnt = 0;
                }

                if (nodeMode) {
                    gctrls.clickStatus.setText("1");
                } else if (triangleMode) {
                    gctrls.clickStatus.setText(Integer.toString(3 - nodeCnt));
                } else if (quadMode) {
                    gctrls.clickStatus.setText(Integer.toString(4 - nodeCnt));
                }
            }
            cvas.repaint();
            Msg.debug("Leaving mouseClicked(..)");
        }

        /** Invoked when the mouse is dragged. */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (isPanning) {
                // 计算鼠标移动的距离（在坐标系中的实际距离）
                double dx = (e.getX() - lastMouseX) / (double) scale;
                double dy = (e.getY() - lastMouseY) / (double) scale;

                // 更新坐标范围（注意y轴方向是相反的）
                cvas.xmin = originalXMin - dx;
                cvas.xmax = originalXMax - dx;
                cvas.ymin = originalYMin + dy;
                cvas.ymax = originalYMax + dy;

                // 更新网格和坐标轴位置
                double ymaxXscale = cvas.ymax * scale;
                double xminXscale = cvas.xmin * scale;
                double rounded_ymaxXscale = cvas.signOf(cvas.ymax)
                        * (Math.abs(ymaxXscale) + cvas.gridIncr
                                - Math.IEEEremainder(Math.abs(ymaxXscale), cvas.gridIncr));
                double rounded_xminXscale = cvas.signOf(cvas.xmin)
                        * (Math.abs(xminXscale) + cvas.gridIncr
                                - Math.IEEEremainder(Math.abs(xminXscale), cvas.gridIncr));

                cvas.xaxis_yval = cvas.gridIncr + (int) (rounded_ymaxXscale);
                cvas.yaxis_xval = cvas.gridIncr + (int) (-rounded_xminXscale);

                // 更新网格显示
                cvas.repaint();
                return;
            }

            // 如果不是平移操作，处理其他拖动事件
            if (movingNode != null) {
                double x = Math.rint(e.getX() / 10.0) * 10;
                double y = Math.rint(e.getY() / 10.0) * 10;
                x = (x - cvas.getYAxisXPos()) / scale;
                y = (y - cvas.getXAxisYPos()) / -scale;
                movingNode.setXY(x, y);
                movingNode.update();
                cvas.repaint();
            }
        }

        /** Invoked when a mouse button is pressed. */
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                // 右键按下，开始平移
                isPanning = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                // 保存当前的坐标范围
                originalXMin = cvas.xmin;
                originalXMax = cvas.xmax;
                originalYMin = cvas.ymin;
                originalYMax = cvas.ymax;
                return;
            }

            // 原有的节点移动逻辑
            Msg.debug("Entering mousePressed(..)");
            double x = Math.rint(e.getX() / 10.0) * 10;
            double y = Math.rint(e.getY() / 10.0) * 10;

            x = (x - cvas.getYAxisXPos()) / scale;
            y = (y - cvas.getXAxisYPos()) / -scale;

            movingNode = new Node(x, y);
            int j = GeomBasics.nodeList.indexOf(movingNode);
            if (j != -1) {
                movingNode = GeomBasics.nodeList.get(j);
                oldX = movingNode.x;
                oldY = movingNode.y;
            } else {
                movingNode = null;
            }

            Msg.debug("Leaving mousePressed(..)");
        }

        /** Invoked when a mouse button is released. */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                // 结束平移，保存最后的鼠标位置
                isPanning = false;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                return;
            }

            Msg.debug("Entering mouseReleased(..)");

            Edge ei, ej, oldE;
            Node n, other;
            int ind, j, k;

            double x = Math.rint(e.getX() / 10.0) * 10;
            double y = Math.rint(e.getY() / 10.0) * 10;
            x = (x - cvas.getYAxisXPos()) / scale;
            y = (y - cvas.getXAxisYPos()) / -scale;

            if (movingNode != null && (x != movingNode.x || y != movingNode.y)) {
                oldMovingNode = movingNode.copy();
                ind = GeomBasics.nodeList.indexOf(new Node(x, y));
                movingNode.setXY(x, y);
                movingNode.update();

                if (ind != -1) { // We have to merge the nodes
                    n = GeomBasics.nodeList.get(ind);

                    for (int i = 0; i < n.edgeList.size(); i++) {
                        ei = n.edgeList.get(i);
                        j = movingNode.edgeList.indexOf(ei);
                        if (j == -1) {
                            ei.replaceNode(n, movingNode);
                            movingNode.edgeList.add(ei);
                            // ei.connectTo()
                        } else { // keep only one copy of each edge
                            // if (ei.leftNode== ei.rightNode) {
                            ej = movingNode.edgeList.get(j);

                            if (ej.element1 != null) {
                                ej.element1.replaceEdge(ej, ei);
                                ei.connectToTriangle((Triangle) ej.element1);
                                Msg.debug("Connecting edge " + ei.descr() + " to triangle " + ej.element1.descr());
                            }
                            if (ej.element2 != null) {
                                ej.element2.replaceEdge(ej, ei);
                                ei.connectToTriangle((Triangle) ej.element2);
                                Msg.debug("Connecting edge " + ei.descr() + " to triangle " + ej.element2.descr());
                            }

                            if (ei.element1 != null) {
                                Msg.debug("ei.element1==" + ei.element1.descr());
                            }
                            if (ei.element2 != null) {
                                Msg.debug("ei.element2==" + ei.element2.descr());
                            }

                            movingNode.edgeList.set(j, ei);
                            ei.replaceNode(n, movingNode);
                            other = ei.otherNode(movingNode);

                            // Remove the correct edge from "global" edgelist
                            k = GeomBasics.edgeList.indexOf(ej);
                            oldE = GeomBasics.edgeList.get(k);
                            if (oldE == ei) {
                                k = GeomBasics.edgeList.lastIndexOf(oldE);
                            }
                            GeomBasics.edgeList.remove(k);

                            // Remove the correct edge from other's edgelist
                            k = other.edgeList.indexOf(ej);
                            oldE = other.edgeList.get(k);
                            if (oldE == ei) {
                                k = other.edgeList.lastIndexOf(oldE);
                            }
                            other.edgeList.remove(k);
                            // }
                        }
                    }

                    List<Element> aeList = movingNode.adjElements();
                    for (Element elem : aeList) {
                        elem.updateAngles();
                    }

                    GeomBasics.nodeList.remove(ind);
                    lastActionMergeNodes = true;
                } else {
                    lastActionMergeNodes = false;
                }

                lastActionMoveNode = true;
                lastActionNewNode = false;
                lastActionNewEdge = false;
                lastActionTwoNewEdges = false;
                lastActionNewTriangle = false;

                cvas.repaint();
            }
            Msg.debug("Leaving mouseReleased(..)");
        }

        /** Undo the last mouse action. */
        public void undo() {
            Edge e;
            int j;
            if (lastActionMoveNode) {
                if (lastActionMergeNodes) {
                    for (int i = 0; i < oldMovingNode.edgeList.size(); i++) {
                        e = oldMovingNode.edgeList.get(i);
                        j = movingNode.edgeList.indexOf(e);
                        if (j != -1) {
                            if (!(e.hasNode(movingNode) && e.hasNode(oldMovingNode))) {
                                movingNode.edgeList.remove(j);
                                e.replaceNode(movingNode, oldMovingNode);
                            }
                        } else { // common edge that collapsed because of the merge
                            movingNode.edgeList.add(e);
                            GeomBasics.edgeList.add(e);
                        }
                    }
                    GeomBasics.nodeList.add(oldMovingNode);
                } else {
                    movingNode.setXY(oldX, oldY);
                    movingNode.update();
                }
            }

            else if (triangleMode && (lastActionNewNode || lastActionNewEdge)) {
                if (nodeCnt == 0) {
                    nodeCnt = 2;
                } else {
                    nodeCnt--;
                }
            }

            else if (quadMode && (lastActionNewNode || lastActionNewEdge)) {
                if (nodeCnt == 0) {
                    nodeCnt = 3;
                } else {
                    nodeCnt--;
                }
            }

            if (lastActionNewNode) {
                GeomBasics.nodeList.remove(GeomBasics.nodeList.size() - 1);
            }
            if (lastActionNewEdge) {
                e = GeomBasics.edgeList.get(GeomBasics.edgeList.size() - 1);
                e.disconnectNodes();
                GeomBasics.edgeList.remove(GeomBasics.edgeList.size() - 1);
            } else if (lastActionTwoNewEdges) {
                e = GeomBasics.edgeList.get(GeomBasics.edgeList.size() - 1);
                e.disconnectNodes();
                GeomBasics.edgeList.remove(GeomBasics.edgeList.size() - 1);
                e = GeomBasics.edgeList.get(GeomBasics.edgeList.size() - 1);
                e.disconnectNodes();
                GeomBasics.edgeList.remove(GeomBasics.edgeList.size() - 1);
            }

            if (lastActionNewTriangle) {
                tri.disconnectEdges();
                GeomBasics.triangleList.remove(GeomBasics.triangleList.size() - 1);
            } else if (lastActionNewQuad) {
                q.disconnectEdges();
                GeomBasics.elementList.remove(GeomBasics.elementList.size() - 1);
            }

            lastActionMoveNode = false;
            lastActionNewNode = false;
            lastActionNewEdge = false;
            lastActionTwoNewEdges = false;
            lastActionNewTriangle = false;
            lastActionNewQuad = false;
        }

    }

    public void updateScale(int percentage) {
        if (gctrls != null) {
            gctrls.scaleCombo.setSelectedItem(percentage + "%");
        }
    }

}
