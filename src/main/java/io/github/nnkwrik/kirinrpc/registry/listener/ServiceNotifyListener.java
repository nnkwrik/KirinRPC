package io.github.nnkwrik.kirinrpc.registry.listener;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author nnkwrik
 * @date 19/05/27 19:37
 */
@Slf4j
public class ServiceNotifyListener implements NotifyListener {

    private ServiceMeta serviceMeta;
    private ConnectorManager connectorManager;

    //用于等待可用连接时的阻塞
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notifyCondition = lock.newCondition();


    public ServiceNotifyListener(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
        this.connectorManager = ConnectorManager.getInstance();
    }


    @Override
    public void notify(RegisterMeta registerMeta, NotifyListener.NotifyEvent event) {
        if (event == NotifyListener.NotifyEvent.CHILD_ADDED) {
            log.info("service {} has a new provider.provider address is {}", registerMeta.getServiceMeta(), registerMeta.getAddress());

            //拿到与该提供者的channel，如果没有与这个提供者的channel则创建
            connectorManager.addConnectionWithProvider(registerMeta.getAppName(),
                    registerMeta.getAddress(),
                    registerMeta.getServiceMeta());

            lock.lock();
            try {
                notifyCondition.signalAll();
            } finally {
                lock.unlock();
            }

        } else if (event == NotifyListener.NotifyEvent.CHILD_REMOVED) {
            log.info("service {} reduced a provider.provider address was {}", registerMeta.getServiceMeta(), registerMeta.getAddress());

            connectorManager.removeConnectionWithProvider(registerMeta.getAppName(),
                    registerMeta.getAddress(),
                    registerMeta.getServiceMeta());

        }
    }


    public boolean waitForAvailable(long timeoutMillis) {

        if (connectorManager.isAvailable(serviceMeta)) return true;

        boolean available = false;
        long remains = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        log.debug("Waiting available provider for {},max {} ms.", serviceMeta, timeoutMillis);
        lock.lock();
        try {
            // avoid "spurious wakeup" occurs
            while (!(available = connectorManager.isAvailable(serviceMeta))) {
                if ((remains = notifyCondition.awaitNanos(remains)) <= 0) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.getStackTrace();
            return false;
        } finally {
            lock.unlock();
        }

        return available || connectorManager.isAvailable(serviceMeta);
    }


}
