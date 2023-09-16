package atypon.app.node.kafka.event.locking;

import atypon.app.node.kafka.event.WriteEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RemoveLockEvent extends WriteEvent {
    private String key;
    public RemoveLockEvent(String key) {
        this.key = key;
    }
}
