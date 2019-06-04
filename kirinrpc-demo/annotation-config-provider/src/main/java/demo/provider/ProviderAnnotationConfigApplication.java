package demo.provider;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@KirinProvider(name = "kirin-provider",
        registryAddress = "127.0.0.1:2181",
        providerAddress = "127.0.0.1",
        providerPort = 7070)
public class ProviderAnnotationConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderAnnotationConfigApplication.class, args);
    }

}
