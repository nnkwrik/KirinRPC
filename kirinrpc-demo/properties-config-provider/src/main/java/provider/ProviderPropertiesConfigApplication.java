package provider;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通过resource/application.yml配置提供者信息
 * 同样需要@KirinConsumer。
 * 如果同时在注解上页进行了配置，则优先使用resource/application.yml中的配置
 * <p>
 * 启动前先确保registry address下存在zookeeper服务器
 *
 * @author nnkwrik
 * @date 19/05/22 14:50
 */
@KirinProvider
@SpringBootApplication
public class ProviderPropertiesConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderPropertiesConfigApplication.class, args);
    }

}
