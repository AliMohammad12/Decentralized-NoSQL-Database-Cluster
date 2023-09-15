package atypon.app.client;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;


public class LockObject implements Serializable {
    private Lock lock;
    private int TTL;

    public LockObject(Lock lock, int TTL) {
        this.lock = lock;
        this.TTL = TTL;
    }
    public LockObject() {
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }
}
