package provider;

import demo.api.EchoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nnkwrik
 * @date 19/06/04 8:13
 */
@RestController
public class ProviderController {

    @Autowired
    private EchoService echoService;

    @GetMapping("/{words}")
    public String testProvider(@PathVariable("words") String words) {
        return echoService.echo(words);
    }
}
