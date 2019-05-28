package io.github.nnkwrik.kirinrpc.netty.handler.cli;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nnkwrik
 * @date 19/05/20 16:35
 */
@Slf4j
@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ResponsePayload) {
            ResponsePayload responsePayload = (ResponsePayload) msg;
            //TODO 在task里进行反序列化
            KirinResponse response = SerializerHolder.serializerImpl().readObject(responsePayload.bytes(), KirinResponse.class);
            ConnectorManager.getInstance().receiveResponse(responsePayload.id(), response);
        }
    }
}
