package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author nnkwrik
 * @date 19/05/27 19:57
 */
public class SyncInvoker<T> implements InvocationHandler {
    private ServiceMeta serviceMeta;
    private Dispatcher dispatcher;

    public SyncInvoker(Dispatcher dispatcher, Class<T> interfaceClass, String group) {
        this.dispatcher = dispatcher;
        this.serviceMeta = new ServiceMeta(interfaceClass.getName(), group);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (Object.class == method.getDeclaringClass()) {//方法所在的类是Object类
            String name = method.getName();
            if ("equals".equals(name)) {//被调用的时Object的equals方法
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        KirinRequest request = new KirinRequest();
        request.setServiceMeta(serviceMeta);
        request.setMethodName(method.getName());
        request.setArgTypes(method.getParameterTypes());
        request.setArgs(args);

        RPCFuture future = dispatcher.dispatch(request);
        Object result = future.get();

        switch (future.status()) {
            case SUCCESS:
                return result;
            case FAIL:
                throw (KirinRemoteException) result;
            case ERROR:
                throw (KirinRemoteException) result;
        }

        return null;
    }
}
