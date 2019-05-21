package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.netty.handler.AcceptorIdealStateTrigger;
import io.github.nnkwrik.kirinrpc.netty.handler.ConnectorHandler;
import io.github.nnkwrik.kirinrpc.netty.handler.ProtocolDecoder;
import io.github.nnkwrik.kirinrpc.netty.handler.ProtocolEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nnkwrik
 * @date 19/05/20 16:11
 */
@Slf4j
public class KirinClientConnector extends NettyConnector {
    //处理心跳超时
    private AcceptorIdealStateTrigger idleStateTrigger = new AcceptorIdealStateTrigger();
    //编码器
    private final ProtocolEncoder encoder = new ProtocolEncoder();

    //进行rpc调用的handler
    private final ConnectorHandler handler = new ConnectorHandler();


    @Override
    public Channel connect(String host,int port) throws InterruptedException {
        bootstrap.channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                //每隔30s的时间触发一次userEventTriggered的方法，并且指定IdleState的状态位是WRITER_IDLE
//                                new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                                //实现userEventTriggered方法，并在state是WRITER_IDLE的时候发送一个心跳包到sever端，告诉server端我还活着
//                                idleStateTrigger,
                                new ProtocolDecoder(),
                                encoder,
                                handler
                        );
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();

        return future.channel();
    }
}
