package io.github.nnkwrik.kirinrpc.demo.consumer;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author nnkwrik
 * @date 19/05/22 14:50
 */
@SpringBootApplication(scanBasePackages = {"io.github.nnkwrik.kirinrpc.demo.consumer", "io.github.nnkwrik.kirinrpc.demo.api"})
@KirinConsumer(registryAddress = "127.0.0.1:2181")
public class ConsumerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerDemoApplication.class, args);
    }
}
