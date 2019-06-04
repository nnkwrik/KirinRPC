package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/31 13:35
 */
public abstract class AbstractInvoker<T> implements InvocationHandler {
    protected ServiceMeta serviceMeta;

    public AbstractInvoker(Class<T> interfaceClass, String group) {
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

        return doInvoke(request);
    }

    public abstract Object doInvoke(KirinRequest request) throws ExecutionException, InterruptedException;
}
