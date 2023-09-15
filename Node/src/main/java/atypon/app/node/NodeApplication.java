package atypon.app.node;


import atypon.app.node.model.Node;
import atypon.app.node.utility.DiskOperations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);

        if (!DiskOperations.isFileExists("Storage/"+ Node.getName() + "/Users.json")) {
            String content = "[]";
            DiskOperations.writeToFile(content, "Storage/" + Node.getName(), "Users.json");
        }
    }
}
