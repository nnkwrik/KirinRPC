package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/31 12:44
 */
public class AsyncFutureContext {

    private static ThreadLocal<Map<String, RPCFuture>> asyncFuture = new ThreadLocal<>();


    public static Object get(ServiceMeta service) throws ExecutionException, InterruptedException {
        String key = service.getServiceGroup() + "/" + service.getServiceName();
        return get(key);
    }

    public static Object get(Class interfaceClass, String group) throws ExecutionException, InterruptedException {
        String key = group + "/" + interfaceClass.getName();
        return get(key);
    }

    public static Object get(String key) throws ExecutionException, InterruptedException {
        if (asyncFuture.get() == null || !asyncFuture.get().containsKey(key)) {
            //没有该服务的异步调用future
            throw new NullPointerException("Can't find async invoke future in context for key = " + key);
        }
        Map<String, RPCFuture> futureMap = asyncFuture.get();
        RPCFuture future = futureMap.remove(key);

        if (futureMap.isEmpty()) {
            asyncFuture.set(null);
        }

        return future.get();
    }

    public static void set(ServiceMeta service, RPCFuture future) {
        String key = service.getServiceGroup() + "/" + service.getServiceName();

        Map<String, RPCFuture> futureMap = asyncFuture.get();
        if (futureMap == null) {
            futureMap = new HashMap<>();
            asyncFuture.set(futureMap);
        }

        futureMap.put(key, future);
    }


}
