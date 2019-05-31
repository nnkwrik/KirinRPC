package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.netty.channel.Channel;


/**
 * @author nnkwrik
 * @date 19/05/31 8:21
 */
public interface ResponseReceiver {

    void receiveSuccessResponse(Channel channel, long requestId, String providerName, Object result);

    void receiveFailResponse(Channel channel, long requestId, String providerName, KirinRemoteException e);

    void receiveErrorResponse(Channel channel, long requestId, String providerName, KirinRemoteException e);
}
