package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import io.github.nnkwrik.kirinrpc.netty.cli.KirinClientConnector;
import io.github.nnkwrik.kirinrpc.netty.cli.NettyConnector;
import io.github.nnkwrik.kirinrpc.registry.NotifyListener;
import io.github.nnkwrik.kirinrpc.registry.RegistryClient;
import io.github.nnkwrik.kirinrpc.registry.RegistryFactory;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

import java.util.Set;

/**
 * @author nnkwrik
 * @date 19/05/22 15:00
 */
@Slf4j
public class KirinConsumerBean<T> implements FactoryBean<T> {

    //    @Autowired
    private ConsumerConfig consumerConfig;

    private Class<T> consumerInterface;

    private KirinConsumeService consumeServiceAnnotation;

    private RegistryClient registryClient;

    private NettyConnector nettyConnector;

    public KirinConsumerBean(ConsumerConfig consumerConfig, Class<T> consumerInterface, KirinConsumeService consumeServiceAnnotation) {
        this.consumerConfig = consumerConfig;
        this.consumerInterface = consumerInterface;
        this.consumeServiceAnnotation = consumeServiceAnnotation;
        this.registryClient = RegistryFactory.getConnectedInstance(consumerConfig.getRegistryAddress());
        this.nettyConnector = new KirinClientConnector();
    }

    @Override
    public T getObject() throws Exception {
        ServiceMeta serviceMeta = new ServiceMeta(consumerInterface.getName(), consumeServiceAnnotation.group());
        registryClient.subscribe(serviceMeta, new NotifyListener() {
            //TODO 与所有服务提供者建立连接完毕之前进行阻塞
            @Override
            public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                if (event == NotifyEvent.CHILD_ADDED) {
                    log.info("service {} has a new provider.provider address is {}", registerMeta.getServiceMeta(), registerMeta.getAddress());

                    //拿到与该提供者的channel，如果没有与这个提供者的channel则创建
                    Channel connection = nettyConnector.getChannelWithAddress(registerMeta.getAddress());
                    Set<Channel> providerChannels = nettyConnector.getProviderChannel(registerMeta.getServiceMeta());
                    providerChannels.add(connection);

                } else if (event == NotifyEvent.CHILD_REMOVED) {
                    log.info("service {} reduced a provider.provider address was {}", registerMeta.getServiceMeta(), registerMeta.getAddress());

                    Channel connection = nettyConnector.getChannelWithAddress(registerMeta.getAddress());
                    Set<Channel> providerChannels = nettyConnector.getProviderChannel(registerMeta.getServiceMeta());
                    providerChannels.remove(connection);

                    if (providerChannels.size() <= 0) {
                        //TODO 没有服务提供者了
                    }

                }
            }

        });

        //创建proxy对象返回。调用proxy时实际是用这里的netty连接
        System.out.println(consumerInterface.getName() + " --- " + consumeServiceAnnotation);
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }


}
