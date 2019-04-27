package io.github.nnkwrik.kirinrpc.init.common;

/**
 * @author nnkwrik
 * @date 19/04/27 16:36
 */
public class KirinConfigException extends RuntimeException {
    public KirinConfigException() {
    }

    public KirinConfigException(String message) {
        super(message);
    }

    public KirinConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public KirinConfigException(Throwable cause) {
        super(cause);
    }

}
