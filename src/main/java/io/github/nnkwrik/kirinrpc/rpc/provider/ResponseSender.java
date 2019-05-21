package io.github.nnkwrik.kirinrpc.rpc.provider;

import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.netty.channel.Channel;


/**
 * @author nnkwrik
 * @date 19/05/21 17:14
 */
public interface ResponseSender {

    void sendSuccessResponse(Channel channel, Object invokeResult);

    void sendFailResponse(Channel channel, KirinRemoteException e);

    void sendErrorResponse(Channel channel, KirinRemoteException e);

}
