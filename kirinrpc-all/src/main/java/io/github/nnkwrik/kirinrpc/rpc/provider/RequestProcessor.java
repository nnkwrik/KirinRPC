package io.github.nnkwrik.kirinrpc.rpc.provider;


import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.netty.channel.Channel;

/**
 * @author nnkwrik
 * @date 19/05/21 17:14
 */
public interface RequestProcessor {

    /**
     * 处理正常请求
     */
    void handleRequest(Channel channel, RequestPayload request) throws Exception;

    /**
     * 处理异常
     */
    void handleException(Channel channel, RequestPayload request, Throwable cause);

    void shutdown();
}
