package io.hk.rpc.test.registry;

import io.hk.rpc.protocol.meta.ServiceMeta;
import io.hk.rpc.registry.api.RegistryService;
import io.hk.rpc.registry.api.config.RegistryConfig;
import io.hk.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Zookeeper注册测试
 *
 * @author HuangKai
 * @date 2024/5/21
 */
public class ZookeeperRegistryTest {

    private RegistryService registryService;
    private ServiceMeta serviceMeta;

    @Before
    public void init() throws Exception {
        RegistryConfig registryConfig = new RegistryConfig("47.103.9.3:2181", "zookeeper", "1");
        this.registryService = new ZookeeperRegistryService();
        this.registryService.init(registryConfig);
        this.serviceMeta = new ServiceMeta(ZookeeperRegistryTest.class.getName(), "1.0.0", "hk", "127.0.0.1", 8080, 1);
    }

    @Test
    public void testRegister() throws Exception {
        this.registryService.register(serviceMeta);
    }

    @Test
    public void testUnregister() throws Exception {
        this.registryService.unRegister(serviceMeta);
    }

    @Test
    public void testDiscovery() throws Exception {
        this.registryService.discovery(RegistryService.class.getName(), "hk".hashCode());
    }

    @Test
    public void testDestroy() throws Exception {
        this.registryService.destroy();
    }


    // 网上找到的其他写法, 仅供了解。因为会报错: ERROR ClientCnxn:532 - Error while calling watcher, 但是插入zookeeper成功
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("47.103.9.3:2181", 6000, null);
        Thread.sleep(1000);
        if (zooKeeper.exists("/test1", false) == null) {
            zooKeeper.create("/test1", "znode1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        System.out.println(new String(zooKeeper.getData("/test1", false, null)));
    }

}
