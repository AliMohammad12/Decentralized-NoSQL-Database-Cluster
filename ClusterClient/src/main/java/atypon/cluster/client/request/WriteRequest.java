package atypon.cluster.client.request;

import atypon.cluster.client.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriteRequest {
    private User user;
    private Object requestData;
    private String endpoint;
}

