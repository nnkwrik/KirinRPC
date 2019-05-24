package io.github.nnkwrik.kirinrpc.springboot.config.provider;

import io.github.nnkwrik.kirinrpc.common.util.NetUtils;
import io.github.nnkwrik.kirinrpc.netty.srv.KirinServerAcceptor;
import io.github.nnkwrik.kirinrpc.registry.RegistryClient;
import io.github.nnkwrik.kirinrpc.registry.RegistryFactory;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.rpc.provider.ServiceBeanContainer;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nnkwrik
 * @date 19/04/28 9:06
 */
@Component
@Slf4j
public class KirinProviderBean implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private ProviderConfig providerConfig;
    private ServiceBeanContainer serviceContainer;
    private RegistryClient registryClient;
    private KirinServerAcceptor nettyServerAcceptor;
    private InetSocketAddress serverAddress;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.providerConfig = applicationContext.getBean(ProviderConfig.class);
        if (providerConfig == null) {
            return;
        }
        this.serverAddress = findServerAddress(providerConfig);
        this.serviceContainer = new ServiceBeanContainer();
        initRegistry();
        initServer();
    }

    private void initRegistry() {
        this.serviceContainer = new ServiceBeanContainer();
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(KirinProvideService.class);
        if (serviceBeanMap == null || serviceBeanMap.isEmpty()) return;
        //放入提供者容器
        List<ServiceMeta> serviceMetas = serviceContainer.addServiceBean(providerConfig.getName(), serviceBeanMap.values());

        //创建远程注册中心连接
        registryClient = RegistryFactory.getConnectedInstance(providerConfig.getRegistryAddress());

        List<RegisterMeta> registerMetas = serviceMetas.stream()
                .map(s -> {
                    RegisterMeta.Address address =
                            new RegisterMeta.Address(serverAddress.getAddress().getHostAddress(), serverAddress.getPort());
                    return new RegisterMeta(address, s);
                })
                .collect(Collectors.toList());


        //注册到远程
        registryClient.register(registerMetas);
    }

    private void initServer() throws InterruptedException {
        nettyServerAcceptor = new KirinServerAcceptor(serviceContainer, providerConfig.getProviderPort());
        nettyServerAcceptor.start();
    }

    /**
     * 查询当前主机的ip地址
     *
     * @param providerConfig
     * @return
     */
    public InetSocketAddress findServerAddress(ProviderConfig providerConfig) {
        String address = providerConfig.getProviderAddress();
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

        return new InetSocketAddress(address, providerConfig.getProviderPort());

    }

}
