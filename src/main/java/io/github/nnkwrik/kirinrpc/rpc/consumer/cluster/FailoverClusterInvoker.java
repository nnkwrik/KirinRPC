package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutionException;

/**
 * 失败自动切换, 当出现失败, 重试其它服务器
 *
 * @author nnkwrik
 * @date 19/05/31 14:40
 */
public class FailoverClusterInvoker extends AbstractClusterInvoker {

    private final int retries; // 重试次数, 不包含第一次

    public FailoverClusterInvoker(LoadBalancer loadBalancer, int retries) {
        super(loadBalancer);
        if (retries >= 0) {
            this.retries = retries;
        } else {
            this.retries = 2;
        }
    }

    @Override
    public <T> RPCFuture<T> invoke(KirinRequest request) throws ExecutionException, InterruptedException {
        RequestPayload payload = new RequestPayload(newId());

        return doInvoke(payload, request, retries, null);
    }

    private <T> RPCFuture<T> doInvoke(RequestPayload payload,
                                      KirinRequest request,
                                      int remain,
                                      RPCFuture<T> lasFuture) throws ExecutionException, InterruptedException {
        if (remain < 0) return lasFuture;
        Channel chanel = select(payload, request);
        RPCFuture future = write(chanel, payload);
        future.get();
        switch (future.status()) {
            case SUCCESS:
                return future;
        }
        return doInvoke(payload, request, remain - 1, future);
    }


}
