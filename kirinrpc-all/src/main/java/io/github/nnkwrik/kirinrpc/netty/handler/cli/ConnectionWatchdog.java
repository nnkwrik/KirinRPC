package io.github.nnkwrik.kirinrpc.netty.handler.cli;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author nnkwrik
 * @date 19/05/27 16:14
 */
@Slf4j
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask {

    private static Map<Channel, ConnectionWatchdog> watchdogMap = new HashMap<>();

    private final Bootstrap bootstrap;
    private final Timer timer;
    private final int port;
    private final String host;

    private volatile boolean reconnect = true;
    private int attempts;

    private Channel connection;

    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int port, String host) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.port = port;
        this.host = host;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public static void setReconnect(Channel connection, boolean reconnect) {
        ConnectionWatchdog watchdog = watchdogMap.get(connection);
        if (watchdog != null) {
            watchdog.setReconnect(reconnect);
        }
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connection = ctx.channel();
        watchdogMap.put(connection, this);
        attempts = 0;

        log.info("Connects with {}.", connection);

        ctx.fireChannelActive();
    }

    /**
     * 因为链路断掉之后，会触发channelInActive方法，进行重连
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        boolean doReconnect = reconnect;
        if (doReconnect) {
            if (attempts < 12) {
                attempts++;
            }
            long timeout = 2 << attempts;
            timer.newTimeout(this, timeout, MILLISECONDS);
        } else {
            log.warn("Disconnects with {}, port: {},host {}, reconnect: {}.", ctx.channel(), port, host, doReconnect);
        }


        ctx.fireChannelInactive();
    }

    @Override
    public void run(Timeout timeout) throws Exception {

        ChannelFuture future;
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handlers());
                }
            });
            future = bootstrap.connect(host, port);
        }

        future.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();

                log.debug("Reconnects with {}, {}.", host + ":" + port, succeed ? "succeed" : "failed");

                if (!succeed) {
                    //如果失败了再次进入调用channelInactive()进行重连
                    f.channel().pipeline().fireChannelInactive();
                } else {
                    Channel newConnection = future.channel();
                    //把无效channel替换为有效的。
                    ConnectionWatchdog watchdog = watchdogMap.remove(connection);
                    watchdogMap.put(newConnection, watchdog);
                    ConnectorManager.getInstance()
                            .replaceInactiveConnection(connection, newConnection);
                    connection = newConnection;

                }
            }
        });
    }


    public abstract ChannelHandler[] handlers();
}
