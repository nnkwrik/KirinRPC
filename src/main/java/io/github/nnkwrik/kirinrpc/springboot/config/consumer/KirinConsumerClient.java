package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import io.github.nnkwrik.kirinrpc.registry.RegistryClient;
import io.github.nnkwrik.kirinrpc.registry.RegistryFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author nnkwrik
 * @date 19/05/24 15:43
 */
@Component
public class KirinConsumerClient implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private ConsumerConfig consumerConfig;
    private RegistryClient registryClient;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.consumerConfig = applicationContext.getBean(ConsumerConfig.class);
        if (consumerConfig == null) {
            return;
        }
        initRegistry();
    }

    private void initRegistry() {
        this.registryClient = RegistryFactory.getConnectedInstance(consumerConfig.getRegistryAddress());

    }

}
