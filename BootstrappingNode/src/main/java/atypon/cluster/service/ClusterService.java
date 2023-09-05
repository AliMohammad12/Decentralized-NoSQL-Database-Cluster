package atypon.cluster.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClusterService {
    private final String dockerComposeFile = "docker-compose.yml";
    public boolean bootCluster() {
        try {
            String[] command = {"docker-compose", "-f", dockerComposeFile, "up", "--build", "-d"};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
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
            boolean skipHeader = false;
            while ((line = reader.readLine()) != null) {
                if (!skipHeader) {
                    skipHeader = true;
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    String containerName = parts[0];
                    String cmd = parts[1]+ "  " + parts[2] + "  " + parts[3];
                    String state = parts[4];
                    String ports = parts[5];
                    containerStatuses.put(containerName, new String[]{cmd, state, ports});
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return containerStatuses;
    }

}

