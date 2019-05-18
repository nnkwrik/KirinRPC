package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.protocol.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.serializer.Serializer;

/**
 * @author nnkwrik
 * @date 19/05/18 16:05
 */
public class MessageTask implements Runnable {

    private final RequestPayload requestPayload;
    private final ProviderProcessor processor;

    public MessageTask(ProviderProcessor processor, RequestPayload requestPayload) {
        this.requestPayload = requestPayload;
        this.processor = processor;
    }

    @Override
    public void run() {
        Serializer serializer = requestPayload.serializer();
        KirinRequest request = serializer.readObject(requestPayload.bytes(), KirinRequest.class);
        request.setPayload(requestPayload);

        final Object service = processor.lookupService(request.getServiceMeta());
        //if (service == null) service_NOT_FOUND;
        process(service);
    }

    private void process(Object service){

    }
}
