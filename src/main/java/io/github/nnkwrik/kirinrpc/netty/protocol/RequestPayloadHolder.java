package io.github.nnkwrik.kirinrpc.netty.protocol;

/**
 * @author nnkwrik
 * @date 19/05/01 10:17
 */
public class RequestPayloadHolder extends PayloadHolder {

    private transient long timestamp;//用于监控处理耗时

    public RequestPayloadHolder(long id) {
        super(id);
    }

    public long timestamp() {
        return timestamp;
    }

    public void timestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
