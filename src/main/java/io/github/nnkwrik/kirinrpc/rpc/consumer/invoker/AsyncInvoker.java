package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import io.github.nnkwrik.kirinrpc.common.util.Requires;
import io.github.nnkwrik.kirinrpc.rpc.consumer.Dispatcher;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;

/**
 * @author nnkwrik
 * @date 19/05/31 13:34
 */
public class AsyncInvoker<T> extends AbstractInvoker {
    private Dispatcher dispatcher;
    private Class<T> interfaceClass;

    public AsyncInvoker(Dispatcher dispatcher, Class<T> interfaceClass, String group) {
        super(interfaceClass, group);
        this.dispatcher = dispatcher;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Object doInvoke(KirinRequest request) {
        RPCFuture future = dispatcher.dispatch(request);

        AsyncFutureContext.set(serviceMeta, future);

        return getTypeDefaultValue(interfaceClass);
    }

    private Object getTypeDefaultValue(Class<?> clazz) {
        Requires.requireNotNull(clazz, "clazz");

        if (clazz.isPrimitive()) {
            if (clazz == byte.class) {
                return (byte) 0;
            }
            if (clazz == short.class) {
                return (short) 0;
            }
            if (clazz == int.class) {
                return 0;
            }
            if (clazz == long.class) {
                return 0L;
            }
            if (clazz == float.class) {
                return 0F;
            }
            if (clazz == double.class) {
                return 0D;
            }
            if (clazz == char.class) {
                return (char) 0;
            }
            if (clazz == boolean.class) {
                return false;
            }
        }
        return null;
    }

}
