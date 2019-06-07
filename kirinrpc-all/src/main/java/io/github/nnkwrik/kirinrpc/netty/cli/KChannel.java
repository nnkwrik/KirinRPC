package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.common.Constants;
import io.github.nnkwrik.kirinrpc.netty.handler.cli.ConnectionWatchdog;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nnkwrik
 * @date 19/06/01 12:47
 */
public class KChannel {//装了一些权限以及预热
    private Channel channel;

    private Map<ServiceMeta, Integer> serviceWight = new ConcurrentHashMap<>();

    private int warmUpTime = Constants.DEFAULT_WARM_UP_TIME;

    private long setUpTime;

    private KChannel() {
    }

    public void setConnection(Channel channel) {
        this.channel = channel;
    }

    public void resetSetUpTime() {
        this.setUpTime = System.currentTimeMillis();
    }

    public long getSetUpTime() {
        return setUpTime;
    }

    public void addService(ServiceMeta service, int wight) {
        serviceWight.put(service, wight);
    }

    public int getWeight(ServiceMeta service) {
        Integer weight = serviceWight.get(service);
        if (weight == null || weight < 0) {
            weight = Constants.DEFAULT_WIGHT;
        }

        int warmUp = getWarmUpTime();
        int setUp = (int) (System.currentTimeMillis() - getSetUpTime());

        if (setUp < warmUp) { //还在预热阶段
            weight = weight * (setUp / warmUp);
        }

        return weight;
    }

    public int getWarmUpTime() {
        return warmUpTime >= 0 ? warmUpTime : 0;
    }

    public void setWarmUpTime(int warmUpTime) {
        this.warmUpTime = warmUpTime;
    }

    public boolean isChannel(Channel channel) {
        return this.channel == channel;
    }

    public boolean replaceChannel(Channel oldChannel, Channel newChannel) {
        if (oldChannel == channel) {
            channel = newChannel;
            return true;
        }
        return false;
    }

    public void close() {
        channel.close();
        ConnectionWatchdog.setReconnect(channel, false);
    }

    public static KChannel connect(NettyConnector connector, RegisterMeta registerMeta) {
        RegisterMeta.Address address = registerMeta.getAddress();
        Channel connection = connector.connect(address.getHost(), address.getPort());

        KChannel kChannel = new KChannel();
        kChannel.addService(registerMeta.getServiceMeta(), registerMeta.getWight());
        kChannel.setConnection(connection);
        kChannel.resetSetUpTime();
        return kChannel;
    }

//    public <T> RPCFuture<T> write(RequestPayload payload) {
//
//        CountDownLatch latch = new CountDownLatch(1);
//
//        channel.writeAndFlush(payload).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                latch.countDown();
//            }
//        });
//
//        try {
//            latch.await();
//            return new RPCFuture(payload.id());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public <T> RPCFuture<T> write(RequestPayload payload) {
        RPCFuture rpcFuture = new RPCFuture(payload.id());
        channel.writeAndFlush(payload).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    rpcFuture.sent(true);
                }
            }
        });

        return rpcFuture;
    }

}
