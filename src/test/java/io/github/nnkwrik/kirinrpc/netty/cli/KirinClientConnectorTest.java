package io.github.nnkwrik.kirinrpc.netty.cli;

import io.github.nnkwrik.kirinrpc.common.Constants;
import io.github.nnkwrik.kirinrpc.netty.model.RequestPayload;
import io.github.nnkwrik.kirinrpc.rpc.model.KirinRequest;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.channel.Channel;
import org.junit.Test;

import java.util.Arrays;

public class KirinClientConnectorTest {

    @Test
    public void connect() throws InterruptedException {
        KirinClientConnector kirinClientConnector = new KirinClientConnector();

        KirinRequest request = new KirinRequest();
        request.setAppName("");
        request.setServiceMeta(new ServiceMeta("appName","io.github.nnkwrik.kirinrpc.demo.api.HelloWorldService", Constants.ANY_GROUP));
        request.setMethodName("sayHello");
        request.setArgTypes(new Class[]{String.class});
        request.setArgs(new Object[]{"tom"});

        RequestPayload requestPayload = new RequestPayload(1);
        requestPayload.bytes(SerializerHolder.serializerImpl().writeObject(request));

        Channel channel = kirinClientConnector.connect("127.0.0.1", 7070);
        channel.writeAndFlush(requestPayload);

        //阻塞
        channel.closeFuture().sync();
    }
}