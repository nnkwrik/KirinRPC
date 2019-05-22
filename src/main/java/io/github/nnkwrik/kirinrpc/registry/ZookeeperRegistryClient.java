package io.github.nnkwrik.kirinrpc.registry;

import io.github.nnkwrik.kirinrpc.common.util.JsonUtil;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author nnkwrik
 * @date 19/04/29 10:24
 */
@Slf4j
public class ZookeeperRegistryClient implements RegistryClient {
    private String registryAddress;

    //zk客户端
    private CuratorFramework configClient;
    private final int sessionTimeoutMs = 60 * 1000;
    private final int connectionTimeoutMs = 15 * 1000;

    //准备注册的RegisterMeta
    private final LinkedBlockingDeque<RegisterMeta> metaQueue = new LinkedBlockingDeque<>();
    //进行注册的线程池
    private final ExecutorService registryExecutor = Executors.newSingleThreadExecutor();
    //注册失败时进行重试的线程池
    private final ScheduledExecutorService registerScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    //与注册中心连接成功
    private CountDownLatch isConnected = new CountDownLatch(1);

    // Provider已发布的注册信息
    private final ConcurrentMap<RegisterMeta, RegisterState> registerMetaMap = new ConcurrentHashMap<>();

    public ZookeeperRegistryClient(String registryAddress) {
        this.registryAddress = registryAddress;
        connect();
    }

    private void connect() {
        configClient = CuratorFrameworkFactory.newClient(
                registryAddress, sessionTimeoutMs, connectionTimeoutMs, new ExponentialBackoffRetry(500, 20));

        configClient.getConnectionStateListenable().addListener((client, newState) -> {
            log.info("Zookeeper connection state changed {}.", newState);
            if (newState == ConnectionState.CONNECTED) {
                isConnected.countDown();
            } else if (newState == ConnectionState.RECONNECTED) {

                log.info("Zookeeper connection has been re-established, will re-subscribe and re-addServiceBean.");
//                // 重新订阅
//                for (RegisterMeta.ServiceMeta serviceMeta : getSubscribeSet()) {
//                    doSubscribe(serviceMeta);
//                }
                // 重新发布服务
                register(new ArrayList<>(registerMetaMap.keySet()));
            }
        });
        configClient.start();
    }

    @Override
    public void register(List<RegisterMeta> registerMetas) {
        metaQueue.addAll(registerMetas);
        registryExecutor.execute(() -> {
            try {
                isConnected.await();
            } catch (InterruptedException e) {
                log.warn("Interrupted when wait connect to registry server.");
            }
            while (!shutdown.get()) {
                RegisterMeta meta = null;
                try {
                    meta = metaQueue.poll(5,TimeUnit.SECONDS);
                    if (meta == null) break; //5秒内没有获取到需要注册的meta，说明所有meta都已经注册成功。
                    registerMetaMap.put(meta, RegisterState.PREPARE);
                    createNode(meta);
                } catch (InterruptedException e) {
                    log.warn("[addServiceBean.executor] interrupted.");
                } catch (Throwable t) {
                    if (meta != null) {
                        log.error("Register [{}] fail: {}, will try again...", meta.getServiceMeta(), t.getStackTrace());

                        // 间隔一段时间再重新入队, 让出cpu
                        final RegisterMeta finalMeta = meta;
                        registerScheduledExecutor.schedule(() -> {
                            createNode(finalMeta);
                        }, 1, TimeUnit.SECONDS);
                    }
                }
            }
        });

    }

    private void createNode(final RegisterMeta meta) {
        String directory = String.format("/kirinrpc/%s/%s",
                meta.getServiceMeta().getServiceGroup(),
                meta.getServiceMeta().getServiceName());

        try {
            if (configClient.checkExists().forPath(directory) == null) {
                configClient.create().creatingParentsIfNeeded().forPath(directory);
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Create parent path failed, directory: {}, {}.", directory, e.getStackTrace());
            }
        }

        // The znode will be deleted upon the client's disconnect.
        try {
            configClient.create().withMode(CreateMode.EPHEMERAL).inBackground((client, event) -> {
                if (event.getResultCode() == KeeperException.Code.OK.intValue()) {
                    registerMetaMap.put(meta, RegisterState.DONE);
                }
                log.info("Register on zookeeper: {} - {}.", meta, event);
            }).forPath(String.format("%s/%s",
                    directory,
                    JsonUtil.toJson(meta)));
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Create addServiceBean meta: {} path failed, {}.", meta, e.getStackTrace());
            }
        }

    }
}
