package atypon.app.node.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfo {
    private String port;
    private String id;
    private String name;
}
