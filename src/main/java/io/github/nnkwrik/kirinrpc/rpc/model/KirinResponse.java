package io.github.nnkwrik.kirinrpc.rpc.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nnkwrik
 * @date 19/05/20 14:17
 */
@Data
public class KirinResponse implements Serializable {

    private Object result; // 响应结果对象, 也可能是异常对象, 由响应状态决定

    public void setError(Throwable cause) {
        this.result = result;
    }

}
