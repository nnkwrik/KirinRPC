package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/31 15:51
 */
public class FailfastClusterInvoker extends AbstractClusterInvoker {

    public FailfastClusterInvoker(LoadBalancer loadBalancer) {
        super(loadBalancer);
    }

    @Override
    public <T> RPCFuture<T> invoke(KirinRequest request)  {
        RequestPayload payload = new RequestPayload(newId());
        Channel chanel = select(payload, request);
        return write(chanel, payload);
    }
}
