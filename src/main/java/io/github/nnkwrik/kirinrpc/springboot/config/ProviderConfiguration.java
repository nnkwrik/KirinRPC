package io.github.nnkwrik.kirinrpc.springboot.config;

import lombok.Data;

/**
 * @author nnkwrik
 * @date 19/04/27 11:11
 */
@Data
public class ProviderConfiguration {
    private boolean enable = true;

    private String name;

    private String serverAddress;

    private Integer serverPort;

    private String registryAddress;

}
