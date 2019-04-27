package io.github.nnkwrik.kirinrpc.init.springboot.config;

import lombok.Data;

/**
 * @author nnkwrik
 * @date 19/04/27 11:11
 */
@Data
public class ProviderConfiguration {
    private boolean enable = true;

    private String name = "";

    private KirinConfigurationProperties.Registry registry = new KirinConfigurationProperties.Registry();

    private KirinConfigurationProperties.Protocol protocol = new KirinConfigurationProperties.Protocol();

}
