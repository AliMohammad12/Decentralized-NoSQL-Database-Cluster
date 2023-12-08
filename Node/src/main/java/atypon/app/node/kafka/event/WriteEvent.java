package atypon.app.node.kafka.event;

import lombok.Data;

@Data
public abstract class WriteEvent {
    protected String username;
}

