package io.hk.rpc.test.spi;

import io.hk.rpc.spi.loader.ExtensionLoader;
import io.hk.rpc.test.spi.service.SPIService;
import org.junit.Test;

/**
 * SPI测试类
 */
public class SPITest {
    @Test
    public void testSpiLoader() {
        SPIService spiService = ExtensionLoader.getExtension(SPIService.class, "spiService");
        String str = spiService.hello("雷子");
        System.out.println(str);
    }
}
