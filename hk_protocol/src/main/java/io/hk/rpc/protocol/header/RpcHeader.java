package io.hk.rpc.protocol.header;

import java.io.Serializable;

/**
 * 消息头，目前固定为32个字节
 * <p>
 * 魔数：验证自定义网络传输协议的最基本的校验信息，占据2字节空间。
 * 报文类型：消息的类型，可以分为请求消息、响应消息和心跳消息，占据1字节空间。
 * 状态：消息的状态，占据1字节空间。
 * 消息ID：消息的唯一标识，占据8字节空间。
 * 序列化类型：数据进行序列化和反序列化的类型标识，暂定16字节空间。
 * 数据长度：标识消息体的数据长度，占据4字节空间。
 */
public class RpcHeader implements Serializable {
    private static final long serialVersionUID = 6011436680686290298L;

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 报文类型 1byte | 状态 1byte |     消息 ID 8byte      |
    +---------------------------------------------------------------+
    |           序列化类型 16byte      |        数据长度 4byte          |
    +---------------------------------------------------------------+
    */

    /**
     * 魔数 2字节
     */
    private short magic;
    /**
     * 报文类型 1字节
     */
    private byte msgType;
    /**
     * 状态 1字节
     */
    private byte status;

    /**
     * 消息 ID 8字节
     */
    private long requestId;

    /**
     * 序列化类型16字节，不足16字节后面补0，约定序列化类型长度最多不能超过16
     */
    private String serializationType;

    /**
     * 消息长度 4字节
     */
    private int msgLen;


    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public void setMsgLen(int msgLen) {
        this.msgLen = msgLen;
    }


}