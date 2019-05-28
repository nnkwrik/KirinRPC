package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;

/**
 * @author nnkwrik
 * @date 19/05/28 10:02
 */
public class ConsumerTask implements Runnable {

    private final ResponsePayload responsePayload;

    private final ConnectorManager connectorManager;

    public ConsumerTask(ResponsePayload responsePayload) {
        this.responsePayload = responsePayload;
        this.connectorManager = ConnectorManager.getInstance();
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
        connectorManager.receiveResponse(responsePayload.id(), response);
    }
}
