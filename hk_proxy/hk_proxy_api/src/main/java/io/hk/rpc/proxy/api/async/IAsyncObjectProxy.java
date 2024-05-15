package io.hk.rpc.proxy.api.async;

import io.hk.rpc.proxy.api.future.RPCFuture;

/**
 * 异步访问接口
 *
 * @author HuangKai
 * @date 2024/5/15
 */
public interface IAsyncObjectProxy {

    /**
     * 异步代理对象调用方法
     *
     * @param funcName 方法名称
     * @param args     方法参数
     * @return 封装好的RPCFuture对象
     */
    RPCFuture call(String funcName, Object... args);

}
