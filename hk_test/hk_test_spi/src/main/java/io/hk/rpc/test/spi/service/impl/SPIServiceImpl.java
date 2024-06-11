package io.hk.rpc.test.spi.service.impl;

import io.hk.rpc.spi.annotation.SPIClass;
import io.hk.rpc.test.spi.service.SPIService;

/**
 * SPIService实现类
 */
@SPIClass
public class SPIServiceImpl implements SPIService {
    @Override
    public String hello(String name) {
        return "Are you ok ?" + name;
    }
}
