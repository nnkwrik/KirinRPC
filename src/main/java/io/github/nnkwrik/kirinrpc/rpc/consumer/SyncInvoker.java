package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.netty.channel.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractMap;

/**
 * @author nnkwrik
 * @date 19/05/27 19:57
 */
public class SyncInvoker<T> implements InvocationHandler {
    private ServiceMeta serviceMeta;
    private ConnectorManager connectorManager;

    public SyncInvoker(Class<T> interfaceClass, String group) {
        serviceMeta = new ServiceMeta(interfaceClass.getName(), group);
        connectorManager = ConnectorManager.getInstance();
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
        AbstractMap.SimpleEntry<String, Channel> entry = connectorManager.chooseConnection(serviceMeta);
        String providerName = entry.getKey();
        Channel connection = entry.getValue();

        KirinRequest request = new KirinRequest();
        request.setProviderName(providerName);
        request.setServiceMeta(serviceMeta);
        request.setMethodName(method.getName());
        request.setArgTypes(method.getParameterTypes());
        request.setArgs(args);

        RPCFuture future = connectorManager.sendRequest(connection, request);
        Object result = future.get().getResult();

        if (result instanceof KirinRemoteException) {
            throw (KirinRemoteException) result;
        }

        return result;
    }
}
