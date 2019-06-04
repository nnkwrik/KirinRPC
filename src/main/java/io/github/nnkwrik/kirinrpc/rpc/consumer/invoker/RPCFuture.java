package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author nnkwrik
 * @date 19/05/28 8:25
 */
public class RPCFuture<T> implements Future<T> {

    private volatile T result;
    private volatile boolean isDone;
    private volatile Status status = Status.NULL;

    private long requestId;

    public static final Map<Long, RPCFuture> sentMsg = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public RPCFuture(long requestId) {
        this.requestId = requestId;
        sentMsg.put(requestId,this);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        lock.lock();
        try {
            while (!isDone) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long remain = unit.convert(timeout, TimeUnit.NANOSECONDS);
        lock.lock();
        try {
            while ((remain = condition.awaitNanos(remain)) <= 0) {
                if (!isDone) {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void done(T result) {
        this.result = result;
        isDone = true;
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public long id(){
        return requestId;
    }

    public void status(Status status) {
        this.status = status;
    }

    public Status status(){
        return status;
    }

    public enum Status {
        NULL,
        SUCCESS,
        FAIL,
        ERROR
    }

}
