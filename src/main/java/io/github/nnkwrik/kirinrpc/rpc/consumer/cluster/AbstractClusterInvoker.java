package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.AbstractMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author nnkwrik
 * @date 19/05/31 15:53
 */
public abstract class AbstractClusterInvoker implements ClusterInvoker {
    //id
    private final AtomicLong aLong = new AtomicLong(0);//TODO atomicLong?

    private final LoadBalancer loadBalancer;

    public AbstractClusterInvoker(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    protected long newId() {
        return aLong.addAndGet(1);
    }

    //TODO 需要优化。每次选择时provider名都会发生变化，所以每次都需要进行依次序列化。
    protected Channel select(RequestPayload payload, KirinRequest request) {
        AbstractMap.SimpleEntry<String, Channel> entry = loadBalancer.select(request.getServiceMeta());
        String provider = entry.getKey();
        Channel connection = entry.getValue();

        //把payload中的providerName值设置为新选择的
        request.setProviderName(provider);
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(request);
        payload.bytes(bytes);

        return connection;
    }

    protected <T> RPCFuture<T> write(Channel connection, RequestPayload payload) {

        CountDownLatch latch = new CountDownLatch(1);

        connection.writeAndFlush(payload).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                latch.countDown();
            }
        });

        try {
            latch.await();
            return new RPCFuture(payload.id());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
