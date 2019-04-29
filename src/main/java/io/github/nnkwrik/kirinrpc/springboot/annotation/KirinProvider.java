package io.github.nnkwrik.kirinrpc.springboot.annotation;

import io.github.nnkwrik.kirinrpc.springboot.config.KirinAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nnkwrik
 * @date 19/04/25 11:47
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(KirinAutoConfiguration.class)
public @interface KirinProvider {

    String name() default "";

    String serverAddress() default "";

    int serverPort() default 7070;

    String registryAddress() default "";
}
