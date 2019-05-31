package io.github.nnkwrik.kirinrpc.rpc.consumer.cluster;

import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;

import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/31 14:40
 */
public interface ClusterInvoker {

    <T> RPCFuture<T> invoke(KirinRequest request) throws ExecutionException, InterruptedException;

}
