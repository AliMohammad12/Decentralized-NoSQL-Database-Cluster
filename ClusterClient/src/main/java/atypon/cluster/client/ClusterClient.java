package atypon.cluster.client;

import atypon.cluster.client.service.ClusterCollectionService;
import atypon.cluster.client.testmodels.Employee;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClusterClient {
    public static void main(String[] args) {
        SpringApplication.run(ClusterClient.class, args);
//        ClusterCollectionService clusterCollectionService = new ClusterCollectionService();
//        clusterCollectionService.createCollection(Employee.class);
    }
}