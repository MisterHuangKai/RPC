package io.hk.rpc.proxy.api.callback;

/**
 * 异步回调接口
 *
 * @author HuangKai
 * @date 2024/5/11
 */
public interface AsyncRPCCallback {

    /**
     * 成功后的回调方法
     */
    void onSuccess(Object result);

    /**
     * 异常的回调方法
     */
    void onException(Exception e);

}
