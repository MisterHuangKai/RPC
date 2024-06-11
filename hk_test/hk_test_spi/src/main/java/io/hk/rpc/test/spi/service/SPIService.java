package io.hk.rpc.test.spi.service;

import io.hk.rpc.spi.annotation.SPI;

/**
 * SPIService
 */
@SPI("spiService")
public interface SPIService {
    String hello(String name);
}
