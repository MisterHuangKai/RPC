package io.hk.rpc.codec;

import io.hk.rpc.common.utils.SerializationUtils;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.header.RpcHeader;
import io.hk.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 实现 RPC编码
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> protocol, ByteBuf byteBuf) throws Exception {
        RpcHeader header = protocol.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        String serializationType = header.getSerializationType();
        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes(StandardCharsets.UTF_8));
//        Serialization jdkSerialization = getJdkSerialization();
        Serialization serialization = getSerialization(serializationType);
        byte[] data = serialization.serialize(protocol.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }

}
