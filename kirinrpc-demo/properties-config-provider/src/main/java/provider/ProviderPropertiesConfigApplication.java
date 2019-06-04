package provider;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@KirinProvider
@SpringBootApplication
public class ProviderPropertiesConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderPropertiesConfigApplication.class, args);
    }

}
