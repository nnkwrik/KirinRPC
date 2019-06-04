package io.github.nnkwrik.kirinrpc.netty.handler.cli;

import io.github.nnkwrik.kirinrpc.common.util.StackTraceUtil;
import io.github.nnkwrik.kirinrpc.netty.IdealStateException;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.rpc.consumer.ResponseProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author nnkwrik
 * @date 19/05/20 16:35
 */
@Slf4j
@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter {

    private final ResponseProcessor processor;

    public ConnectorHandler(ResponseProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (msg instanceof ResponsePayload) {
            try {
                processor.handleResponse(ch, (ResponsePayload) msg);
            } catch (Throwable t) {
                log.error("An exception was caught: {}, on {} #channelRead().", StackTraceUtil.stackTrace(t), ch);
            }
        } else {
            log.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel ch = ctx.channel();

        if (cause instanceof IdealStateException) {
            log.error("IdealState exception exception was caught, force to close channel: {}.\r\n{}", ch, StackTraceUtil.stackTrace(cause));

            ch.close();
        } else if (cause instanceof IOException) {
            log.error("An I/O exception was caught, force to close channel: {}.\r\n{}", ch, StackTraceUtil.stackTrace(cause));

            ch.close();
        } else if (cause instanceof DecoderException) {
            log.error("Decoder exception was caught, force to close channel: {}.\r\n{}", ch, StackTraceUtil.stackTrace(cause));
            ch.close();
        } else {
            log.error("Unexpected exception was caught, channel: {}.\r\n{}", ch, StackTraceUtil.stackTrace(cause));
        }

    }

}
