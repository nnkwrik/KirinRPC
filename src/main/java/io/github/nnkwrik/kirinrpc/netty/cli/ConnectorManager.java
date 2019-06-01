package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.consumer.ConsumerProcessor;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author nnkwrik
 * @date 19/05/27 20:56
 */
public class ConnectorManager {
    private static ConnectorManager connectorManager;
    private NettyConnector connector;

    //address对应的channel连接
    private final ConcurrentMap<RegisterMeta.Address, KChannel> addressChannel = new ConcurrentHashMap<>();
    //服务和提供该服务提供者channel.
    private final ConcurrentMap<ServiceMeta, Set<KChannel>> serviceChannels = new ConcurrentHashMap<>();

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


    public void addConnection(RegisterMeta registerMeta) {

        RegisterMeta.Address address = registerMeta.getAddress();
        KChannel channel = addressChannel.get(address);
        if (channel == null) {
            KChannel newChannel = KChannel.connect(connector, registerMeta);
            channel = addressChannel.putIfAbsent(address, newChannel);
            if (channel == null) {
                channel = newChannel;
            } else {
                newChannel.close();
            }
        }

        ServiceMeta service = registerMeta.getServiceMeta();
        Set<KChannel> channels = serviceChannels.get(service);
        if (channels == null) {
            Set<KChannel> newChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
            channels = serviceChannels.putIfAbsent(service, newChannels);
            if (channels == null) {
                channels = newChannels;
            }
        }
        channels.add(channel);
        channel.addService(service, registerMeta.getWight());
    }


    public KChannel removeConnection(RegisterMeta registerMeta) {
        KChannel channel;
        KChannel closedChannel = null;

        RegisterMeta.Address address = registerMeta.getAddress();
        if (addressChannel.get(address) != null) {
            channel = addressChannel.remove(address);

            ServiceMeta service = registerMeta.getServiceMeta();
            Set<KChannel> channels = serviceChannels.get(service);
            if (channels != null) {
                channels.remove(channel);
                if (channels.isEmpty()) {
                    serviceChannels.remove(service);
                }
            }

            //关闭这个不提供服务的channel
            channel.close();
            closedChannel = channel;
        }

        return closedChannel;
    }

    public void replaceInactiveConnection(Channel inactive, Channel active) {
        for (RegisterMeta.Address address : addressChannel.keySet()) {
            KChannel kChannel = addressChannel.get(address);
            if (kChannel.replaceChannel(inactive, active)) {
                kChannel.resetSetUpTime();
                break;
            }
        }
    }

    public void removeInactiveConnection(Channel inactive) {
        inactive.close();
        for (Set<KChannel> channels : serviceChannels.values()) {
            for (KChannel channel : channels) {
                if (channel.isChannel(inactive)) {
                    channels.remove(channel);
                }
            }
        }
        for (RegisterMeta.Address address : addressChannel.keySet()) {
            KChannel channel = addressChannel.get(address);
            if (channel.isChannel(inactive)) {
                addressChannel.remove(address);
                break;
            }
        }
    }

    public boolean isAvailable(ServiceMeta service) {
        Set<KChannel> channels = serviceChannels.get(service);
        if (channels != null && !channels.isEmpty()) {
            return true;
        }
        return false;
    }

    public Set<KChannel> getConnections(ServiceMeta service) {
        Set<KChannel> channels = serviceChannels.get(service);
        if (channels != null && !channels.isEmpty()) {
            return channels;
        }
        return null;
    }

}
