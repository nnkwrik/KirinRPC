package io.github.nnkwrik.kirinrpc.springboot.config;

import io.github.nnkwrik.kirinrpc.common.util.Requires;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvider;
import lombok.extern.slf4j.Slf4j;
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
@ComponentScan(basePackages = {"io.github.nnkwrik.kirinrpc"})
@EnableConfigurationProperties(KirinConfigurationProperties.class)
public class KirinAutoConfiguration {


    private KirinConfigurationProperties configurationProperties;//配置文件配置的方式

    private KirinProvider providerAnnotationConfig;//注解配置的方式

    @Autowired
    public KirinAutoConfiguration(KirinConfigurationProperties configurationProperties, ApplicationContext context) {
        this.configurationProperties = configurationProperties;

        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
        Class<?> springBootstrap = annotatedBeans.values().toArray()[0].getClass().getSuperclass();
        this.providerAnnotationConfig = springBootstrap.getAnnotation(KirinProvider.class);

    }

    /**
     * 对2种配置方式合并得到Provider的配置
     *
     * @return
     */
    @Bean
    public ProviderConfiguration getProviderConfig() {
        ProviderConfiguration config = configurationProperties.getProvider();

        if (!config.isEnable()) {
            return null;
        }


        String name = config.getName();
        if (StringUtils.isEmpty(name)) {
            name = providerAnnotationConfig.name();
            if (StringUtils.isEmpty(name)) {
                //生成随机的name
                name = "provider-" + UUID.randomUUID();
            }
            config.setName(name);
        }

        String registryAddress = config.getRegistryAddress();
        if (StringUtils.isEmpty(registryAddress)) {
            registryAddress = providerAnnotationConfig.registryAddress();
            Requires.requireNotNull(registryAddress, "registryAddress is null,should set registryAddress in @KirinProvider.");
            config.setRegistryAddress(registryAddress);
        }

        String serverAddress = config.getServerAddress();
        if (StringUtils.isEmpty(serverAddress)){
            serverAddress = providerAnnotationConfig.providerAddress();
            config.setServerAddress(serverAddress);
        }

        Integer serverPort = config.getServerPort();
        if (serverPort == null) {
            serverPort = providerAnnotationConfig.providerPort();
            config.setServerPort(serverPort);
        }

        log.info("Success to get provider configuration : " + config);
        return config;
    }


}
