package io.hk.rpc.protocol.enumeration;

/**
 * 协议的类型
 */
public enum RpcType {
    // 请求消息
    REQUEST(1),
    // 响应消息
    RESPONSE(2),
    // 心跳数据: 消费者,发起.
    HEARTBEAT_FROM_CONSUMER(3),
    // 心跳数据: 提供者,响应,消费者.
    HEARTBEAT_TO_CONSUMER(4),
    // 心跳数据: 提供者,发起.
    HEARTBEAT_FROM_PROVIDER(5),
    // 心跳数据: 消费者,响应,提供者.
    HEARTBEAT_TO_PROVIDER(6);

    private final int type;

    RpcType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static RpcType findByType(int type) {
        for (RpcType rpcType : RpcType.values()) {
            if (rpcType.getType() == type) {
                return rpcType;
            }
        }
        return null;
    }

}
