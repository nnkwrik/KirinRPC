package io.github.nnkwrik.kirinrpc.rpc.provider;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
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
public class ProviderTask implements Runnable {

    private final Channel channel;

    private final RequestPayload requestPayload;

    private final ResponseSender sender;

    private final ProviderLookup providerLookup;

    public ProviderTask(Channel channel, RequestPayload requestPayload, ResponseSender sender, ProviderLookup providerLookup) {
        this.channel = channel;
        this.requestPayload = requestPayload;
        this.sender = sender;
        this.providerLookup = providerLookup;
    }


    @Override
    public void run() {
        try {
            //反序列化获取Request对象
            KirinRequest request;
            try {
                request = SerializerHolder.serializerImpl().readObject(requestPayload.bytes(), KirinRequest.class);
            } catch (Throwable t) {
                String msg = "Can't solve request payload.Fail to deserialize.";
                sender.sendFailResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                        new KirinRemoteException(msg, t, Status.BAD_REQUEST));
                return;
            }

            //自己是否为目标的provider
            if (!System.getProperty("kirin.provider.name").equals(request.getProviderName())) {
                String msg = String.format("Consumer excepted provider name is %s,but this provider name is %s.Refuse RPC request.",
                        request.getProviderName(),
                        System.getProperty("kirin.provider.name"));
                sender.sendFailResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                        new KirinRemoteException(msg, Status.APP_FLOW_CONTROL));
                return;
            }


            //查找服务
            final ServiceWrapper serviceProvider = providerLookup.lookupService(request.getServiceMeta());
            if (serviceProvider == null) {
                String msg = String.format("Can't lookup service provider for [serviceName = %s, serviceGroup = %s]",
                        request.getServiceMeta().getServiceName(),
                        request.getServiceMeta().getServiceGroup());
                sender.sendFailResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                        new KirinRemoteException(msg, Status.SERVICE_NOT_FOUND));
                return;
            }

            //调用服务方法
            Object invokeResult;
            try {
                invokeResult = invoke(request, serviceProvider);
            } catch (InvocationTargetException e) {
                String msg = "Fail to invoke service for RPC request.";
                sender.sendFailResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                        new KirinRemoteException(msg, e, Status.SERVICE_EXPECTED_ERROR));
                return;
            }

            //发送invoke结果
            try {
                sender.sendSuccessResponse(channel, requestPayload.id(), requestPayload.timestamp(), invokeResult);
            } catch (IllegalStateException e) {
                String msg = "Fail to serialize response.";
                sender.sendFailResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                        new KirinRemoteException(msg, e, Status.SERVICE_EXPECTED_ERROR));
                return;
            } catch (Throwable t) {
                String msg = "Fail to send response.";
                sender.sendFailResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                        new KirinRemoteException(msg, t, Status.SERVICE_EXPECTED_ERROR));
                return;
            }

        } catch (Throwable t) {
            String msg = "Unknown error happened when run rpc task";
            sender.sendErrorResponse(channel, requestPayload.id(), requestPayload.timestamp(),
                    new KirinRemoteException(msg, t, Status.SERVICE_UNEXPECTED_ERROR));
        }
    }


    private Object invoke(KirinRequest request, ServiceWrapper serviceWrapper) throws InvocationTargetException {
        Object provider = serviceWrapper.getServiceBean();
        Class<?> providerClass = provider.getClass();
        String methodName = request.getMethodName();
        Class<?>[] argTypes = request.getArgTypes();
        Object[] args = request.getArgs();


        log.debug("Invoke service for RPC request. " +
                        "[providerClass = {}, methodName = {}, argTypes = {}, args = {}]",
                providerClass.getName(),
                methodName,
                Arrays.stream(argTypes).map(Class::getName).collect(Collectors.toList()),
                Arrays.stream(args).map(Object::toString).collect(Collectors.toList()));

        //Cglib reflect
        FastClass providerFastClass = FastClass.create(providerClass);
        int methodIndex = providerFastClass.getIndex(methodName, argTypes);
        return providerFastClass.invoke(methodIndex, provider, args);
    }

}
