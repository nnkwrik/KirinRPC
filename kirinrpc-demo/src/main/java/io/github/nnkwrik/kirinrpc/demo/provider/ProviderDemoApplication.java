package io.github.nnkwrik.kirinrpc.demo.provider;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumer;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@KirinProvider(registryAddress = "127.0.0.1:2181")
@KirinConsumer(registryAddress = "127.0.0.1:2181")
public class ProviderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderDemoApplication.class, args);
    }

}
