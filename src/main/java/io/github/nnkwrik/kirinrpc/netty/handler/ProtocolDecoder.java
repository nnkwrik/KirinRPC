package io.github.nnkwrik.kirinrpc.netty.handler;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.ProtocolHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Signal;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.github.nnkwrik.kirinrpc.netty.protocol.ProtocolHeader.*;

/**
 * @author nnkwrik
 * @date 19/05/01 9:04
 */
@Slf4j
public class ProtocolDecoder extends ReplayingDecoder<ProtocolDecoder.State> {

    //构造函数 设置初始的枚举类型是什么
    public ProtocolDecoder() {
        super(State.HEADER_MAGIC);
    }

    // 协议头
    private final ProtocolHeader header = new ProtocolHeader();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(in.readShort());             // MAGIC
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                header.sign(in.readByte());             // 消息标志位
                checkpoint(State.HEADER_STATUS);
            case HEADER_STATUS:
                header.status(in.readByte());           // 消息状态
                checkpoint(State.HEADER_ID);
            case HEADER_ID:
                header.id(in.readLong());               // 消息id
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.bodyLength(in.readInt());        // 消息体长度
                checkpoint(State.BODY);
            case BODY:
                switch (header.sign()) {
                    case HEARTBEAT:
                        log.debug("Receive heartBeat package");
                        break;
                    case REQUEST: {
                        byte[] bytes = new byte[header.bodyLength()];
                        in.readBytes(bytes);

                        RequestPayload requestHolder = new RequestPayload(header.id());
                        requestHolder.timestamp(System.currentTimeMillis());
                        requestHolder.bytes(bytes);
                        out.add(requestHolder);

                        break;
                    }
                    case RESPONSE: {
                        byte[] bytes = new byte[header.bodyLength()];
                        in.readBytes(bytes);

                        ResponsePayload responseHolder = new ResponsePayload(header.id());
                        responseHolder.status(header.status());
                        responseHolder.bytes(bytes);
                        out.add(responseHolder);
                    }

                    default:
                        throw new IllegalAccessException();
                }
                checkpoint(State.HEADER_MAGIC);
        }
    }

    private static void checkMagic(short magic) throws Signal {
        if (MAGIC != magic) {
            throw new IllegalArgumentException();
        }
    }

    enum State {
        HEADER_MAGIC,
        HEADER_SIGN,
        HEADER_STATUS,
        HEADER_ID,
        HEADER_BODY_LENGTH,
        BODY
    }
}
