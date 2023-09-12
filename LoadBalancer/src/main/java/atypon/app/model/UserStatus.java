package atypon.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatus {
    private NodeInfo nodeInfo;
    private int connectedDevices;
}
