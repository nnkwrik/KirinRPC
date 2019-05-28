package io.github.nnkwrik.kirinrpc.netty.cli;

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
        log.info("netty client cli completed initialization.");
    }

    public abstract Channel connect(String host, int port);

}
