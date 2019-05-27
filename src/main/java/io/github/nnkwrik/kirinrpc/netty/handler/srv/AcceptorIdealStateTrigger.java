package io.github.nnkwrik.kirinrpc.netty.handler.srv;

import io.github.nnkwrik.kirinrpc.netty.IdealStateException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 如果60秒没有收到心跳包，则抛出异常
 * @author nnkwrik
 * @date 19/04/30 9:27
 */
@Slf4j
@ChannelHandler.Sharable
public class AcceptorIdealStateTrigger extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                /**
                 * 60秒都没有收到心跳包.
                 */
                throw new IdealStateException("Did not receive heartbeat over 60 seconds.");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
