package demo.consumer;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author nnkwrik
 * @date 19/05/22 14:50
 */
@KirinConsumer
@SpringBootApplication
public class ConsumerPropertiesConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerPropertiesConfigApplication.class, args);
    }
}
