package benchmark.rpc;

import io.github.nnkwrik.kirinrpc.common.Constants;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvideService;

/**
 * @author nnkwrik
 * @date 19/06/06 10:21
 */
@KirinProvideService(group = Constants.ANY_GROUP, wight = Constants.DEFAULT_WIGHT)
public class ServiceImpl implements Service {

    public String hello(String arg) {
        return "hello " + arg;
    }

}
