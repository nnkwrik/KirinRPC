package io.github.nnkwrik.kirinrpc.netty.protocol;

import io.github.nnkwrik.kirinrpc.serializer.Serializer;

/**
 * 序列化之前的KririnRequest
 *
 * @author nnkwrik
 * @date 19/05/01 9:47
 */
public class KirinRequestHolder {

    //status,id,timestamp

    private byte status;
    private long id;
    private byte[] bytes;
    private Serializer serializer;

    public KirinRequestHolder(long id, byte status) {
        this.id = id;
        this.status = status;
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }

    public long id() {
        return id;
    }

    public void id(long id) {
        this.id = id;
    }

    public void bytes(byte[] bytes, Serializer serializer) {
        this.bytes = bytes;
        this.serializer = serializer;
    }

    public byte[] bytes() {
        return bytes;
    }

    public Serializer serializer() {
        return serializer;
    }

}
