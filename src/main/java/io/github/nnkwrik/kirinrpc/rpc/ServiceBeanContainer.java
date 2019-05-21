package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProviderService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author nnkwrik
 * @date 19/05/18 18:18
 */
@Slf4j
public class ServiceBeanContainer {

    private final ConcurrentMap<String, Object> serviceBeans = new ConcurrentHashMap<>();

    public List<ServiceMeta> addServiceBean(String appName, Collection<Object> serviceBeans) {
        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        for (Object serviceBean : serviceBeans) {
            List<String> interfaceName = Arrays.stream(serviceBean.getClass().getInterfaces())
                    .map(Class::getName).collect(Collectors.toList());

            String serviceGroup = serviceBean.getClass().getAnnotation(KirinProviderService.class).group();
            interfaceName.stream().forEach(serviceName -> {
                log.info("Loading service: {} ,group : {}", serviceName, serviceGroup);
                ServiceMeta serviceMeta = new ServiceMeta(appName, serviceName, serviceGroup);
                String serviceKey = serviceGroup + "/" + serviceName;
                this.serviceBeans.put(serviceKey, serviceBean);
                serviceMetaList.add(serviceMeta);
            });

        }
        return serviceMetaList;
    }

    public ServiceWrapper lookupService(ServiceMeta serviceMeta) {
        Object serviceBean = serviceBeans.get(serviceMeta.getServiceGroup() + "/" + serviceMeta.getServiceName());
        return new ServiceWrapper(serviceBean);
    }

}
