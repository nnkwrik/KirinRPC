package io.github.nnkwrik.kirinrpc.netty.util;

import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.concurrent.TimeUnit;


/**
 * @author nnkwrik
 * @date 19/05/21 22:51
 */
public class PayloadUtil {

    public static final AttributeKey<Long> requestIdAttrKey = AttributeKey.newInstance("requestId");
    public static final AttributeKey<Long> requestTimeAttrKey = AttributeKey.newInstance("requestTime");

    public static void saveRequestInfoInChannel(Channel ch, RequestPayload requestPayload) {
        ch.attr(requestIdAttrKey).set(requestPayload.id());
        ch.attr(requestTimeAttrKey).set(requestPayload.timestamp());
    }

    public static long getRequestId(Channel ch) {
        Long requestId = ch.attr(requestIdAttrKey).get();
        if (requestId == null) {
            throw new NullPointerException("Can't find requestId in channel.You need to run PayloadUtil#saveRequestInfoInChannel first.");
        }
        return requestId;
    }

    public static long getProcessingTime(Channel ch, TimeUnit timeUnit) {
        Long requestTime = ch.attr(requestTimeAttrKey).get();
        if (requestTime == null) {
            throw new NullPointerException("Can't find requestTime in channel.You need to run PayloadUtil#saveRequestInfoInChannel first.");
        }

        long duration = System.currentTimeMillis() - requestTime;
        return TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }
}
