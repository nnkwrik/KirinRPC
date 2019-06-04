package io.github.nnkwrik.kirinrpc.netty.model;

/**
 * @author nnkwrik
 * @date 19/05/01 10:17
 */
public class ResponsePayload extends PayloadHolder {

    private byte status;

    private transient long timestamp;//用于监控处理耗时

    public ResponsePayload(long id) {
        super(id);
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }

    public long timestamp() {
        return timestamp;
    }

    public void timestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
