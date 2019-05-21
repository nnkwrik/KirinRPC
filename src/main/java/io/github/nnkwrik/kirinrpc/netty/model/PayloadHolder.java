package io.github.nnkwrik.kirinrpc.netty.model;

/**
 * 序列化之前的KririnRequest
 *
 * @author nnkwrik
 * @date 19/05/01 9:47
 */
public class PayloadHolder {

    //status,id,timestamp

    private long id;
    private byte[] bytes;

    public PayloadHolder(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public void id(long id) {
        this.id = id;
    }

    public void bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] bytes() {
        return bytes;
    }

}
