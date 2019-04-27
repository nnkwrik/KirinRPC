package io.github.nnkwrik.kirinrpc.init.springboot.config;

import io.github.nnkwrik.kirinrpc.init.common.KirinConfigException;
import io.github.nnkwrik.kirinrpc.init.springboot.annotation.KirinProvider;
import io.github.nnkwrik.kirinrpc.init.common.ProtocolType;
import io.github.nnkwrik.kirinrpc.init.common.RegistryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * @author nnkwrik
 * @date 19/04/25 14:58
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {"io.github.nnkwrik.kirinrpc.init"})
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
     * 对三种配置方式合并得到Provider的配置
     *
     * @return
     */
    @Bean
    public ProviderConfiguration getProviderConfig() {
        ProviderConfiguration config = configurationProperties.getProvider();

        if (!config.isEnable()) {
            return null;
        }


        String name = config.getName().trim();
        if (StringUtils.isEmpty(name)) {
            name = providerAnnotationConfig.name().trim();
            if (StringUtils.isEmpty(name)) {
                name = configurationProperties.getName().trim();
                if (StringUtils.isEmpty(name)) {
                    //生成随机的name
                    name = "provider-" + UUID.randomUUID();
                }
            }

            config.setName(name);
        }

        String registryType = config.getRegistry().getType().toLowerCase();
        //从三种配置中优先加载与默认值不同的的
        if (registryType.equals(RegistryType.defaultRegistry)) {
            if (!providerAnnotationConfig.registryType().name().equals(RegistryType.defaultRegistry)) {
                registryType = providerAnnotationConfig.registryType().name();
            } else {
                registryType = configurationProperties.getRegistry().getType().toLowerCase();
            }
            config.getRegistry().setType(registryType);
        }

        String registryAddress = config.getRegistry().getAddress().trim();
        if (StringUtils.isEmpty(registryAddress)) {
            registryAddress = providerAnnotationConfig.registryAddress();
            if (StringUtils.isEmpty(registryAddress)) {
                registryAddress = configurationProperties.getRegistry().getAddress().trim();
                if (StringUtils.isEmpty(registryAddress)) {
                    throw new KirinConfigException("Should config registry address in application.properties file. ( or set registryAddress in @KirinProvider)");
                }
            }


            config.getRegistry().setAddress(registryAddress);
        }

        String protocolType = config.getProtocol().getType().toLowerCase();
        if (protocolType.equals(ProtocolType.defaultProtocol)) {
            if (!providerAnnotationConfig.protocolType().name().equals(ProtocolType.defaultProtocol)) {
                protocolType = providerAnnotationConfig.protocolType().name();
            } else {
                protocolType = configurationProperties.getProtocol().getType().toLowerCase();
            }
            config.getProtocol().setType(protocolType);
        }

        String protocolPort = config.getProtocol().getPort();
        if (protocolPort.equals(ProtocolType.defaultPort)) {
            if (!providerAnnotationConfig.protocolPort().equals(ProtocolType.defaultPort)) {
                protocolPort = providerAnnotationConfig.protocolPort();
            } else {
                protocolPort = configurationProperties.getProtocol().getPort();
            }
            config.getProtocol().setPort(protocolPort);
        }

        checkConfig(config);
        log.info("Success to get provider configuration : "+ config);
        return config;

    }

    private void checkConfig(ProviderConfiguration providerConfiguration) {

        String userReg = providerConfiguration.getRegistry().getType();
        RegistryType[] values = RegistryType.values();
        boolean isSupportReg = Arrays.stream(RegistryType.values()).anyMatch(type -> type.name().equals(userReg));

        if (!isSupportReg) {
            throw new KirinConfigException("Can't support registry : " + userReg);
        }

        String userProto = providerConfiguration.getProtocol().getType();
        boolean isSupportProto = Arrays.stream(ProtocolType.values()).anyMatch(type -> type.name().equals(userProto));
        if (!isSupportProto) {
            throw new KirinConfigException("Cant't support protocol : " + userProto);
        }
    }


}
