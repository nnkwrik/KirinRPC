package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.consumer.cluster.ClusterInvoker;
import io.github.nnkwrik.kirinrpc.rpc.consumer.cluster.FailoverClusterInvoker;
import io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer.LoadBalancer;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;

import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/27 19:57
 */
public class SyncInvoker<T> extends AbstractInvoker {
    private ClusterInvoker clusterInvoker;

    public SyncInvoker(LoadBalancer loadBalancer, Class<T> interfaceClass, String group) {
        super(interfaceClass, group);
        this.clusterInvoker = new FailoverClusterInvoker(loadBalancer, 2);
    }

    @Override
    public T doInvoke(KirinRequest request) throws ExecutionException, InterruptedException {
        RPCFuture<T> future = clusterInvoker.invoke(request);
        T result = future.get();

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
