package provider;

import demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvideService;

/**
 * @author nnkwrik
 * @date 19/04/25 12:41
 */
@KirinProvideService
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String sayHello(String name) {
        return name + " say Hello.";
    }

    @Override
    public String sayWorld(String name) {
        return name + " say World.";
    }
}
