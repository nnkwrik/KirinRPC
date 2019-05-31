package io.github.nnkwrik.kirinrpc.rpc.consumer.invoker;

import io.github.nnkwrik.kirinrpc.rpc.KirinRemoteException;
import io.github.nnkwrik.kirinrpc.rpc.consumer.Dispatcher;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;

import java.util.concurrent.ExecutionException;

/**
 * @author nnkwrik
 * @date 19/05/27 19:57
 */
public class SyncInvoker<T> extends AbstractInvoker {
    private Dispatcher dispatcher;

    public SyncInvoker(Dispatcher dispatcher, Class<T> interfaceClass, String group) {
        super(interfaceClass, group);
        this.dispatcher = dispatcher;
    }

    @Override
    public Object doInvoke(KirinRequest request) throws ExecutionException, InterruptedException {
        RPCFuture future = dispatcher.dispatch(request);
        Object result = future.get();

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
