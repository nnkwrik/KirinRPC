package io.github.nnkwrik.kirinrpc.init.springboot.config;

import io.github.nnkwrik.kirinrpc.init.common.ProtocolType;
import io.github.nnkwrik.kirinrpc.init.common.RegistryType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nnkwrik
 * @date 19/04/25 13:56
 */
@Data
@ConfigurationProperties(prefix = "kirin")
public class KirinConfigurationProperties {

    private String name = "";

    private Registry registry = new Registry();

    private Protocol protocol = new Protocol();

    private ProviderConfiguration provider = new ProviderConfiguration();


    @Data
    public static class Registry {
        private String type = RegistryType.defaultRegistry;
        private String address = "";
    }


    @Data
    public static class Protocol {
        private String type = ProtocolType.defaultProtocol;
        private String port = ProtocolType.defaultPort;
    }


}
