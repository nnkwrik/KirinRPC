package io.github.nnkwrik.kirinrpc;

/**
 * @author nnkwrik
 * @date 19/05/27 16:43
 */
public class ConnectFailedException extends RuntimeException {

    public ConnectFailedException() {
    }

    public ConnectFailedException(String message) {
        super(message);
    }

    public ConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectFailedException(Throwable cause) {
        super(cause);
    }

    public ConnectFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
