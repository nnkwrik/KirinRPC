package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.netty.channel.Channel;

/**
 * @author nnkwrik
 * @date 19/05/18 15:48
 */
public class ProviderProcessor {

    private final ServiceBeanContainer serviceBeanContainer;

    public ProviderProcessor(ServiceBeanContainer serviceBeanContainer) {
        this.serviceBeanContainer = serviceBeanContainer;
    }


    public void handleRequest(Channel channel, RequestPayload requestPayload) throws Exception {
        RPCTask task = new RPCTask(serviceBeanContainer, channel, requestPayload);
        task.run();
    }

    public void handleException(Channel channel, RequestPayload requestPayload, Throwable cause) {

    }

}
