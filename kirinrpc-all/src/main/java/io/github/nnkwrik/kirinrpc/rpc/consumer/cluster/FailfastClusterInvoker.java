package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.netty.cli.KChannel;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;

/**
 * 快速失败, 只发起一次调用
 *
 * @author nnkwrik
 * @date 19/05/31 15:51
 */
public class FailfastClusterInvoker extends AbstractClusterInvoker {

    public FailfastClusterInvoker(LoadBalancer loadBalancer) {
        super(loadBalancer);
    }

    @Override
    public <T> RPCFuture<T> invoke(KirinRequest request) {
        RequestPayload payload = new RequestPayload(newId());
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(request);
        payload.bytes(bytes);

        KChannel chanel = loadBalancer.select(request.getServiceMeta());
        return chanel.write(payload);
    }
}
