package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.netty.cli.KChannel;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;

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
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(request);
        payload.bytes(bytes);

        return doInvoke(payload, request, retries, null);
    }

    private <T> RPCFuture<T> doInvoke(RequestPayload payload,
                                      KirinRequest request,
                                      int remain,
                                      RPCFuture<T> lastFuture) throws ExecutionException, InterruptedException {
        if (remain < 0) return lastFuture;

        if (lastFuture != null) {
            lastFuture.get();
            switch (lastFuture.status()) {
                case SUCCESS:
                    return lastFuture;
            }
        }
        KChannel chanel = loadBalancer.select(request.getServiceMeta());
        RPCFuture future = chanel.write(payload);

        return doInvoke(payload, request, remain - 1, future);
    }


}
