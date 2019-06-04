package io.github.nnkwrik.kirinrpc.springboot.config.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nnkwrik
 * @date 19/04/27 11:11
 */
@Data
@ConfigurationProperties(prefix = "kirin.provider")
public class ProviderPropertiesConfig  {

    private boolean enable = true;

    private String name;

    private String providerAddress;

    private Integer providerPort;

    private String registryAddress;

}
