package atypon.cluster.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "bank.app")
public class ClusterClient {
    public static void main(String[] args) {
        SpringApplication.run(ClusterClient.class, args);
    }
}