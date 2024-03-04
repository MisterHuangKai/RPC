package io.hk.rpc.protocol.header;

import io.hk.rpc.common.id.IdFactory;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.protocol.enumeration.RpcType;

/**
 * RpcHeaderFactory
 */
public class RpcHeaderFactory {

    public static RpcHeader getRequestHeader(String serializationType, int messageType) {
        RpcHeader header = new RpcHeader();
        long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setMsgType((byte) messageType);
        header.setStatus((byte) 0x1);
        header.setRequestId(requestId);
        header.setSerializationType(serializationType);
        return header;
    }

    public static RpcHeader getRequestHeader(String serializationType) {
        RpcHeader header = new RpcHeader();
        long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setMsgType((byte) RpcType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        header.setRequestId(requestId);
        header.setSerializationType(serializationType);
        return header;
    }

}
