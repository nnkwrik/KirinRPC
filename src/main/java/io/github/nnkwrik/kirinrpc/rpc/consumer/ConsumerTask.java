package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;

/**
 * @author nnkwrik
 * @date 19/05/28 10:02
 */
public class ConsumerTask implements Runnable {

    private final Channel channel;

    private final ResponsePayload responsePayload;

    private final ResponseReceiver receiver;

    public ConsumerTask(Channel channel, ResponsePayload responsePayload, ResponseReceiver receiver) {
        this.channel = channel;
        this.responsePayload = responsePayload;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        KirinResponse response;
        try {
            response = SerializerHolder.serializerImpl().readObject(responsePayload.bytes(), KirinResponse.class);
        } catch (Throwable t) {
            String msg = "Can't solve response payload.Fail to deserialize.";
            response = new KirinResponse();
            response.setError(new KirinRemoteException(msg, t, Status.DESERIALIZATION_FAIL));
        }

        java.lang.Object result = response.getResult();
        if (result instanceof KirinRemoteException
                && ((KirinRemoteException) result).getStatus() == Status.SERVICE_UNEXPECTED_ERROR) {

            receiver.receiveErrorResponse(channel, responsePayload.id(), (KirinRemoteException) result);

        } else if (result instanceof KirinRemoteException) {

            receiver.receiveFailResponse(channel, responsePayload.id(), (KirinRemoteException) result);

        } else {

            receiver.receiveSuccessResponse(channel, responsePayload.id(), result);

        }
    }
}
