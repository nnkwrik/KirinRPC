package io.github.nnkwrik.kirinrpc.demo;

import io.github.nnkwrik.kirinrpc.demo.provider.ProviderDemoApplication;
import io.github.nnkwrik.kirinrpc.springboot.config.KirinConfigurationProperties;
import io.github.nnkwrik.kirinrpc.springboot.config.ProviderConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProviderDemoApplication.class)
public class ProviderDemoApplicationTests {

    @Autowired
    public KirinConfigurationProperties configurationProperties;

    @Autowired
    public ProviderConfiguration providerConfig;

    @Test
    public void contextLoads() {

    }

}
