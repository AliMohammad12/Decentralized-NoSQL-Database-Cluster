package atypon.app.node;


import atypon.app.node.model.Node;
import atypon.app.node.utility.FileOperations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class NodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);

        if (!FileOperations.isFileExists("Storage/"+ Node.getName() + "/Users.json")) {
            String content = "[]";
            FileOperations.writeJsonAtLocation(content, "Storage/" + Node.getName(), "Users.json");
        }
    }
}
