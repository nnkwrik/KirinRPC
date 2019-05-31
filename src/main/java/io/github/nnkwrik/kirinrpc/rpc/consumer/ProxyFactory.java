package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.common.Constants;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.AsyncInvoker;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.SyncInvoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author nnkwrik
 * @date 19/05/27 19:46
 */
public class ProxyFactory<I> {

    private Class<I> interfaceClass;

    private String group = Constants.ANY_GROUP;

    private InvokerType invokerType = InvokerType.SYNC;

    private Dispatcher dispatcher;

    private ProxyFactory(Class<I> interfaceClass) {
        this.interfaceClass = interfaceClass;
        this.dispatcher = new Dispatcher();
    }

    public static <I> ProxyFactory<I> factory(Class<I> interfaceClass) {
        return new ProxyFactory<>(interfaceClass);
    }

    public ProxyFactory<I> group(String group) {
        this.group = group;
        return this;
    }

    public ProxyFactory<I> invokerType(InvokerType invokerType) {
        this.invokerType = invokerType;
        return this;
    }

    public I newProxy() {
        InvocationHandler handler = null;
        switch (invokerType) {
            case SYNC:
                handler = new SyncInvoker(dispatcher, interfaceClass, group);
                break;
            case ASYNC:
                handler = new AsyncInvoker(dispatcher, interfaceClass, group);
                break;
        }

        Object proxy = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                handler);

        return interfaceClass.cast(proxy);
    }

    public enum InvokerType {
        SYNC,
        ASYNC
    }
}
