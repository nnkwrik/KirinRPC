package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;
import io.github.nnkwrik.kirinrpc.serializer.Serializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;

/**
 * @author nnkwrik
 * @date 19/05/18 16:05
 */
@Slf4j
public class RPCTask implements Runnable {

    private final RequestPayload requestPayload;
    private final ServiceBeanContainer serviceBeanContainer;
    private final Serializer serializer;
    private final Channel channel;

    public RPCTask(ServiceBeanContainer serviceBeanContainer, Channel channel, RequestPayload requestPayload) {
        this.requestPayload = requestPayload;
        this.serviceBeanContainer = serviceBeanContainer;
        this.serializer = requestPayload.serializer();
        this.channel = channel;
    }

    @Override
    public void run() {
        KirinRequest request = serializer.deserializeRequest(requestPayload);
        KirinResponse response = new KirinResponse();
        final ServiceWrapper serviceProvider = serviceBeanContainer.lookupService(request.getServiceMeta());
        //if (service == null) service_NOT_FOUND;
        try {
            Object invokeResult = invoke(request, serviceProvider);
            response.setResult(invokeResult);
            ResponsePayload responsePayload = serializer.serializeResponse(response, requestPayload.id());

            channel.writeAndFlush(responsePayload).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.debug("Send response for request {}", requestPayload.id());
                }
            });

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private Object invoke(KirinRequest request, ServiceWrapper serviceWrapper) throws InvocationTargetException {
        Object provider = serviceWrapper.getServiceBean();
        Class<?> providerClass = provider.getClass();
        String methodName = request.getMethodName();
        Class<?>[] argTypes = request.getArgTypes();
        Object[] args = request.getArgs();

        //Cglib reflect
        FastClass providerFastClass = FastClass.create(providerClass);
        int methodIndex = providerFastClass.getIndex(methodName, argTypes);
        return providerFastClass.invoke(methodIndex, provider, args);

    }
}
