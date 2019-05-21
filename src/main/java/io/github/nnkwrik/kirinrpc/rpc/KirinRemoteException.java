package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.protocol.Status;
import lombok.Data;

/**
 * @author nnkwrik
 * @date 19/05/21 10:48
 */
@Data
public class KirinRemoteException extends RuntimeException {

    private Status status;


    public KirinRemoteException(String message, Status status) {
        super(message);
        this.status = status;
    }

    public KirinRemoteException(String message, Throwable cause, Status status) {
        super(message, cause);
        this.status = status;
    }


}
