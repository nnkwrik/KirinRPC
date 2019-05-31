package io.github.nnkwrik.kirinrpc.rpc.consumer;

import io.github.nnkwrik.kirinrpc.common.Constants;

import java.lang.reflect.Proxy;

/**
 * @author nnkwrik
 * @date 19/05/27 19:46
 */
public class ProxyFactory<I> {

    private Class<I> interfaceClass;

    private String group = Constants.ANY_GROUP;

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

    public I newProxy() {

        Object proxy = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new SyncInvoker(dispatcher, interfaceClass, group));

        return interfaceClass.cast(proxy);
    }
}
