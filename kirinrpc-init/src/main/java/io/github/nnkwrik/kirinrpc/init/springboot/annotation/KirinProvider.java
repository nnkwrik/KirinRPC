package io.github.nnkwrik.kirinrpc.init.springboot.annotation;

import io.github.nnkwrik.kirinrpc.init.springboot.config.KirinAutoConfiguration;
import io.github.nnkwrik.kirinrpc.init.common.ProtocolType;
import io.github.nnkwrik.kirinrpc.init.common.RegistryType;
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

    RegistryType registryType() default RegistryType.zookeeper;

    String registryAddress() default "";

    ProtocolType protocolType() default ProtocolType.protobuf;

    String protocolPort() default ProtocolType.defaultPort;
}
