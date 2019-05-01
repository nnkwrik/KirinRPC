package io.github.nnkwrik.kirinrpc.netty;

import io.github.nnkwrik.kirinrpc.netty.protocol.PayloadHolder;
import io.github.nnkwrik.kirinrpc.netty.protocol.ProtocolHeader;
import io.github.nnkwrik.kirinrpc.netty.protocol.RequestPayloadHolder;
import io.github.nnkwrik.kirinrpc.netty.protocol.ResponsePayloadHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @author nnkwrik
 * @date 19/05/01 10:08
 */
public class ProtocolEncoder extends MessageToByteEncoder<PayloadHolder> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PayloadHolder msg, ByteBuf out) throws Exception {
        if (msg instanceof RequestPayloadHolder) {
            doEncodeRequest((RequestPayloadHolder) msg, out);
        } else if (msg instanceof ResponsePayloadHolder) {
            doEncodeResponse((ResponsePayloadHolder) msg, out);
        } else {
            throw new IllegalArgumentException(msg.getClass().getSimpleName());
        }

    }

    private void doEncodeRequest(RequestPayloadHolder msg, ByteBuf out) {
        out.writeShort(ProtocolHeader.MAGIC)
                .writeByte(ProtocolHeader.REQUEST)
                .writeByte(0x00)
                .writeLong(msg.id())
                .writeInt(msg.bytes().length)
                .writeBytes(msg.bytes());
    }

    private void doEncodeResponse(ResponsePayloadHolder msg, ByteBuf out) {
        out.writeShort(ProtocolHeader.MAGIC)
                .writeByte(ProtocolHeader.RESPONSE)
                .writeByte(msg.status())
                .writeLong(msg.id())
                .writeInt(msg.bytes().length)
                .writeBytes(msg.bytes());
    }
}
