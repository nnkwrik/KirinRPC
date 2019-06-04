package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/31 12:44
 */
public class AsyncFutureContext {

    private static ThreadLocal<RPCFuture> asyncFuture = new ThreadLocal<>();

    public static Object get() throws ExecutionException, InterruptedException {
        RPCFuture future = asyncFuture.get();
        if (future == null) {
            throw new NullPointerException("Can't find async invoke future in context.");
        }
        return future.get();
    }

    public static <T> T get(Class<T> returnType) throws ExecutionException, InterruptedException {
        return returnType.cast(get());
    }

    public static void set(RPCFuture future) {
        asyncFuture.set(future);
    }
}
