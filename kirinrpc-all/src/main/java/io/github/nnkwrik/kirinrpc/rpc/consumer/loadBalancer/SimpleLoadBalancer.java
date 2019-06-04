package io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer;

import io.github.nnkwrik.kirinrpc.netty.cli.KChannel;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.util.Random;

/**
 * 一个简单的随机数负载均衡
 *
 * @author nnkwrik
 * @date 19/05/31 15:59
 */
public class SimpleLoadBalancer extends AbstractLoadBalancer {

    private Random rand = new Random();

    @Override
    protected KChannel doSelect(KChannel[] connectionArray, ServiceMeta service) {
        return connectionArray[rand.nextInt(connectionArray.length)];
    }
}
