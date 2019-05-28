package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.rpc.provider.ProviderProcessor;
import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/05/28 9:59
 */
public class ConsumerProcessor implements ResponseProcessor {

    private static ThreadPoolExecutor executor;

    @Override
    public void handleResponse(Channel channel, ResponsePayload response) throws Exception {
        ConsumerTask task = new ConsumerTask(response);
        submit(task);
    }

    @Override
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private static void submit(Runnable task) {
        if (executor == null) {
            synchronized (ProviderProcessor.class) {
                if (executor == null) {
                    //双重锁创建线程池
                    executor = new ThreadPoolExecutor(16, 16, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        executor.execute(task);
    }
}
