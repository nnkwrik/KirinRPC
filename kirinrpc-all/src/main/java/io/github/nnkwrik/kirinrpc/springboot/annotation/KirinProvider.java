package io.github.nnkwrik.kirinrpc.springboot.annotation;

import io.github.nnkwrik.kirinrpc.springboot.config.provider.KirinProviderAutoConfiguration;
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
@Import(KirinProviderAutoConfiguration.class)
public @interface KirinProvider {

    String name() default "";

    //放到注册中心的信息
    String providerAddress() default "";

    int providerPort() default 7070;

    String registryAddress() default "";
}
