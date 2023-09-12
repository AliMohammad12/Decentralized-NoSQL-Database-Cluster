package atypon.app.client.controller;

import atypon.app.client.model.Employee;
import atypon.cluster.client.service.ClusterCollectionService;
import atypon.cluster.client.service.ClusterConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BankController {
    private final ClusterCollectionService clusterCollectionService;
    @Autowired
    public BankController(ClusterCollectionService clusterCollectionService) {
        this.clusterCollectionService = clusterCollectionService;
    }
    @GetMapping("/bank")
    public void bank() {
        clusterCollectionService.createCollection(Employee.class);

    }
}
