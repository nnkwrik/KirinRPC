package io.github.nnkwrik.kirinrpc.rpc.provider;

import io.github.nnkwrik.kirinrpc.common.util.StackTraceUtil;
import io.github.nnkwrik.kirinrpc.netty.handler.AcceptorHandler;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.netty.model.ResponsePayload;
import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.ServiceBeanContainer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinResponse;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceWrapper;
import io.github.nnkwrik.kirinrpc.serializer.Serializer;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/05/18 15:48
 */
@Slf4j
public class ProviderProcessor implements RequestProcessor {

    private final ServiceBeanContainer serviceBeanContainer;

    private static ThreadPoolExecutor threadPoolExecutor;

    private final ResponseSender responseSender;

    private final ProviderLookup providerLookup;

    public ProviderProcessor(ServiceBeanContainer serviceBeanContainer) {
        this.serviceBeanContainer = serviceBeanContainer;
        this.responseSender = new ResponseSenderImpl();
        this.providerLookup = new ProviderLookupImpl(serviceBeanContainer);
    }


    @Override
    public void handleRequest(Channel channel, RequestPayload requestPayload) throws Exception {
        RPCTask task = new RPCTask(channel, requestPayload, responseSender, providerLookup);

        submit(task);
    }

    @Override
    public void handleException(Channel channel, RequestPayload requestPayload, Throwable cause) {
        log.error("Handling exception (requestId = {}).", requestPayload.id());

        String msg = "Unknown Error happened when solve remote call";
        responseSender.sendErrorResponse(channel, new KirinRemoteException(msg, cause, Status.SERVER_ERROR));

    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }


    private static void submit(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (ProviderProcessor.class) {
                if (threadPoolExecutor == null) {
                    //双重锁创建线程池
                    threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    public static Long getRequestId(Channel channel) {
        return channel.attr(AcceptorHandler.requestIdAttrKey).get();
    }

    public static class ResponseSenderImpl implements ResponseSender {

        private Serializer serializer = SerializerHolder.serializerImpl();

        @Override
        public void sendSuccessResponse(Channel channel, Object invokeResult) {
            final Long requestId = getRequestId(channel);
            log.info("Success to invoke provider (requestId = {}), result = [{}].", requestId, invokeResult.toString());
            KirinResponse response = new KirinResponse();
            response.setResult(invokeResult);

            ResponsePayload responsePayload = new ResponsePayload(requestId);
            responsePayload.status(Status.OK.value());
            byte[] bytes = serializer.writeObject(response);
            responsePayload.bytes(bytes);

            sendResponsePayload(channel, responsePayload);
        }

        @Override
        public void sendFailResponse(Channel channel, KirinRemoteException e) {
            final Long requestId = getRequestId(channel);
            log.error("Excepted Error Happened when solve remote call (requestId = {}), {}.", requestId, StackTraceUtil.stackTrace(e));

            KirinResponse response = new KirinResponse();
            response.setError(e);

            ResponsePayload responsePayload = new ResponsePayload(requestId);
            responsePayload.status(e.getStatus().value());
            byte[] bytes = serializer.writeObject(response);
            responsePayload.bytes(bytes);

            sendResponsePayload(channel, responsePayload);
        }

        @Override
        public void sendErrorResponse(Channel channel, KirinRemoteException e) {
            final Long requestId = getRequestId(channel);
            log.error("Unknown Error happened when solve remote call (requestId = {}), {}.", requestId, StackTraceUtil.stackTrace(e));

            KirinResponse response = new KirinResponse();
            response.setError(e);

            ResponsePayload responsePayload = new ResponsePayload(requestId);
            responsePayload.status(e.getStatus().value());
            byte[] bytes = SerializerHolder.serializerImpl().writeObject(response);
            responsePayload.bytes(bytes);

            sendResponsePayload(channel, responsePayload, true);
        }

        private void sendResponsePayload(Channel channel, ResponsePayload responsePayload) {
            sendResponsePayload(channel, responsePayload, false);
        }

        private void sendResponsePayload(Channel channel, ResponsePayload responsePayload, boolean close) {
            if (responsePayload.status() == 0x00) {
                responsePayload.status(Status.OK.value());
            }

            channel.writeAndFlush(responsePayload).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.debug("Send response for request {}", responsePayload.id());
                    if (close) {
                        log.debug("Close the channel (requestId = {}).", responsePayload.id());
                        channel.close();
                    }
                }
            });
        }

    }

    public static class ProviderLookupImpl implements ProviderLookup {
        private final ServiceBeanContainer serviceBeanContainer;

        public ProviderLookupImpl(ServiceBeanContainer serviceBeanContainer) {
            this.serviceBeanContainer = serviceBeanContainer;
        }


        @Override
        public ServiceWrapper lookupService(ServiceMeta serviceMeta) {
            return serviceBeanContainer.lookupService(serviceMeta);
        }
    }
}
