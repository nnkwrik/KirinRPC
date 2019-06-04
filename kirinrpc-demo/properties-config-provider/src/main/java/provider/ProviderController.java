package provider;

import demo.api.HelloWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nnkwrik
 * @date 19/06/04 8:13
 */
@RestController
public class ProviderController {

    @Autowired
    private HelloWorldService helloWorldService;

    @GetMapping("/")
    public String testProvider(){
        return helloWorldService.sayHello("danny");
    }
}
