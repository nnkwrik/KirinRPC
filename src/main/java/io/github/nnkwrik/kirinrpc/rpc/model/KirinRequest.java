package io.github.nnkwrik.kirinrpc.rpc.model;

import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nnkwrik
 * @date 19/05/01 9:41
 */
@Data
public class KirinRequest implements Serializable {
    private String providerName;

    private ServiceMeta serviceMeta;

    private String methodName;

    private Class<?>[] argTypes;

    private Object[] args;

}
