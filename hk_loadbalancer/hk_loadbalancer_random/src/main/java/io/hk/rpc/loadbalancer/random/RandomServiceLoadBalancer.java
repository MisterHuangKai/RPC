package io.hk.rpc.loadbalancer.random;

import io.hk.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

/**
 * 基于随机算法的负载均衡策略
 */
@SPIClass
public class RandomServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final Logger logger = LoggerFactory.getLogger(RandomServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashCode) {
        logger.info("基于随机算法的负载均衡策略...");

        if (servers == null || servers.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }

}
