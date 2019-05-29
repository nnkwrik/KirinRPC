package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.ConnectFailedException;
import io.github.nnkwrik.kirinrpc.netty.handler.cli.ConnectionWatchdog;
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

import java.util.AbstractMap;
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
    private final ConcurrentMap<RegisterMeta.Address, Channel> addressChannel = new ConcurrentHashMap<>();
    //共用一个channel的provider集合，address相同的provider会共用一个channel
    private final ConcurrentMap<Channel, Set<String>> channelProviders = new ConcurrentHashMap<>();
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

    /**
     * TODO
     * #createConnectionWithProvider(),#closeConnectionWithProvider()与#replaceInactiveConnection之间可能存在线程问题
     * 暂时全部加上synchronize，但绝对不是好的方案。颗粒度太大了
     *
     * @param name
     * @param address
     * @return
     */
    public synchronized Channel createConnectionWithProvider(String name, RegisterMeta.Address address) {
        Channel channel = addressChannel.get(address);

        if (channel == null) {
            Channel newChannel = connector.connect(address.getHost(), address.getPort());
            channel = addressChannel.putIfAbsent(address, newChannel);
            if (channel == null) {
                channel = newChannel;
            } else {
                channel.close();
            }
        }
        //比如这里就可能会有线程问题。如果这个是旧channel，可能channelProviders已经被replace改为新的channel了。再去get就会拿到的必然是空。
        channel = addressChannel.get(address);
        Set<String> providers = channelProviders.get(channel);
        if (providers == null) {
            Set<String> newProviders = Collections.newSetFromMap(new ConcurrentHashMap<>());
            providers = channelProviders.putIfAbsent(channel, newProviders);
            if (providers == null) {
                providers = newProviders;
            }
        }
        providers.add(name);

        return channel;
    }

    public synchronized Channel closeConnectionWithProvider(String name, RegisterMeta.Address address) {
        Channel channel = addressChannel.get(address);
        Channel removed = null;
        if (channel != null) {
            Set<String> providers = channelProviders.get(channel);
            if (providers != null) {
                providers.remove(name);
                if (providers.isEmpty()) {
                    channelProviders.remove(channel);
                    removed = addressChannel.remove(address);
                    if (removed != null) {
                        removed.close();
                        ConnectionWatchdog.setReconnect(removed, false);
                    }
                }
            }
        }
        return removed;
    }

    public Set<Channel> getServiceConnections(ServiceMeta serviceMeta) {
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
        if (!getServiceConnections(meta).isEmpty()) {
            return true;
        }
        return false;
    }

    public AbstractMap.SimpleEntry<String, Channel> chooseConnection(ServiceMeta serviceMeta) {
        if (getServiceConnections(serviceMeta).size() <= 0) {
            throw new ConnectFailedException("No provider can provide this service " + serviceMeta);
        }
        Channel channel = (Channel) getServiceConnections(serviceMeta).toArray()[0];
        Set<String> providers = channelProviders.get(channel);
        //TODO throw exception
        String appName = (String) providers.toArray()[0];

        return new AbstractMap.SimpleEntry<>(appName, channel);
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

    public synchronized void replaceInactiveConnection(Channel inactive, Channel active) {
        //对addressChannel，serviceChannels，channelProviders中的channel对象进行替换

        for (RegisterMeta.Address address : addressChannel.keySet()) {
            addressChannel.replace(address, inactive, active);
        }


        Set<String> remove = channelProviders.remove(inactive);
        if (remove != null && !remove.isEmpty()) {
            Set<String> providers = channelProviders.get(active);
            if (providers == null) {
                Set<String> newProviders = Collections.newSetFromMap(new ConcurrentHashMap<>());
                providers = channelProviders.putIfAbsent(active, newProviders);
                if (providers == null) {
                    providers = newProviders;
                }
            }
            providers.addAll(remove);
        }

        for (Set<Channel> channels : serviceChannels.values()) {
            if (channels.remove(inactive)) {
                channels.add(active);
            }
        }

    }

    public synchronized void removeInactiveConnection(Channel inactive) {
        for (RegisterMeta.Address address : addressChannel.keySet()) {
            addressChannel.remove(address, inactive);
        }

        channelProviders.remove(inactive);

        for (Set<Channel> channels : serviceChannels.values()) {
            channels.remove(inactive);
        }

    }

}
