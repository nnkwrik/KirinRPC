package demo.consumer;

import demo.api.EchoService;
import demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nnkwrik
 * @date 19/05/28 11:54
 */
@RestController
public class ConsumerController {

    @KirinConsumeService(group = "group1")
    private HelloWorldService helloWorldService;

    @KirinConsumeService(group = "group2")
    private HelloWorldService helloWorldService2;

    @KirinConsumeService
    private EchoService echoService;

    @GetMapping("/hello")
    public String testConsumer() {
        return helloWorldService.sayHello("tom");
    }

    @GetMapping("/world")
    public String testConsumer2() {
        return helloWorldService2.sayWorld("jenny");
    }

    @GetMapping("/echo/{words}")
    public String echo(@PathVariable("words") String words) {
        return echoService.echo(words);
    }


}
