package org.tim.qmorph.meshing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tim.qmorph.geom.Edge;
import org.tim.qmorph.geom.Node;
import org.tim.qmorph.geom.Triangle;
import java.awt.geom.Point2D;

public class MeshLoader {

    public static List<Triangle> triangleList;
    public static List<Edge> edgeList;
    public static List<Node> nodeList;

    private static int cInd;

    /**
     * 从指定目录加载三角形网格数据。
     *
     * @param meshDirectory 网格数据所在的目录路径
     * @param meshFilename  网格数据的文件名
     * @return 包含所有三角形对象的列表
     * @throws Exception 如果在读取文件或处理数据时发生异常，将抛出该异常
     */
    public static List<Triangle> loadTriangleMesh(String meshDirectory, String meshFilename) {
        triangleList = new ArrayList<>();
        edgeList = new ArrayList<>();
        // 使用HashMap代替ArrayList来提高节点查找速度
        Map<Point2D.Double, Node> nodeMap = new HashMap<>();
        List<Node> usNodeList = new ArrayList<>(); // 仍然保持一个列表来维护顺序，如果需要的话，或者可以删除，如果nodeMap足够。

        try (FileInputStream fis = new FileInputStream(meshDirectory + meshFilename);
                BufferedReader in = new BufferedReader(new InputStreamReader(fis))) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                cInd = 0;
                double x1 = nextDouble(inputLine);
                double y1 = nextDouble(inputLine);
                double x2 = nextDouble(inputLine);
                double y2 = nextDouble(inputLine);
                double x3 = nextDouble(inputLine);
                double y3 = nextDouble(inputLine);

                Node node1 = getNode(nodeMap, usNodeList, x1, y1);
                Node node2 = getNode(nodeMap, usNodeList, x2, y2);
                Node node3 = getNode(nodeMap, usNodeList, x3, y3);

                Edge edge1 = getEdge(edgeList, node1, node2);
                Edge edge2 = getEdge(edgeList, node2, node3);
                Edge edge3 = getEdge(edgeList, node1, node3);

                Triangle t = new Triangle(edge1, edge2, edge3);
                t.connectEdges();
                triangleList.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeList = usNodeList;
        return triangleList;
    }

    /**
     * 从三角形坐标数组加载三角形网格。数组应该
     * 是double[n][6]的格式，其中每一行代表一个三角形，
     * 坐标为x1, y1, x2, y2, x3, y3。
     *
     * @param triangleCoordinates 一个包含三角形坐标的二维数组。
     * @return 一个包含三角形对象的列表，或者null如果发生错误（例如，无效的输入数组）。
     */
    public static List<Triangle> loadTriangleMeshFromArray(double[][] triangleCoordinates, boolean meshLenOpt,
            boolean meshAngOpt) {
        triangleList = new ArrayList<>();
        edgeList = new ArrayList<>();
        Map<Point2D.Double, Node> nodeMap = new HashMap<>();
        List<Node> usNodeList = new ArrayList<>();

        for (double[] coords : triangleCoordinates) {
            double x1 = coords[0];
            double y1 = coords[1];
            double x2 = coords[2];
            double y2 = coords[3];
            double x3 = coords[4];
            double y3 = coords[5];

            Node node1 = getNode(nodeMap, usNodeList, x1, y1);
            Node node2 = getNode(nodeMap, usNodeList, x2, y2);
            Node node3 = getNode(nodeMap, usNodeList, x3, y3);

            Edge edge1 = getEdge(edgeList, node1, node2);
            Edge edge2 = getEdge(edgeList, node2, node3);
            Edge edge3 = getEdge(edgeList, node1, node3);

            Triangle t = new Triangle(edge1, edge2, edge3);
            t.connectEdges();
            triangleList.add(t);
        }
        nodeList = usNodeList; // Assign to class level nodeList if needed
        return triangleList;
    }

    /**
     * 根据给定的坐标从节点映射中获取节点，若不存在则创建新节点并添加到节点映射和未使用节点列表中
     *
     * @param nodeMap    节点映射，key为Point2D.Double类型，value为Node类型
     * @param usNodeList 未使用的节点列表
     * @param x          节点的横坐标
     * @param y          节点的纵坐标
     * @return 返回获取或创建的节点
     */
    private static Node getNode(Map<Point2D.Double, Node> nodeMap, List<Node> usNodeList, double x, double y) {
        Point2D.Double point = new Point2D.Double(x, y);
        Node node = nodeMap.get(point);
        if (node == null) {
            node = new Node(x, y);
            nodeMap.put(point, node);
            usNodeList.add(node);
        }
        return node;
    }

    /**
     * 根据给定的节点从边列表中获取边，若不存在则创建新边并添加到边列表中
     *
     * @param edgeList 边列表
     * @param node1    边的第一个节点
     * @param node2    边的第二个节点
     * @return 返回获取或创建的边
     */
    private static Edge getEdge(List<Edge> edgeList, Node node1, Node node2) {
        Edge edge = new Edge(node1, node2);
        int index = edgeList.indexOf(edge); // 假设Edge类有适当的equals()和hashCode()方法
        if (index == -1) {
            edgeList.add(edge);
        } else {
            edge = edgeList.get(index);
        }
        edge.leftNode.connectToEdge(edge);
        edge.rightNode.connectToEdge(edge);
        return edge;
    }

    /**
     * 从输入行中提取下一个双精度浮点数
     *
     * @param inputLine 输入行字符串
     * @return 提取的下一个双精度浮点数
     */
    private static double nextDouble(String inputLine) {
        int startIndex = cInd;
        while (cInd < inputLine.length() && inputLine.charAt(cInd) != ' ' && inputLine.charAt(cInd) != '\t'
                && inputLine.charAt(cInd) != ',') {
            cInd++;
        }
        String doubleStr = inputLine.substring(startIndex, cInd).trim();
        while (cInd < inputLine.length()
                && (inputLine.charAt(cInd) == ' ' || inputLine.charAt(cInd) == '\t' || inputLine.charAt(cInd) == ',')) {
            cInd++;
        }
        return Double.parseDouble(doubleStr);
    }
}