package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author nnkwrik
 * @date 19/05/20 16:00
 */
@Slf4j
public abstract class NettyConnector {

    protected Bootstrap bootstrap;
    private int nWorkers;
    private EventLoopGroup worker;
    protected volatile ByteBufAllocator allocator;

    //address对应的channel连接
    private final ConcurrentMap<RegisterMeta.Address, Channel> addressChannelMap = new ConcurrentHashMap<>();
    //服务和提供该服务提供者channel.
    private final ConcurrentMap<ServiceMeta, Set<Channel>> serviceChannels = new ConcurrentHashMap<>();


    public NettyConnector() {
        this(Runtime.getRuntime().availableProcessors() << 1);
    }

    public NettyConnector(int nWorkers) {
        this.nWorkers = nWorkers;
        init();
    }

    protected void init() {
        worker = new NioEventLoopGroup(nWorkers);
        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
        bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .option(ChannelOption.ALLOCATOR, allocator)
                .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOW_HALF_CLOSURE, false);
        log.info("netty client connector completed initialization.");
    }


    public Channel createConnectionWithAddress(RegisterMeta.Address address) {
        Channel channel = addressChannelMap.get(address);
        if (addressChannelMap.get(address) == null) {
            Channel newChannel = connect(address.getHost(), address.getPort());
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

    public abstract Channel connect(String host, int port);

}
