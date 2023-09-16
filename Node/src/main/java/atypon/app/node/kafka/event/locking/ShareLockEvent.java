package atypon.app.node.kafka.event.locking;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class ShareLockEvent extends WriteEvent {
    private String key;
    private int time;
    private String nodeName;
    public ShareLockEvent(String key, int time) {
        this.key = key;
        this.time = time;
        this.nodeName = Node.getName();
    }
}
