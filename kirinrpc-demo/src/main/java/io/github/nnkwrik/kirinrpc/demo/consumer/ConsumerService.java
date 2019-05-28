package io.github.nnkwrik.kirinrpc.demo.consumer;

import io.github.nnkwrik.kirinrpc.demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.demo.api.HelloWorldService2;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import org.springframework.stereotype.Component;

/**
 * @author nnkwrik
 * @date 19/05/22 14:54
 */
@Component
public class ConsumerService {
    @KirinConsumeService
    HelloWorldService helloWorldService;

//    @KirinConsumeService
//    HelloWorldService2 helloWorldService2;

    public String invokeRemoteService() {
        String response = helloWorldService.sayHello("tom");
        System.out.println(response);
        return response;
//        helloWorldService2.test();
    }
}
