package atypon.cluster.controller;
import atypon.cluster.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RequestMapping("/cluster")
public class ClusterController {
    private boolean isClusterRunning = false;
    private final ClusterService clusterService;
    @Autowired
    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }
    @PostMapping("/boot")
    public String bootCluster(Model model) {
        boolean success = clusterService.bootCluster();
        if (!success) {
            model.addAttribute("errorMessage", "Error Occurred! Unable to start the cluster.");
            return "cluster-status";
        }
        isClusterRunning = true;
        return "redirect:/cluster/status";
    }

    @PostMapping("/stop")
    public String stopCluster(Model model) {
        boolean success = clusterService.stopCluster();
        if (!success) {
            model.addAttribute("errorMessage", "Error Occurred! Unable to stop the cluster.");
            return "cluster-status";
        }
        isClusterRunning = false;
        return "redirect:/cluster/status";
    }

    @GetMapping("/status")
    public String getStatus(Model model) {
        if (isClusterRunning) {
            model.addAttribute("clusterStatus", clusterService.clusterStatus());
        } else {
            model.addAttribute("clusterStatus", null);
        }
        return "cluster-status";
    }
}
