package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.ConnectFailedException;
import io.github.nnkwrik.kirinrpc.netty.handler.ProtocolDecoder;
import io.github.nnkwrik.kirinrpc.netty.handler.ProtocolEncoder;
import io.github.nnkwrik.kirinrpc.netty.handler.cli.ConnectionWatchdog;
import io.github.nnkwrik.kirinrpc.netty.handler.cli.ConnectorHandler;
import io.github.nnkwrik.kirinrpc.netty.handler.cli.ConnectorIdealStateTrigger;
import io.github.nnkwrik.kirinrpc.rpc.consumer.ResponseProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/05/20 16:11
 */
@Slf4j
public class KirinClientConnector extends NettyConnector {
    //定期发送心跳包
    private ConnectorIdealStateTrigger idealStateTrigger = new ConnectorIdealStateTrigger();
    //编码器
    private final ProtocolEncoder encoder = new ProtocolEncoder();

    //处理rpc调用结果的handler
    private final ConnectorHandler handler;
    //处理rpc调用结果的处理器
    private final ResponseProcessor processor;

    public KirinClientConnector(ResponseProcessor processor) {
        this.processor = processor;
        this.handler = new ConnectorHandler(processor);
    }

    @Override
    public Channel connect(String host, int port) {

        ChannelHandler[] handlers = {
                //每隔30s的时间触发一次userEventTriggered的方法，并且指定IdleState的状态位是WRITER_IDLE
                new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                //实现userEventTriggered方法，并在state是WRITER_IDLE的时候发送一个心跳包到sever端，告诉server端我还活着
                idealStateTrigger,
                new ProtocolDecoder(),
                encoder,
                handler
        };

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, null, port, host, handlers);
        watchdog.setReconnect(true);

        bootstrap.channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handlers);
                    }
                });


        Channel channel = null;
        try {
            ChannelFuture future;
            synchronized (bootstrap) {
                bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handlers);
                    }
                });

                future = bootstrap.connect(host, port);
            }
            future.sync();
            channel = future.channel();
        } catch (Throwable t) {
            throw new ConnectFailedException("connects to [" + host + ":" + port + "] fails", t);
        }

        return channel;
    }

    @Override
    public void shutdown() {
        worker.shutdownGracefully().syncUninterruptibly();
        processor.shutdown();
    }
}
