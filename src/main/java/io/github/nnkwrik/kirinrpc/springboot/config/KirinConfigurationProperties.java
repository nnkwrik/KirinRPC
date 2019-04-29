package io.github.nnkwrik.kirinrpc.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nnkwrik
 * @date 19/04/25 13:56
 */
@Data
@ConfigurationProperties(prefix = "kirin")
public class KirinConfigurationProperties {

    private ProviderConfiguration provider = new ProviderConfiguration();

}
