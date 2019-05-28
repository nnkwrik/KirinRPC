package io.github.nnkwrik.kirinrpc.demo.consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsumerDemoApplication.class)
public class ConsumerDemoApplicationTest {

    @Autowired
    ConsumerService consumerService;

    @Test
    public void test() {
        consumerService.invokeRemoteService();
    }
}