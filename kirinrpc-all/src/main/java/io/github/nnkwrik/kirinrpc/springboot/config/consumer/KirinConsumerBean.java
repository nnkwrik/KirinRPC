package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import io.github.nnkwrik.kirinrpc.registry.RegistryClient;
import io.github.nnkwrik.kirinrpc.registry.RegistryFactory;
import io.github.nnkwrik.kirinrpc.registry.listener.ServiceNotifyListener;
import io.github.nnkwrik.kirinrpc.rpc.consumer.ProxyFactory;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/05/22 15:00
 */
@Slf4j
public class KirinConsumerBean<T> implements FactoryBean<T> {

    private ConsumerConfig consumerConfig;

    private Class<T> consumerInterface;

    private KirinConsumeService consumeServiceAnnotation;

    private RegistryClient registryClient;

    public KirinConsumerBean(ConsumerConfig consumerConfig, Class<T> consumerInterface, KirinConsumeService consumeServiceAnnotation) {
        this.consumerConfig = consumerConfig;
        this.consumerInterface = consumerInterface;
        this.consumeServiceAnnotation = consumeServiceAnnotation;
        this.registryClient = RegistryFactory.getConnectedInstance(consumerConfig.getRegistryAddress());
    }

    @Override
    public T getObject() throws Exception {
        ServiceMeta serviceMeta = new ServiceMeta(consumerInterface.getName(), consumeServiceAnnotation.group());

        ServiceNotifyListener listener = new ServiceNotifyListener(serviceMeta);
        registryClient.subscribe(serviceMeta, listener);

        if (!listener.waitForAvailable(TimeUnit.SECONDS.toMillis(3))) {
            String msg = "Can't find provider for service " + consumerInterface.getName() + " when client boot";
            log.warn(msg);
        }
        //创建proxy对象返回。调用proxy时实际是用netty进行远程调用
        return ProxyFactory.factory(consumerInterface)
                .group(consumeServiceAnnotation.group())
                .invokerType(consumeServiceAnnotation.invokeType())
                .newProxy();
    }

    @Override
    public Class<?> getObjectType() {
        return consumerInterface;
    }

}
