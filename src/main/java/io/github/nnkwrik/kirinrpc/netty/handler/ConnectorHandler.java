package io.github.nnkwrik.kirinrpc.netty.handler;

import io.github.nnkwrik.kirinrpc.common.Constants;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
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


        ResponsePayload responsePayload = (ResponsePayload) msg;
        KirinResponse response = SerializerHolder.serializerImpl().readObject(responsePayload.bytes(), KirinResponse.class);

        System.out.println("==== " + response);
        ctx.channel().close();
    }
}
