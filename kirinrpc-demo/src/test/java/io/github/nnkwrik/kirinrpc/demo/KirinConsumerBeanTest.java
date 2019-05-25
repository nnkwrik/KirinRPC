package io.github.nnkwrik.kirinrpc.demo;

import io.github.nnkwrik.kirinrpc.springboot.config.consumer.ConsumerConfig;
import io.github.nnkwrik.kirinrpc.springboot.config.consumer.KirinConsumerBean;
import io.github.nnkwrik.kirinrpc.common.Constants;
import io.github.nnkwrik.kirinrpc.demo.api.HelloWorldService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

public class KirinConsumerBeanTest {

    @Test
    public void testSubscribe() throws Exception {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setName("name");
        consumerConfig.setRegistryAddress("127.0.0.1:2181");
        KirinConsumeService kirinConsumeService = new KirinConsumeService(){

            @Override
            public Class<? extends Annotation> annotationType() {
                return KirinConsumeService.class;
            }

            @Override
            public String group() {
                return Constants.ANY_GROUP;
            }
        };
        KirinConsumerBean<HelloWorldService> consumerBean
                = new KirinConsumerBean<>(consumerConfig, HelloWorldService.class, kirinConsumeService);

        consumerBean.getObject();


        Thread.sleep(TimeUnit.MINUTES.toMillis(5));


    }

}