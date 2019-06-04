package io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer;

import io.github.nnkwrik.kirinrpc.netty.cli.KChannel;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

/**
 * @author nnkwrik
 * @date 19/05/31 14:53
 */
public interface LoadBalancer {

    KChannel select(ServiceMeta service);

    enum loadBalancerType {
        RANDOM,
        SIMPLE
    }

}
