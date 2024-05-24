package io.hk.rpc.loadbalancer.api;

import java.util.List;

/**
 * 负载均衡 接口
 *
 * @author HuangKai
 * @date 2024/5/24
 */
public interface ServiceLoadBalancer<T> {

    /**
     * 以负载均衡的方式选取一个服务节点
     *
     * @param servers  服务列表
     * @param hashCode Hash值
     * @return 可用的服务节点
     */
    T select(List<T> servers, int hashCode);

}
