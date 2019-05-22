package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import io.github.nnkwrik.kirinrpc.common.util.Requires;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

/**
 * @author nnkwrik
 * @date 19/04/25 14:58
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {"io.github.nnkwrik.kirinrpc.springboot.config.consumer"})
@EnableConfigurationProperties(ConsumerPropertiesConfig.class)
public class KirinConsumerAutoConfiguration {


    private ConsumerPropertiesConfig consumerPropertiesConfig;//配置文件配置的方式

    private KirinConsumer consumerAnnotationConfig;//注解配置的方式,不存在注解配置时空对象

    @Autowired
    public KirinConsumerAutoConfiguration(ConsumerPropertiesConfig consumerPropertiesConfig, ApplicationContext context) {
        this.consumerPropertiesConfig = consumerPropertiesConfig;

        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
        Class<?> springBootstrap = annotatedBeans.values().toArray()[0].getClass().getSuperclass();
        this.consumerAnnotationConfig = springBootstrap.getAnnotation(KirinConsumer.class);
    }

    /**
     * 对2种配置方式合并得到Consumer的配置
     *
     * @return
     */
    @Bean
    public ConsumerConfig getConsumerConfig() {

        if (!consumerPropertiesConfig.isEnable()) {
            return null;
        }

        ConsumerConfig config = new ConsumerConfig();
        BeanUtils.copyProperties(consumerPropertiesConfig, config);

        String name = config.getName();
        if (StringUtils.isEmpty(name)) {
            name = consumerAnnotationConfig.name();
            if (StringUtils.isEmpty(name)) {
                //生成随机的name
                name = "consumer-" + UUID.randomUUID();
            }
            config.setName(name);
        }

        String registryAddress = config.getRegistryAddress();
        if (StringUtils.isEmpty(registryAddress)) {
            registryAddress = consumerAnnotationConfig.registryAddress();
            Requires.requireNotNull(registryAddress, "registryAddress is null,should set registryAddress in @KirinConsumer.");
            config.setRegistryAddress(registryAddress);
        }

        log.info("Success to load consumer configuration : " + config);
        return config;
    }

}
