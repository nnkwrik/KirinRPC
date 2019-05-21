package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;
import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/05/18 15:48
 */
public class ProviderProcessor {

    private final ServiceBeanContainer serviceBeanContainer;

    private static ThreadPoolExecutor threadPoolExecutor;

    public ProviderProcessor(ServiceBeanContainer serviceBeanContainer) {
        this.serviceBeanContainer = serviceBeanContainer;
    }


    public void handleRequest(Channel channel, RequestPayload requestPayload) throws Exception {
        RPCTask task = new RPCTask(this, channel, requestPayload);
        submit(task);
    }

    public void handleException(Channel channel, RequestPayload requestPayload, Throwable cause) {

    }

    private static void submit(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (ProviderProcessor.class) {
                if (threadPoolExecutor == null) {
                    //双重锁创建线程池
                    threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    public ServiceWrapper lookupService(ServiceMeta serviceMeta) {
        return serviceBeanContainer.lookupService(serviceMeta);
    }

}
