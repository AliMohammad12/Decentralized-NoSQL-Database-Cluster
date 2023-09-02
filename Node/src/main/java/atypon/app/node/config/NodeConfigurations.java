package atypon.app.node.config;

import atypon.app.node.model.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeConfigurations {
    @Value("${node_id}")
    private String nodeId;
    @Value("${node_name}")
    private String nodeName;
    @PostConstruct
    public void init() {
        Node.setNodeId(nodeId);
        Node.setName(nodeName);
    }
}