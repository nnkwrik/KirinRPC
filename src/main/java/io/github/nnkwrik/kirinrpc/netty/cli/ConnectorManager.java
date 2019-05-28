package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.consumer.ConsumerProcessor;
import io.github.nnkwrik.kirinrpc.rpc.consumer.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author nnkwrik
 * @date 19/05/27 20:56
 */
public class ConnectorManager {
    private static ConnectorManager connectorManager;
    private NettyConnector connector;

    //address对应的channel连接
    private final ConcurrentMap<RegisterMeta.Address, Channel> addressChannelMap = new ConcurrentHashMap<>();
    //服务和提供该服务提供者channel.
    private final ConcurrentMap<ServiceMeta, Set<Channel>> serviceChannels = new ConcurrentHashMap<>();
    //已发送但还未收到响应的request
    private final ConcurrentMap<Long, RPCFuture> sentRequest = new ConcurrentHashMap<>();

    //id
    private final AtomicLong aLong = new AtomicLong(0);//TODO atomicLong?


    public static ConnectorManager getInstance() {
        if (connectorManager == null) {
            synchronized (ConnectorManager.class) {
                if (connectorManager == null) {
                    connectorManager = new ConnectorManager();
                    connectorManager.connector = new KirinClientConnector(new ConsumerProcessor());
                }
            }
        }
        return connectorManager;
    }

    public Channel createConnectionWithAddress(RegisterMeta.Address address) {
        Channel channel = addressChannelMap.get(address);
        if (addressChannelMap.get(address) == null) {
            Channel newChannel = connector.connect(address.getHost(), address.getPort());
            channel = addressChannelMap.putIfAbsent(address, newChannel);
            if (channel == null) {
                channel = newChannel;
            } else {
                channel.close();
            }
        }
        return channel;
    }

    public Channel closeConnectionWithAddress(RegisterMeta.Address address) {
        Channel channel = addressChannelMap.get(address);
        if (channel != null) {
            channel.close();
        }
        return channel;
    }

    public Set<Channel> getProviderConnections(ServiceMeta serviceMeta) {
        Set<Channel> channels = serviceChannels.get(serviceMeta);
        if (channels == null) {
            Set<Channel> newChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
            channels = serviceChannels.putIfAbsent(serviceMeta, newChannels);
            if (channels == null) {
                channels = newChannels;
            }
        }
        return channels;
    }

    public boolean isAvailable(ServiceMeta meta) {
        if (!getProviderConnections(meta).isEmpty()) {
            return true;
        }
        return false;
    }

    public Channel chooseConnection(ServiceMeta serviceMeta) {
        return (Channel) getProviderConnections(serviceMeta).toArray()[0];
    }

    public RPCFuture sendRequest(Channel connection, KirinRequest request) {
        long id = aLong.addAndGet(1);
        RequestPayload payload = new RequestPayload(id);
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(request);
        payload.bytes(bytes);

        RPCFuture future = new RPCFuture(request, id);
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

        RPCFuture existFuture = sentRequest.putIfAbsent(id, future);
        if (existFuture != null) {
            future = existFuture;
        }

        return future;
    }

    public void receiveResponse(long id, KirinResponse response) {
        RPCFuture future = sentRequest.remove(id);
        if (future != null) {
            future.done(response);
        }
    }

}
