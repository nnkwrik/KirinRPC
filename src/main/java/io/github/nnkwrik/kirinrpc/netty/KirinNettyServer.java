package io.github.nnkwrik.kirinrpc.netty;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author nnkwrik
 * @date 19/04/29 12:33
 */
@Slf4j
public class KirinNettyServer {
    private InetSocketAddress serverAddress;
    private int nBosses;
    private int nWorkers;


    public KirinNettyServer(int port) {
        this(port, 1, Runtime.getRuntime().availableProcessors() << 1);
    }

    public KirinNettyServer(int port, int nBosses, int nWorkers) {
        this.serverAddress = new InetSocketAddress(port);
        this.nBosses = nBosses;
        this.nWorkers = nWorkers;
    }

    public void init(){

    }
}
