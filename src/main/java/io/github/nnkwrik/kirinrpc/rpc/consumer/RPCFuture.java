package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author nnkwrik
 * @date 19/05/28 8:25
 */
public class RPCFuture implements Future<KirinResponse> {

    private volatile KirinRequest request;
    private volatile KirinResponse response;

    private long requestId;

    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public RPCFuture(KirinRequest request, long requestId) {
        this.request = request;
        this.requestId = requestId;
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
        return response != null ? true : false;
    }

    @Override
    public KirinResponse get() throws InterruptedException, ExecutionException {
        lock.lock();
        try {
            while (response == null) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
        return response;
    }

    @Override
    public KirinResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long remain = unit.convert(timeout, TimeUnit.NANOSECONDS);
        lock.lock();
        try {
            while ((remain = condition.awaitNanos(remain)) <= 0) {
                if (response != null) {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return response;
    }

    public void done(KirinResponse response) {
        this.response = response;
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

}
