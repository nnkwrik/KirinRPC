package io.github.nnkwrik.kirinrpc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author nnkwrik
 * @date 19/05/18 19:42
 */
@Slf4j
public abstract class NettyAcceptor {
    protected InetSocketAddress serverAddress;
    protected ServerBootstrap bootstrap;
    private int nBosses;
    private int nWorkers;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    protected volatile ByteBufAllocator allocator;

    public NettyAcceptor(int port) {
        this(port, 1, Runtime.getRuntime().availableProcessors() << 1);
    }

    public NettyAcceptor(int port, int nBosses, int nWorkers) {
        this.serverAddress = new InetSocketAddress(port);
        this.nBosses = nBosses;
        this.nWorkers = nWorkers;
    }

    public void init() {
        bossGroup = new NioEventLoopGroup(nBosses);
        workerGroup = new NioEventLoopGroup(nWorkers);
        //优先使用直接内存，提高性能
        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .childOption(ChannelOption.ALLOCATOR, allocator)
                /**
                 * backlog参数被定义为下面两个队列的大小之和
                 * 一个未完成连接的队列，此队列维护着那些已收到了客户端SYN分节信息，等待完成三路握手的连接，socket的状态是SYN_RCVD
                 * 一个已完成的连接的队列，此队列包含了那些已经完成三路握手的连接，socket的状态是ESTABLISHED
                 */
                .option(ChannelOption.SO_BACKLOG, 32768)
                /**
                 * [TCP/IP协议详解]中描述:
                 * 当TCP执行一个主动关闭, 并发回最后一个ACK ,该连接必须在TIME_WAIT状态停留的时间为2倍的MSL.
                 * 这样可让TCP再次发送最后的ACK以防这个ACK丢失(另一端超时并重发最后的FIN).
                 * 这种2MSL等待的另一个结果是这个TCP连接在2MSL等待期间, 定义这个连接的插口对(TCP四元组)不能再被使用.
                 * 这个连接只能在2MSL结束后才能再被使用.
                 *
                 * 具体实现：
                 * 许多具体的实现中允许一个进程重新使用仍处于2MSL等待的端口(通常是设置选项SO_REUSEADDR),
                 * 但TCP不能允许一个新的连接建立在相同的插口对上。
                 */
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                /**
                 * 为TCP套接字设置keepalive选项时, 如果在2个小时（实际值与具体实现有关）内在
                 * 任意方向上都没有跨越套接字交换数据, 则 TCP 会自动将 keepalive 探头发送到对端.
                 * 此探头是对端必须响应的TCP段.
                 *
                 * 期望的响应为以下三种之一:
                 * 1. 收到期望的对端ACK响应
                 *      不通知应用程序(因为一切正常), 在另一个2小时的不活动时间过后，TCP将发送另一个探头。
                 * 2. 对端响应RST
                 *      通知本地TCP对端已崩溃并重新启动, 套接字被关闭.
                 * 3. 对端没有响
                 *      套接字被关闭。
                 *
                 * 此选项的目的是检测对端主机是否崩溃, 仅对TCP套接字有效.
                 */
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                /**
                 * 对此连接禁用 Nagle 算法.
                 * 在确认以前的写入数据之前不会缓冲写入网络的数据. 仅对TCP有效.
                 *
                 * Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送.
                 * 但这不是重点, 关键是这个算法受TCP延迟确认影响, 会导致相继两次向连接发送请求包,
                 * 读数据时会有一个最多达500毫秒的延时.
                 *
                 * 这叫做“ACK delay”, 解决办法是设置TCP_NODELAY。
                 */
                .childOption(ChannelOption.TCP_NODELAY, true)
                /**
                 * 禁用掉半关闭的状态的链接状态
                 * TCP四次握手关闭连接的时候，step2-step3中出现的状态
                 */
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);

        log.info("netty acceptor completed initialization.");
    }

    public abstract void start(boolean sync) throws InterruptedException;

}
