package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author nnkwrik
 * @date 19/05/31 15:53
 */
public abstract class AbstractClusterInvoker implements ClusterInvoker {
    //id
    private final AtomicLong aLong = new AtomicLong(0);//TODO atomicLong?

    protected final LoadBalancer loadBalancer;

    public AbstractClusterInvoker(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    protected long newId() {
        return aLong.addAndGet(1);
    }
}
