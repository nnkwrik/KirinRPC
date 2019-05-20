package io.github.nnkwrik.kirinrpc.registry;

import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;

import java.util.List;

/**
 * @author nnkwrik
 * @date 19/04/29 10:25
 */
public interface RegistryClient {
    void register(List<RegisterMeta> registerMetas);

    enum RegisterState {
        PREPARE,
        DONE
    }
}
