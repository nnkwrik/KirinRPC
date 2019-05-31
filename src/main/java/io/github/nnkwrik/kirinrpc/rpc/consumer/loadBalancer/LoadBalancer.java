package io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer;

import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.channel.Channel;

import java.util.AbstractMap;

/**
 * @author nnkwrik
 * @date 19/05/31 14:53
 */
public interface LoadBalancer {

    AbstractMap.SimpleEntry<String, Channel> select(ServiceMeta service);

}
