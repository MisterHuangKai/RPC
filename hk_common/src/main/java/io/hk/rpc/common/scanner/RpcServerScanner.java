package io.hk.rpc.common.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RpcServerScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用@RpcService注解标注的类
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(String host, int port, String scanPackage, RegistryService registryService) throws Exception {

    }


}
