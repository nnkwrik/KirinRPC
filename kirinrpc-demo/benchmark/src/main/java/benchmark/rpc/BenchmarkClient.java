package benchmark.rpc;

import io.github.nnkwrik.kirinrpc.netty.cli.ConnectorManager;
import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;
import io.github.nnkwrik.kirinrpc.rpc.consumer.ProxyFactory;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.AsyncFutureContext;
import io.github.nnkwrik.kirinrpc.rpc.consumer.invoker.RPCFuture;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.nnkwrik.kirinrpc.common.Constants.ANY_GROUP;
import static io.github.nnkwrik.kirinrpc.common.Constants.DEFAULT_WIGHT;

/**
 * 在本机进行测试
 * <p>
 * 机器配置：
 * Intel® Core™ i5-8250U CPU @ 1.60GHz × 8
 * <p>
 * 测试结果：
 * - 同步调用
 * [SyncInvoke Benchmark] Request count: 25600000, time: 380 second, qps: 67368
 * - 异步调用
 * [AsyncInvoke Benchmark] Request count: 1024000, time: 12 second, qps: 85333
 *
 * @author nnkwrik
 * @date 19/06/06 10:00
 */
public class BenchmarkClient {


    private static Logger logger = LoggerFactory.getLogger(BenchmarkClient.class);

    public static void main(String[] args) {

        RegisterMeta registerMeta = new RegisterMeta();
        RegisterMeta.Address address = new RegisterMeta.Address("127.0.0.1", 7071);
        ServiceMeta serviceMeta = new ServiceMeta(Service.class.getName(), ANY_GROUP);
        registerMeta.setAppName("benchmark-provider");
        registerMeta.setWight(DEFAULT_WIGHT);
        registerMeta.setAddress(address);
        registerMeta.setServiceMeta(serviceMeta);


        ConnectorManager.getInstance().addConnection(registerMeta);

        syncCall();
//        futureCall();
    }

    private static void syncCall() {
        final Service service = ProxyFactory.factory(Service.class)
                .group(ANY_GROUP)
                .invokerType(ProxyFactory.InvokerType.SYNC)
                .newProxy();

        for (int i = 0; i < 10000; i++) {
            try {
                service.hello("warmUp");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final int t = 50000;
        final int step = 6;
        int processors = Runtime.getRuntime().availableProcessors();
        long start = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(processors << step);
        final AtomicLong count = new AtomicLong();
        for (int i = 0; i < (processors << step); i++) {
            new Thread(() -> {
                for (int i1 = 0; i1 < t; i1++) {
                    try {
                        service.hello("kirin");

                        if (count.getAndIncrement() % 10000 == 0) {
                            logger.warn("count=" + count.get());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
            logger.warn("count=" + count.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long second = (System.currentTimeMillis() - start) / 1000;
        logger.warn("[SyncInvoke Benchmark] Request count: " + count.get() + ", time: " + second + " second, qps: " + count.get() / second);

    }

    private static void futureCall() {
        final Service service = ProxyFactory.factory(Service.class)
                .group(ANY_GROUP)
                .invokerType(ProxyFactory.InvokerType.ASYNC)
                .newProxy();

        for (int i = 0; i < 10000; i++) {
            try {
                service.hello("warmUp");
                AsyncFutureContext.get();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        final int t = 8000;
        int processors = Runtime.getRuntime().availableProcessors();
        long start = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(processors << 4);
        final AtomicLong count = new AtomicLong();
        final int futureSize = 80;
        for (int i = 0; i < (processors << 4); i++) {
            new Thread(new Runnable() {
                List<RPCFuture> futures = new ArrayList<>(futureSize);

                @SuppressWarnings("all")
                @Override
                public void run() {
                    for (int i = 0; i < t; i++) {
                        try {
                            service.hello("kirin");
                            futures.add(AsyncFutureContext.getFuture());
                            if (futures.size() == futureSize) {
                                int fSize = futures.size();
                                for (int j = 0; j < fSize; j++) {
                                    try {
                                        futures.get(j).get();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                }
                                futures.clear();
                            }
                            if (count.getAndIncrement() % 10000 == 0) {
                                logger.warn("count=" + count.get());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!futures.isEmpty()) {
                        int fSize = futures.size();
                        for (int j = 0; j < fSize; j++) {
                            try {
                                futures.get(j).get();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        futures.clear();
                    }
                    latch.countDown();
                }
            }).start();
        }
        try {
            latch.await();
            logger.warn("count=" + count.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long second = (System.currentTimeMillis() - start) / 1000;
        logger.warn("[AsyncInvoke Benchmark] Request count: " + count.get() + ", time: " + second + " second, qps: " + count.get() / second);
    }

}
