package io.github.nnkwrik.kirinrpc.springboot;

import io.github.nnkwrik.kirinrpc.common.util.NetUtils;
import io.github.nnkwrik.kirinrpc.netty.KirinNettyServer;
import io.github.nnkwrik.kirinrpc.registry.RegisterMeta;
import io.github.nnkwrik.kirinrpc.registry.RegistryClient;
import io.github.nnkwrik.kirinrpc.registry.ZookeeperRegistryClient;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProviderService;
import io.github.nnkwrik.kirinrpc.springboot.config.ProviderConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nnkwrik
 * @date 19/04/28 9:06
 */
@Component
@Slf4j
public class KirinProviderBean implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private ProviderConfiguration providerConfig;
    private Map<RegisterMeta.ServiceMeta, Object> serviceMap = new HashMap<>();
    private RegistryClient registryClient;
    private KirinNettyServer nettyServer;
    private InetSocketAddress serverAddress;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.providerConfig = applicationContext.getBean(ProviderConfiguration.class);
        if (providerConfig == null) {
            return;
        }
        this.serverAddress = findServerAddress(providerConfig);
        initRegistry();
        initServer();
    }

    private void initRegistry() {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(KirinProviderService.class);
        if (serviceBeanMap != null) {
            for (Object serviceBean : serviceBeanMap.values()) {
                List<String> interfaceName = Arrays.stream(serviceBean.getClass().getInterfaces())
                        .map(Class::getName).collect(Collectors.toList());

                String serviceGroup = serviceBean.getClass().getAnnotation(KirinProviderService.class).group();
                interfaceName.stream().forEach(serviceName -> {
                    log.info("Loading service: {} ,group : {}", serviceName, serviceGroup);
                    serviceMap.put(new RegisterMeta.ServiceMeta(serviceName, serviceGroup), serviceBean);
                });

            }
        }
        registryClient = new ZookeeperRegistryClient(providerConfig.getRegistryAddress());

        List<RegisterMeta> registerMetas = new ArrayList<>();
        serviceMap.keySet().forEach(serviceMeta -> {
            RegisterMeta.Address address
                    = new RegisterMeta.Address(serverAddress.getAddress().getHostAddress(), serverAddress.getPort());
            registerMetas.add(new RegisterMeta(address, serviceMeta));
        });
        registryClient.register(registerMetas);
    }

    private void initServer() {
        nettyServer = new KirinNettyServer(providerConfig.getServerPort());
        nettyServer.init();
    }

    public InetSocketAddress findServerAddress(ProviderConfiguration providerConfig) {
        String address = providerConfig.getServerAddress();
        if (NetUtils.isInvalidLocalHost(address)) {
            try {
                address = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.warn(e.getMessage(), e);
            }
            if (NetUtils.isInvalidLocalHost(address)) {
                try (Socket socket = new Socket()) {

                    String[] registryAddress = providerConfig.getRegistryAddress().split(":");
                    String host = registryAddress[0];
                    int port = Integer.parseInt(registryAddress[1]);
                    SocketAddress addr = new InetSocketAddress(host, port);
                    socket.connect(addr, 1000);
                    address = socket.getLocalAddress().getHostAddress();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }

                if (NetUtils.isInvalidLocalHost(address)) {
                    address = "127.0.0.1";
                }

            }
        }

        return new InetSocketAddress(address, providerConfig.getServerPort());

    }

}
