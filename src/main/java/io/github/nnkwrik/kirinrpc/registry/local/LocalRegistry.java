package io.github.nnkwrik.kirinrpc.registry.local;

import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProviderService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author nnkwrik
 * @date 19/05/18 18:18
 */
@Slf4j
public class LocalRegistry {

    private final ConcurrentMap<String, Object> serviceProviders = new ConcurrentHashMap<>();

    public List<ServiceMeta> register(Collection<Object> serviceBeans) {
        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        for (Object serviceBean : serviceBeans) {
            List<String> interfaceName = Arrays.stream(serviceBean.getClass().getInterfaces())
                    .map(Class::getName).collect(Collectors.toList());

            String serviceGroup = serviceBean.getClass().getAnnotation(KirinProviderService.class).group();
            interfaceName.stream().forEach(serviceName -> {
                log.info("Loading service: {} ,group : {}", serviceName, serviceGroup);
                ServiceMeta serviceMeta = new ServiceMeta(serviceName, serviceGroup);
                String serviceKey = serviceGroup+"/"+serviceName;
                serviceProviders.put(serviceKey, serviceBean);
                serviceMetaList.add(serviceMeta);
            });

        }
        return serviceMetaList;
    }

    public Object lookupService(ServiceMeta serviceMeta){
        return serviceProviders.get(serviceMeta.getServiceGroup() + "/" + serviceMeta.getServiceName());
    }

}
