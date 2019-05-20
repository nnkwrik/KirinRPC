package io.github.nnkwrik.kirinrpc.netty.srv;

import io.github.nnkwrik.kirinrpc.netty.handler.AcceptorHandler;
import io.github.nnkwrik.kirinrpc.netty.handler.AcceptorIdleStateTrigger;
import io.github.nnkwrik.kirinrpc.netty.handler.ProtocolDecoder;
import io.github.nnkwrik.kirinrpc.netty.handler.ProtocolEncoder;
import io.github.nnkwrik.kirinrpc.rpc.ProviderProcessor;
import io.github.nnkwrik.kirinrpc.rpc.ServiceBeanContainer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/04/29 12:33
 */
@Slf4j
public class KirinServerAcceptor extends NettyAcceptor {

    private InetSocketAddress serverAddress;
    //处理心跳超时
    private AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
    //编码器
    private final ProtocolEncoder encoder = new ProtocolEncoder();
    //进行rpc调用的handler
    private final AcceptorHandler handler;

    public KirinServerAcceptor(ServiceBeanContainer serviceContainer, int port) {
        super();
        ProviderProcessor providerProcessor = new ProviderProcessor(serviceContainer);
        this.serverAddress = new InetSocketAddress(port);
        this.handler = new AcceptorHandler(providerProcessor);
    }


    public void start() throws InterruptedException {
        this.start(true);
    }

    public void start(boolean sync) throws InterruptedException {
        bootstrap.channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                //每隔60s的时间内如果没有接受到任何的read事件的话，则会触发userEventTriggered事件，并指定IdleState的类型为READER_IDLE
//                                new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS),
                                //因为我们在client端设置了每隔30s会发送一个心跳包过来，如果60s都没有收到心跳，则说明链路发生了问题
//                                idleStateTrigger,
                                new ProtocolDecoder(),
                                encoder,
                                handler
                        );
                    }
                });

        ChannelFuture future = bootstrap.bind(serverAddress).sync();

        log.info("netty acceptor server start.");

        if (sync) {
            future.channel().closeFuture().sync();//把主线程wait
            //服务器同步连接断开时,这句代码才会往下执行
        }

    }
}
