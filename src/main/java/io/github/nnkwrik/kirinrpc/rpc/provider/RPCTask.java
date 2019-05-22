package io.github.nnkwrik.kirinrpc.rpc.provider;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import io.github.nnkwrik.kirinrpc.netty.util.PayloadUtil;
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
public class RPCTask implements Runnable {

    private final Channel channel;

    private final RequestPayload requestPayload;

    private final ResponseSender responseSender;

    private final ProviderLookup providerLookup;

    public RPCTask(Channel channel, RequestPayload requestPayload, ResponseSender responseSender, ProviderLookup providerLookup) {
        this.channel = channel;
        this.requestPayload = requestPayload;
        this.responseSender = responseSender;
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
                responseSender.sendFailResponse(channel, new KirinRemoteException(msg, t, Status.BAD_REQUEST));
                return;
            }

            //查找服务
            final ServiceWrapper serviceProvider = providerLookup.lookupService(request.getServiceMeta());
            if (serviceProvider == null) {
                String msg = String.format("Can't lookup service provider for [serviceName = %s, serviceGroup = %s]",
                        request.getServiceMeta().getServiceName(),
                        request.getServiceMeta().getServiceGroup());
                responseSender.sendFailResponse(channel, new KirinRemoteException(msg, Status.SERVICE_NOT_FOUND));
                return;
            }

            //调用服务方法
            Object invokeResult;
            try {
                invokeResult = invoke(request, serviceProvider);
            } catch (InvocationTargetException e) {
                String msg = "Fail to invoke service for RPC request.";
                responseSender.sendFailResponse(channel, new KirinRemoteException(msg, e, Status.SERVICE_EXPECTED_ERROR));
                return;
            }

            //发送invoke结果
            try {
                responseSender.sendSuccessResponse(channel, invokeResult);
            } catch (IllegalStateException e) {
                String msg = "Fail to serialize response.";
                responseSender.sendFailResponse(channel, new KirinRemoteException(msg, e, Status.SERVICE_EXPECTED_ERROR));
                return;
            } catch (Throwable t) {
                String msg = "Fail to send response.";
                responseSender.sendFailResponse(channel, new KirinRemoteException(msg, t, Status.SERVICE_EXPECTED_ERROR));
                return;
            }

        } catch (Throwable t) {
            String msg = "Unknown error happened when run rpc task";
            responseSender.sendErrorResponse(channel, new KirinRemoteException(msg, t, Status.SERVER_ERROR));
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
                PayloadUtil.getRequestId(channel),
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
