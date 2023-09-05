package atypon.app.request;

import lombok.Data;

@Data
public abstract class ApiRequest {
    private boolean isBroadcast = false;
}
