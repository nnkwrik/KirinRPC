package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.ConnectFailedException;
import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author nnkwrik
 * @date 19/05/31 11:02
 */
public class Dispatcher {

    private ConnectorManager connectorManager = ConnectorManager.getInstance();

    //id
    private final AtomicLong aLong = new AtomicLong(0);//TODO atomicLong?

    private AbstractMap.SimpleEntry<String, Channel> selectConnection(ServiceMeta service) {

        Set<Channel> connections = connectorManager.getConnections(service);
        if (connections == null) {
            throw new ConnectFailedException("No provider can provide this service " + service);
        }

        Channel connection = (Channel) connections.toArray()[0];

        Set<String> providers = connectorManager.getProviders(connection);
        if (providers == null) {
            throw new ConnectFailedException("No provider can provide this service " + service);
        }

        String provider = (String) providers.toArray()[0];

        return new AbstractMap.SimpleEntry<>(provider, connection);
    }

    public RPCFuture dispatch(KirinRequest request) {
        AbstractMap.SimpleEntry<String, Channel> entry = selectConnection(request.getServiceMeta());
        String provider = entry.getKey();
        Channel connection = entry.getValue();

        request.setProviderName(provider);

        long id = aLong.addAndGet(1);
        RequestPayload payload = new RequestPayload(id);
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(request);
        payload.bytes(bytes);

        RPCFuture future = new RPCFuture(id);
        CountDownLatch latch = new CountDownLatch(1);

        connection.writeAndFlush(payload).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return future;
    }
}
