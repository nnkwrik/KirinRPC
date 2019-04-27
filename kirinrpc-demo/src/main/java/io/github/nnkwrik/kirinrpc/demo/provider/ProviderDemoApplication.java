package io.github.nnkwrik.kirinrpc.demo.provider;

import io.github.nnkwrik.kirinrpc.init.common.ProtocolType;
import io.github.nnkwrik.kirinrpc.init.common.RegistryType;
import io.github.nnkwrik.kirinrpc.init.springboot.annotation.KirinProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@KirinProvider(name = "provider-demo",
        registryType = RegistryType.zookeeper,
        registryAddress = "192.168.0.5:2181",
        protocolType = ProtocolType.protobuf,
        protocolPort = "7070")
public class ProviderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderDemoApplication.class, args);
    }

}
