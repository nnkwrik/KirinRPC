package io.github.nnkwrik.kirinrpc.springboot;

import io.github.nnkwrik.kirinrpc.common.util.NetUtils;
import io.github.nnkwrik.kirinrpc.netty.KirinServerAcceptor;
import io.github.nnkwrik.kirinrpc.registry.local.LocalRegistry;
import io.github.nnkwrik.kirinrpc.registry.remote.RegisterMeta;
import io.github.nnkwrik.kirinrpc.registry.remote.RegistryClient;
import io.github.nnkwrik.kirinrpc.registry.remote.ZookeeperRegistryClient;
import io.github.nnkwrik.kirinrpc.rpc.ProviderProcessor;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProviderService;
import io.github.nnkwrik.kirinrpc.springboot.config.ProviderConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nnkwrik
 * @date 19/04/28 9:06
 */
@Component
@Slf4j
public class KirinProviderBean implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private ProviderConfiguration providerConfig;
    private LocalRegistry localRegistry;
    private RegistryClient remoteRegistry;
    private KirinServerAcceptor nettyServerAcceptor;
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
        this.localRegistry = new LocalRegistry();
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(KirinProviderService.class);
        List<ServiceMeta> serviceMetas = null;
        if (serviceBeanMap != null) {
            //注册到本地
            serviceMetas = localRegistry.register(serviceBeanMap.values());
        }
        //创建远程注册中心连接
        remoteRegistry = new ZookeeperRegistryClient(providerConfig.getRegistryAddress());

        List<RegisterMeta> registerMetas = new ArrayList<>();
        serviceMetas.forEach(serviceMeta -> {
            RegisterMeta.Address address
                    = new RegisterMeta.Address(serverAddress.getAddress().getHostAddress(), serverAddress.getPort());
            registerMetas.add(new RegisterMeta(address, serviceMeta));
        });
        //注册到远程
        remoteRegistry.register(registerMetas);
    }

    private void initServer() {
        nettyServerAcceptor = new KirinServerAcceptor(new ProviderProcessor() {
            @Override
            public Object lookupService(ServiceMeta serviceMeta) {
                return localRegistry.lookupService(serviceMeta);
            }
        }, providerConfig.getServerPort());

        nettyServerAcceptor.init();
    }

    /**
     * 查询当前主机的ip地址
     *
     * @param providerConfig
     * @return
     */
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
