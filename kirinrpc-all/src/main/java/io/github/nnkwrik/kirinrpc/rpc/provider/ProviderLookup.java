package io.github.nnkwrik.kirinrpc.rpc.provider;

import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;

/**
 * @author nnkwrik
 * @date 19/05/21 17:14
 */
public interface ProviderLookup {

    ServiceWrapper lookupService(ServiceMeta serviceMeta);
}
