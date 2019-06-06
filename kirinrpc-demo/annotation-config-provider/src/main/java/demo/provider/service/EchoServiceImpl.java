package demo.provider.service;

import demo.api.EchoService;
import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinProvideService;

/**
 * @author nnkwrik
 * @date 19/06/05 8:22
 */
@KirinProvideService
public class EchoServiceImpl implements EchoService {

    public String echo(String words) {
        return words;

    }
}
