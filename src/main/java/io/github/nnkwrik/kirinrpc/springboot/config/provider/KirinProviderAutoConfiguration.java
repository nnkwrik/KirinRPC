package io.github.nnkwrik.kirinrpc.springboot.config.provider;

import io.github.nnkwrik.kirinrpc.common.util.Requires;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvider;
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
@ComponentScan(basePackages = {"io.github.nnkwrik.kirinrpc.springboot.config.provider"})
@EnableConfigurationProperties(ProviderPropertiesConfig.class)
public class KirinProviderAutoConfiguration {


    private ProviderPropertiesConfig providerPropertiesConfig;//配置文件配置的方式

    private KirinProvider providerAnnotationConfig;//注解配置的方式,不存在注解配置时空对象

    @Autowired
    public KirinProviderAutoConfiguration(ProviderPropertiesConfig providerPropertiesConfig, ApplicationContext context) {
        this.providerPropertiesConfig = providerPropertiesConfig;

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
    public ProviderConfig getProviderConfig() {

        if (!providerPropertiesConfig.isEnable()) {
            return null;
        }

        ProviderConfig config = new ProviderConfig();
        BeanUtils.copyProperties(providerPropertiesConfig, config);

        String name = System.getProperty("kirin.provider.name");
        if (!StringUtils.isEmpty(name)){
            config.setName(name);
        }else {
            name = config.getName();
            if (StringUtils.isEmpty(name)) {
                name = providerAnnotationConfig.name();
                if (StringUtils.isEmpty(name)) {
                    //生成随机的name
                    name = "provider-" + UUID.randomUUID();
                }
                config.setName(name);
            }
            System.setProperty("kirin.provider.name",name);
        }

        String registryAddress = config.getRegistryAddress();
        if (StringUtils.isEmpty(registryAddress)) {
            registryAddress = providerAnnotationConfig.registryAddress();
            Requires.requireNotNull(registryAddress, "registryAddress is null,should set registryAddress in @KirinProvider.");
            config.setRegistryAddress(registryAddress);
        }

        String serverAddress = config.getProviderAddress();
        if (StringUtils.isEmpty(serverAddress)) {
            serverAddress = providerAnnotationConfig.providerAddress();
            config.setProviderAddress(serverAddress);
        }

        Integer serverPort = config.getProviderPort();
        if (serverPort == null) {
            serverPort = providerAnnotationConfig.providerPort();
            config.setProviderPort(serverPort);
        }

        log.info("Success to load provider configuration : " + config);
        return config;
    }

}
