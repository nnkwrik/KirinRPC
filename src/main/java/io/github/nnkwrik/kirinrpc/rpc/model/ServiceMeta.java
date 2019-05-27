package io.github.nnkwrik.kirinrpc.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author nnkwrik
 * @date 19/05/18 16:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ServiceMeta {
    //服务接口名
    private String serviceName;
    //筛选
    private String serviceGroup;
}
