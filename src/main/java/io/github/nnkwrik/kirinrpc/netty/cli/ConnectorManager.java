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
import io.netty.util.AttributeKey;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
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
    //共用一个channel的provider集合，address相同的provider会共用一个channel.value为provider的appName
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


    public void addConnectionWithProvider(String providerName, RegisterMeta.Address address, ServiceMeta service) {

        Channel channel;

        if (addressChannel.get(address) != null) {
            //一个简单的CAS操作，防止对于一个add,remove,replace3个操作同时执行
            channel = ChannelLock.lock(addressChannel, address);
        } else {
            Channel newChannel = connector.connect(address.getHost(), address.getPort());
            channel = addressChannel.putIfAbsent(address, newChannel);
            if (channel == null) {
                channel = newChannel;
            } else {
                newChannel.close();
            }
            ChannelLock.lock(channel);
        }


        Set<String> providers = channelProviders.get(channel);
        if (providers == null) {
            Set<String> newProviders = Collections.newSetFromMap(new ConcurrentHashMap<>());
            providers = channelProviders.putIfAbsent(channel, newProviders);
            if (providers == null) {
                providers = newProviders;
            }
        }
        providers.add(providerName);

        Set<Channel> channels = serviceChannels.get(service);
        if (channels == null) {
            Set<Channel> newChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
            channels = serviceChannels.putIfAbsent(service, newChannels);
            if (channels == null) {
                channels = newChannels;
            }
        }
        channels.add(channel);

        ChannelLock.unlock(channel);
    }


    public Channel removeConnectionWithProvider(String providerName, RegisterMeta.Address address, ServiceMeta service) {
        Channel channel;
        Channel closedChannel = null;

        if (addressChannel.get(address) != null) {
            channel = ChannelLock.lock(addressChannel, address);
            Set<String> providers = channelProviders.get(channel);
            if (providers != null) {
                providers.remove(providerName);
                if (providers.isEmpty()) {
                    //说明该channel上没有任何provider
                    addressChannel.remove(address);
                    channelProviders.remove(channel);

                    Set<Channel> channels = serviceChannels.get(service);
                    if (channels != null) {
                        channels.remove(channel);
                        if (channels.isEmpty()) {
                            serviceChannels.remove(service);
                        }
                    }

                    //关闭这个不提供服务的channel
                    channel.close();
                    ConnectionWatchdog.setReconnect(channel, false);
                    closedChannel = channel;
                }
            }

            ChannelLock.unlock(channel);
        }

        return closedChannel;
    }

    public void replaceInactiveConnection(Channel inactive, Channel active) {
        ChannelLock.lock(inactive);
        ChannelLock.lock(active);

        //对addressChannel，serviceChannels，channelProviders中的channel对象进行替换
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
            if (channels.contains(inactive)) {
                synchronized (inactive) {
                    channels.remove(inactive);
                    channels.add(active);
                }
            }
        }
        for (RegisterMeta.Address address : addressChannel.keySet()) {
            if (addressChannel.replace(address, inactive, active)) {
                break;
            }
        }

        ChannelLock.unlock(inactive);
        ChannelLock.unlock(active);
    }

    public boolean isAvailable(ServiceMeta service) {
        Set<Channel> channels = serviceChannels.get(service);
        if (channels != null && !channels.isEmpty()) {
            return true;
        }
        return false;
    }

    private Set<Channel> getConnections(ServiceMeta service) {
        Set<Channel> channels = serviceChannels.get(service);
        if (channels != null && !channels.isEmpty()) {
            return channels;
        }
        return null;
    }

    public AbstractMap.SimpleEntry<String, Channel> chooseConnection(ServiceMeta serviceMeta) {
        if (!isAvailable(serviceMeta)) {
            throw new ConnectFailedException("No provider can provide this service " + serviceMeta);
        }
        Channel channel = (Channel) getConnections(serviceMeta).toArray()[0];
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


    static class ChannelLock {

        private static final AttributeKey<Object> lock = AttributeKey.newInstance("lock");

        public static Channel lock(Map<RegisterMeta.Address, Channel> addressChannel,
                                   RegisterMeta.Address address) {
            Channel channel;
            Object newLock = new Object();
            while ((channel = addressChannel.get(address)) != null
                    && !channel.attr(lock).compareAndSet(null, newLock)) {//占用失败之间
                Object currentLock = channel.attr(lock).get();
                synchronized (currentLock) {
                    try {
                        currentLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return channel;
        }

        public static Channel lock(Channel channel) {
            Object newLock = new Object();
            while (!channel.attr(lock).compareAndSet(null, newLock)) {
                Object currentLock = channel.attr(lock).get();
                synchronized (currentLock) {
                    try {
                        currentLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return channel;
        }

        public static void unlock(Channel channel) {
            Object currentLock = channel.attr(lock).getAndSet(null);
            synchronized (currentLock) {
                currentLock.notifyAll();
            }
        }
    }


}
