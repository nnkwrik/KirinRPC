package demo.consumer;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author nnkwrik
 * @date 19/05/22 14:50
 */
@SpringBootApplication
@KirinConsumer(name = "kirin-consumer", registryAddress = "127.0.0.1:2181")
public class ConsumerAnnotationConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerAnnotationConfigApplication.class, args);
    }
}
