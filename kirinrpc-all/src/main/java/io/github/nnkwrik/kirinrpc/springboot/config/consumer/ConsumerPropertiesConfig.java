package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nnkwrik
 * @date 19/04/27 11:11
 */
@Data
@ConfigurationProperties(prefix = "kirin.consumer")
public class ConsumerPropertiesConfig {

    private boolean enable = true;

    private String name;

    private String registryAddress;

}
