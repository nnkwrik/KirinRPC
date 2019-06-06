package demo.consumer;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通过resource/application.yml配置消费者信息
 * 同样需要@KirinConsumer。
 * 如果同时在注解上页进行了配置，则优先使用resource/application.yml中的配置
 * <p>
 * 启动前先确保registry address下存在zookeeper服务器
 *
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
