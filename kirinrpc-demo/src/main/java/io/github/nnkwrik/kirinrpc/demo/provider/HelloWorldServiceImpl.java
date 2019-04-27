package io.github.nnkwrik.kirinrpc.demo.provider;

import io.github.nnkwrik.kirinrpc.demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.init.springboot.annotation.KirinProviderService;

/**
 * @author nnkwrik
 * @date 19/04/25 12:41
 */
@KirinProviderService
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String sayHello(String name) {
        return name + " say Hello.";
    }

    @Override
    public String sayWorld(String name) {
        return null;
    }
}
