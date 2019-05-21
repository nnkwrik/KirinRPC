package io.github.nnkwrik.kirinrpc.rpc;

/**
 * @author nnkwrik
 * @date 19/05/21 10:48
 */
public class KirinRemoteException extends RuntimeException {


    public KirinRemoteException(String message) {
        super(message);
    }

    public KirinRemoteException(String message, Throwable cause) {
        super(message, cause);
    }


}
