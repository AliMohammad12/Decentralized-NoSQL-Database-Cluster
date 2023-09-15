package atypon.app.node.locking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.locks.Lock;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockObject {
    private int TTL;
    private Lock lock;
}
