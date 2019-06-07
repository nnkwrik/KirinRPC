package benchmark.rpc;

import io.github.nnkwrik.kirinrpc.netty.srv.KirinServerAcceptor;
import io.github.nnkwrik.kirinrpc.netty.srv.NettyAcceptor;
import io.github.nnkwrik.kirinrpc.rpc.provider.ServiceBeanContainer;

/**
 * @author nnkwrik
 * @date 19/06/06 10:17
 */
public class BenchmarkServer {

    public static void main(String[] args) throws InterruptedException {
        ServiceBeanContainer serviceBeanContainer = new ServiceBeanContainer();
        serviceBeanContainer.addServiceBean(new ServiceImpl());

        NettyAcceptor acceptor = new KirinServerAcceptor(serviceBeanContainer, 7071);
        acceptor.start(false);
    }
}
