package atypon.app.model;

public class Node {
    private static String nodeId;
    private static String name;
    private static String port;

    public Node() {
    }
    public static String getNodeId() {
        return nodeId;
    }

    public static void setNodeId(String nodeId) {
        Node.nodeId = nodeId;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        Node.name = name;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        Node.port = port;
    }
}