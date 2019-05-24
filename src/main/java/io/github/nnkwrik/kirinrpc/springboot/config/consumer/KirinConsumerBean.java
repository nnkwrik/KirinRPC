package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author nnkwrik
 * @date 19/05/22 15:00
 */
@Slf4j
public class KirinConsumerBean<T> implements FactoryBean<T> {

    private Class<T> consumerClass;

    private KirinConsumeService consumeServiceAnnotation;

    public KirinConsumerBean(Class<T> consumerClass, KirinConsumeService consumeServiceAnnotation) {
        this.consumerClass = consumerClass;
        this.consumeServiceAnnotation = consumeServiceAnnotation;
    }

    @Override
    public T getObject() throws Exception {
        System.out.println(consumerClass.getName() + " --- " + consumeServiceAnnotation);
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }
}
