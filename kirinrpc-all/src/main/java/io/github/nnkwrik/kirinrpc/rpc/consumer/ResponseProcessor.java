package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.netty.channel.Channel;

/**
 * @author nnkwrik
 * @date 19/05/28 9:58
 */
public interface ResponseProcessor {
    /**
     * 处理正常响应
     */
    void handleResponse(Channel channel, ResponsePayload response) throws Exception;

    void shutdown();
}
