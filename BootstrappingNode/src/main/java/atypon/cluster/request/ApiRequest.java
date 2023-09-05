package atypon.cluster.request;

import lombok.Data;

@Data
public abstract class ApiRequest {
    private boolean isBroadcast = false;
}
