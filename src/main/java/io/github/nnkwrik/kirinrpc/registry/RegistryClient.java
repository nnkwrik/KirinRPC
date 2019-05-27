package io.github.nnkwrik.kirinrpc.registry;

import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.util.List;

/**
 * @author nnkwrik
 * @date 19/04/29 10:25
 */
public interface RegistryClient {

    void connect();

    void register(List<RegisterMeta> registerMetas);

    void subscribe(ServiceMeta serviceMeta, NotifyListener listener);

    enum RegisterState {
        PREPARE,
        DONE
    }
}
