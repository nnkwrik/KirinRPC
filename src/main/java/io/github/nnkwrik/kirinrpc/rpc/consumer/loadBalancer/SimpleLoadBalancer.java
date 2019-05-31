package io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer;

import io.github.nnkwrik.kirinrpc.ConnectFailedException;
import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.channel.Channel;

import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个简单的随机数负载均衡
 *
 * @author nnkwrik
 * @date 19/05/31 15:59
 */
public class SimpleLoadBalancer implements LoadBalancer {
    private ConnectorManager connectorManager = ConnectorManager.getInstance();
    private AtomicInteger aInt = new AtomicInteger(0);

    @Override
    public AbstractMap.SimpleEntry<String, Channel> select(ServiceMeta service) {
        Set<Channel> connections = connectorManager.getConnections(service);
        if (connections == null) {
            throw new ConnectFailedException("No provider can provide this service " + service);
        }

        Channel[] connectionArray = connections.stream().toArray(Channel[]::new);
        Channel connection = connectionArray[aInt.incrementAndGet() % connectionArray.length];

        Set<String> providers = connectorManager.getProviders(connection);
        if (providers == null) {
            throw new ConnectFailedException("No provider can provide this service " + service);
        }

        String[] providerArray = providers.stream().toArray(String[]::new);
        String provider = providerArray[aInt.incrementAndGet() % providerArray.length];

        return new AbstractMap.SimpleEntry<>(provider, connection);
    }
}
