package io.github.nnkwrik.kirinrpc.rpc;

import io.github.nnkwrik.kirinrpc.netty.protocol.Status;

/**
 * @author nnkwrik
 * @date 19/05/21 10:48
 */
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

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        String s = getClass().getName() + "(status=" + this.status + ")";
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
