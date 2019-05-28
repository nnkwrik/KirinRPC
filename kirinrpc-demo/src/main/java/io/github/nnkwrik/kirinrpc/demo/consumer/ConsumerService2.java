package io.github.nnkwrik.kirinrpc.demo.consumer;

import io.github.nnkwrik.kirinrpc.demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import org.springframework.stereotype.Component;

/**
 * @author nnkwrik
 * @date 19/05/22 14:54
 */
//@Component
public class ConsumerService2 {
//    @KirinConsumeService
    HelloWorldService helloWorldService;//从缓存中取通过ConsumerService1构造的

    public void invokeRemoteService() {
        helloWorldService.sayHello("tom");
    }
}
