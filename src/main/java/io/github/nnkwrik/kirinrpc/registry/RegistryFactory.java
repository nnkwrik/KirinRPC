package io.github.nnkwrik.kirinrpc.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author nnkwrik
 * @date 19/05/22 15:27
 */
public class RegistryFactory {
    private static ConcurrentMap<String, RegistryClient> registryMap = new ConcurrentHashMap<>();

    public static RegistryClient getConnectedInstance(String registryAddr) {
        RegistryClient registryClient = registryMap.putIfAbsent(registryAddr,new ZookeeperRegistryClient(registryAddr));
        if (registryClient == null){
            //添加进去了新的
            registryClient = registryMap.get(registryAddr);
            registryClient.connect(registryAddr);
        }
        return registryClient;
    }
}
