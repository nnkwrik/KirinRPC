package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import io.github.nnkwrik.kirinrpc.common.util.Requires;
import io.github.nnkwrik.kirinrpc.rpc.consumer.cluster.ClusterInvoker;
import io.github.nnkwrik.kirinrpc.rpc.consumer.cluster.FailfastClusterInvoker;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;

import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/31 13:34
 */
public class AsyncInvoker<T> extends AbstractInvoker {
    private Class<T> interfaceClass;
    private ClusterInvoker clusterInvoker;

    public AsyncInvoker(LoadBalancer loadBalancer, Class<T> interfaceClass, String group) {
        super(interfaceClass, group);
        this.interfaceClass = interfaceClass;
        this.clusterInvoker = new FailfastClusterInvoker(loadBalancer);
    }

    @Override
    public Object doInvoke(KirinRequest request) throws ExecutionException, InterruptedException {
        RPCFuture future = clusterInvoker.invoke(request);

        AsyncFutureContext.set(future);

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
