package io.github.nnkwrik.kirinrpc.springboot.annotation;

import io.github.nnkwrik.kirinrpc.common.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nnkwrik
 * @date 19/05/22 14:52
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KirinConsumeService {

    String group() default Constants.ANY_GROUP;
}
