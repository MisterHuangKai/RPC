package io.hk.rpc.provider.common.scanner;

import com.alibaba.fastjson.JSONObject;
import io.hk.rpc.annotation.RpcService;
import io.hk.rpc.common.helper.RpcServiceHelper;
import io.hk.rpc.common.scanner.ClassScanner;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.protocol.meta.ServiceMeta;
import io.hk.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * '@RpcService'注解扫描器
 */
public class RpcServiceScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，筛选其中使用@RpcService注解标注的类
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(String host, int port, String scanPackage, RegistryService registryService) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage, true);
        if (classNameList.isEmpty()) {
            return handlerMap;
        }
        classNameList.forEach(className -> {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    //优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    ServiceMeta serviceMeta = new ServiceMeta(getServiceName(rpcService), rpcService.version(), rpcService.group(), host, port, getWeight(rpcService.weight()));
                    //将元数据注册到注册中心
                    registryService.register(serviceMeta);

                    handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()), clazz.newInstance());
                    LOGGER.info("scan key ===>>> {}", JSONObject.toJSONString(handlerMap.keySet()));
                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception: {}", e);
            }
        });
        return handlerMap;
    }


    private static int getWeight(int weight) {
        if (weight < RpcConstants.SERVICE_WEIGHT_MIN) {
            weight = RpcConstants.SERVICE_WEIGHT_MIN;
        }
        if (weight > RpcConstants.SERVICE_WEIGHT_MAX) {
            weight = RpcConstants.SERVICE_WEIGHT_MAX;
        }
        return weight;
    }

    /**
     * 获取 serviceName
     */
    private static String getServiceName(RpcService rpcService) {
        // 优先使用 interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == null || clazz == void.class) {
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()) {
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }

}
