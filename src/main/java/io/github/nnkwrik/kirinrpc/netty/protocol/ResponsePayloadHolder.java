package io.github.nnkwrik.kirinrpc.netty.protocol;

/**
 * @author nnkwrik
 * @date 19/05/01 10:17
 */
public class ResponsePayloadHolder extends PayloadHolder {


    private byte status;

    public ResponsePayloadHolder(long id) {
        super(id);
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }
}
