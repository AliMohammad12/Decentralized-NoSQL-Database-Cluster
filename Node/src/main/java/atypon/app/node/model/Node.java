package atypon.app.node.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

public class Node {
    private static String nodeId;
    private static String nodeName;
    public static String getNodeName() {
        return nodeName;
    }
    public static void setNodeName(String nodeName) {
        Node.nodeName = nodeName;
    }
    public static String getNodeId() {
        return nodeId;
    }
    public static void setNodeId(String nodeIdValue) {
        nodeId = nodeIdValue;
    }
}