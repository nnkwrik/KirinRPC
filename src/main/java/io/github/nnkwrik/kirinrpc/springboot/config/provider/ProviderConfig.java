package io.github.nnkwrik.kirinrpc.springboot.config.provider;

import lombok.Data;

/**
 * @author nnkwrik
 * @date 19/04/27 11:11
 */
@Data
public class ProviderConfig {

    private String name;

    private String providerAddress;

    private Integer providerPort;

    private String registryAddress;

}
