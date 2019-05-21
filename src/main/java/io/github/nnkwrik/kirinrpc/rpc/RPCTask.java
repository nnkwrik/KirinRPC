package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.common.util.StackTraceUtil;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;
import io.github.nnkwrik.kirinrpc.serializer.Serializer;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author nnkwrik
 * @date 19/05/18 16:05
 */
@Slf4j
public class RPCTask implements Runnable {

    private final ProviderProcessor processor;
    private final RequestPayload requestPayload;
    private final Serializer serializer;
    private final Channel channel;
    private final long requestId;

    public RPCTask(ProviderProcessor processor, Channel channel, RequestPayload requestPayload) {
        this.processor = processor;
        this.requestPayload = requestPayload;
        this.serializer = SerializerHolder.serializerImpl();
        this.channel = channel;
        this.requestId = requestPayload.id();
    }

    @Override
    public void run() {
        try {
            //反序列化获取Request对象
            KirinRequest request;
            try {
                request = serializer.readObject(requestPayload.bytes(), KirinRequest.class);
            } catch (Throwable t) {
                String msg = "Can't solve request payload.Fail to deserialize.";
                fail(Status.BAD_REQUEST, msg, t);
                return;
            }

            //查找服务
            final ServiceWrapper serviceProvider = processor.lookupService(request.getServiceMeta());
            if (serviceProvider == null) {
                String msg = String.format("Can't lookup service provider for [serviceName = %s, serviceGroup = %s]",
                        request.getServiceMeta().getServiceName(),
                        request.getServiceMeta().getServiceGroup());
                fail(Status.SERVICE_NOT_FOUND, msg);
                return;
            }


            //调用服务方法
            Object invokeResult;
            try {
                invokeResult = invoke(request, serviceProvider);
            } catch (InvocationTargetException e) {
                String msg = "Fail to invoke service for RPC request.";
                fail(Status.SERVICE_EXPECTED_ERROR, msg, e);
                return;
            }

            KirinResponse response = new KirinResponse();
            response.setResult(invokeResult);

            //序列化Response对象
            ResponsePayload responsePayload;
            try {
                responsePayload = new ResponsePayload(requestId);
                byte[] bytes = serializer.writeObject(response);
                responsePayload.bytes(bytes);
            } catch (Throwable t) {
                String msg = "Fail to serialize response.";
                fail(Status.SERVICE_EXPECTED_ERROR, msg, t);
                return;
            }

            //发送rpc调用结果
            sendResponsePayload(responsePayload);
        } catch (Throwable t) {
            String msg = "Unknown happened when solve remote call";
            fail(Status.SERVER_ERROR, msg, t);
        }

    }


    private Object invoke(KirinRequest request, ServiceWrapper serviceWrapper) throws InvocationTargetException {
        Object provider = serviceWrapper.getServiceBean();
        Class<?> providerClass = provider.getClass();
        String methodName = request.getMethodName();
        Class<?>[] argTypes = request.getArgTypes();
        Object[] args = request.getArgs();


        log.debug("Invoke service for RPC request(requestId = {}). " +
                        "[providerClass = {}, methodName = {}, argTypes = {}, args = {}]",
                requestId,
                providerClass.getName(),
                methodName,
                Arrays.stream(argTypes).map(Class::getName).collect(Collectors.toList()),
                Arrays.stream(args).map(Object::toString).collect(Collectors.toList()));

        //Cglib reflect
        FastClass providerFastClass = FastClass.create(providerClass);
        int methodIndex = providerFastClass.getIndex(methodName, argTypes);
        Object invokeResult = providerFastClass.invoke(methodIndex, provider, args);
        log.debug("Success to invoke service for RPC request(requestId = {}). result = [{}]", requestId, invokeResult);
        return invokeResult;

    }

    private void sendResponsePayload(ResponsePayload responsePayload) {
        if (responsePayload.status() == 0x00) {
            responsePayload.status(Status.OK.value());
        }

        channel.writeAndFlush(responsePayload).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("Send response for request {}", requestId);
            }
        });
    }

    private void fail(Status status, String message) {
        fail(status, message, null);
    }

    private void fail(Status status, String message, Throwable cause) {
        KirinRemoteException exception = new KirinRemoteException(message, cause);
        log.error("happened when solve remote call (requestId = {}), {}.", requestId, StackTraceUtil.stackTrace(cause));

        KirinResponse response = new KirinResponse();
        response.setError(exception);

        ResponsePayload responsePayload = new ResponsePayload(requestId);
        responsePayload.status(status.value());
        byte[] bytes = serializer.writeObject(response);
        responsePayload.bytes(bytes);

        sendResponsePayload(responsePayload);


    }
}
