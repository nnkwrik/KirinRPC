package io.github.nnkwrik.kirinrpc.demo.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nnkwrik
 * @date 19/05/28 11:54
 */
@RestController
public class TestController {

    @Autowired
    private ConsumerService consumerService;

    @GetMapping("/")
    public String test() {
        return consumerService.invokeRemoteService();
    }
}
