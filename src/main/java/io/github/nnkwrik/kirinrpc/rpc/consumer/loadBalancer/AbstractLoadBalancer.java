package io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer;

import io.github.nnkwrik.kirinrpc.ConnectFailedException;
import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.netty.cli.KChannel;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.util.Set;

/**
 * @author nnkwrik
 * @date 19/06/01 17:08
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    private ConnectorManager connectorManager = ConnectorManager.getInstance();

    @Override
    public KChannel select(ServiceMeta service) {

        Set<KChannel> connections = connectorManager.getConnections(service);
        if (connections == null) {
            throw new ConnectFailedException("No provider can provide this service " + service);
        }
        KChannel[] connectionArray = connections.stream().toArray(KChannel[]::new);
        return doSelect(connectionArray, service);
    }

    protected abstract KChannel doSelect(KChannel[] connectionArray, ServiceMeta service);
}
