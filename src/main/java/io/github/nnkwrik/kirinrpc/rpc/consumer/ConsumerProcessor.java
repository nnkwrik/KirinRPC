package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.provider.ProviderProcessor;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/05/28 9:59
 */
@Slf4j
public class ConsumerProcessor implements ResponseProcessor {

    private static ThreadPoolExecutor executor;

    private final ResponseReceiver responseReceiver;

    public ConsumerProcessor() {
        this.responseReceiver = new ResponseReceiverImpl();
    }

    @Override
    public void handleResponse(Channel channel, ResponsePayload response) throws Exception {
        ConsumerTask task = new ConsumerTask(channel, response, responseReceiver);
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

    public static class ResponseReceiverImpl implements ResponseReceiver {

        @Override
        public void receiveSuccessResponse(Channel channel, long requestId, Object result) {
            log.debug("Receive success response for [requestId = {}],result is {}.", requestId, result);
            RPCFuture future = RPCFuture.sentMsg.remove(requestId);
            if (future != null) {
                future.status(RPCFuture.Status.SUCCESS);
                future.done(result);
            }
        }

        @Override
        public void receiveFailResponse(Channel channel, long requestId, KirinRemoteException e) {
            log.debug("Receive fail response for [requestId = {}],exception is {}.", requestId, e);
            RPCFuture future = RPCFuture.sentMsg.remove(requestId);
            if (future != null) {
                future.status(RPCFuture.Status.FAIL);
                future.done(e);
            }
        }

        @Override
        public void receiveErrorResponse(Channel channel, long requestId, KirinRemoteException e) {
            log.debug("Receive error response for [requestId = {}],error is {}.", requestId, e);
            ConnectorManager.getInstance().removeInactiveConnection(channel);
            RPCFuture future = RPCFuture.sentMsg.remove(requestId);
            if (future != null) {
                future.status(RPCFuture.Status.ERROR);
                future.done(e);
            }
        }
    }
}
