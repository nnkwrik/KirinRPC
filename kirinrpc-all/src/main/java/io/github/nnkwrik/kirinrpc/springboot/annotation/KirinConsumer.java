package io.github.nnkwrik.kirinrpc.springboot.annotation;

import io.github.nnkwrik.kirinrpc.springboot.config.consumer.KirinConsumerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nnkwrik
 * @date 19/05/22 12:27
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(KirinConsumerAutoConfiguration.class)
public @interface KirinConsumer {

    String name() default "";

    String registryAddress() default "";
}
