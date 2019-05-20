package io.github.nnkwrik.kirinrpc.rpc.model;

import lombok.Data;

/**
 * @author nnkwrik
 * @date 19/05/20 15:44
 */
@Data
public class ServiceWrapper {
    private Object serviceBean;

    public ServiceWrapper(Object serviceBean) {
        this.serviceBean = serviceBean;
    }
}
