package atypon.cluster.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.util.EscapedAttributeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ClusterService {
    private final String dockerComposeFile = "docker-compose.yml";
    public boolean bootCluster() {
        try {
            String[] command = {"docker-compose", "-f", dockerComposeFile, "up", "--build", "-d"};
            Process process = Runtime.getRuntime().exec(command);
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean stopCluster() {
        try {
            String[] command = {"docker-compose", "-f", dockerComposeFile, "down"};
            Process process = Runtime.getRuntime().exec(command);
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    public Map<String, String[]> clusterStatus() {
        Map<String, String[]> containerStatuses = new HashMap<>();
        try {
            String[] command = {"docker-compose", "-f", dockerComposeFile, "ps"};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int skipHeader = 0;
            while ((line = reader.readLine()) != null) {
                if (skipHeader < 2) {
                    skipHeader++;
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                String containerName = parts[0];
                String state = parts[parts.length - 2];
                String port = parts[parts.length - 1];
                if (containerName.equals("zookeeper")) {
                    port = parts[parts.length - 3];
                    state = parts[parts.length - 4];
                }
                containerStatuses.put(containerName, new String[]{state, extractPortNumber(port)});
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return containerStatuses;
    }

    public static String extractPortNumber(String input) {
        String[] parts = input.split(":");
        String[] portParts = parts[1].split("->");
        String extractedPort = portParts[0];
        return extractedPort;
    }
}

