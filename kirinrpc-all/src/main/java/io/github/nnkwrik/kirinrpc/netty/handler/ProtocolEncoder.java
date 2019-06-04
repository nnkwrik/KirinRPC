package io.github.nnkwrik.kirinrpc.netty.handler;

import io.github.nnkwrik.kirinrpc.netty.model.PayloadHolder;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.ProtocolHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @author nnkwrik
 * @date 19/05/01 10:08
 */
@ChannelHandler.Sharable
public class ProtocolEncoder extends MessageToByteEncoder<PayloadHolder> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PayloadHolder msg, ByteBuf out) throws Exception {
        if (msg instanceof RequestPayload) {
            doEncodeRequest((RequestPayload) msg, out);
        } else if (msg instanceof ResponsePayload) {
            doEncodeResponse((ResponsePayload) msg, out);
        } else {
            throw new IllegalArgumentException(msg.getClass().getSimpleName());
        }

    }

    private void doEncodeRequest(RequestPayload msg, ByteBuf out) {
        out.writeShort(ProtocolHeader.MAGIC)
                .writeByte(ProtocolHeader.REQUEST)
                .writeByte(0x00)
                .writeLong(msg.id())
                .writeInt(msg.bytes().length)
                .writeBytes(msg.bytes());
    }

    private void doEncodeResponse(ResponsePayload msg, ByteBuf out) {
        out.writeShort(ProtocolHeader.MAGIC)
                .writeByte(ProtocolHeader.RESPONSE)
                .writeByte(msg.status())
                .writeLong(msg.id())
                .writeInt(msg.bytes().length)
                .writeBytes(msg.bytes());
    }
}
