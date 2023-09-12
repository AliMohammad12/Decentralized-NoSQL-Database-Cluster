package atypon.app.model;

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

