package io.hk.rpc.provider.spring;

import io.hk.rpc.provider.common.server.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

/**
 * @param
 * @author HuangKai
 * @date 2024/8/6
 * @return
 */
public class RpcSpringServer extends BaseServer implements ApplicationContextAware, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(RpcSpringServer.class);

}
