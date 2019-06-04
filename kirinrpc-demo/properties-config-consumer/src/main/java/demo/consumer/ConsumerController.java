package demo.consumer;

import demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nnkwrik
 * @date 19/05/28 11:54
 */
@RestController
public class ConsumerController {

    @KirinConsumeService
    HelloWorldService helloWorldService;

    @GetMapping("/hello")
    public String testConsumer() {
        return helloWorldService.sayHello("tom");
    }

    @GetMapping("/world")
    public String testConsumer2() {
        return helloWorldService.sayWorld("jenny");
    }

}
