package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.protocol.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.channel.Channel;

/**
 * @author nnkwrik
 * @date 19/05/18 15:48
 */
public abstract class ProviderProcessor {


    public void handleRequest(Channel channel, RequestPayload requestPayload) throws Exception {

    }

    public void handleException(Channel channel, RequestPayload requestPayload, Throwable cause) {

    }

    public abstract Object lookupService(ServiceMeta serviceMeta);
}
