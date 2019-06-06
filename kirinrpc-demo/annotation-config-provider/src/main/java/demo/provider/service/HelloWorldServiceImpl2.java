package demo.provider.service;

import demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvideService;

/**
 * @author nnkwrik
 * @date 19/04/25 12:41
 */
@KirinProvideService(group = "group2")
public class HelloWorldServiceImpl2 implements HelloWorldService {

    public String sayHello(String name) {
        return name + " say Hello. from group2 by annotation-config-provider";
    }

    public String sayWorld(String name) {
        return name + " say World. from group2 by annotation-config-provider";
    }
}
