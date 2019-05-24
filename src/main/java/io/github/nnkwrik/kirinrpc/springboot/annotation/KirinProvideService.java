package io.github.nnkwrik.kirinrpc.springboot.annotation;

import io.github.nnkwrik.kirinrpc.common.Constants;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nnkwrik
 * @date 19/04/25 11:48
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface KirinProvideService {

    String group() default Constants.ANY_GROUP;

}
