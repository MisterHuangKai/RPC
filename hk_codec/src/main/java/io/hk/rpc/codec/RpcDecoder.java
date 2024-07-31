package io.hk.rpc.codec;

import io.hk.rpc.common.utils.SerializationUtils;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.enumeration.RpcType;
import io.hk.rpc.protocol.header.RpcHeader;
import io.hk.rpc.protocol.request.RpcRequest;
import io.hk.rpc.protocol.response.RpcResponse;
import io.hk.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * 实现 RPC解码
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {

    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < RpcConstants.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();

        short magic = in.readShort();
        if (magic != RpcConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal," + magic);
        }
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();

        ByteBuf serializationTypeByteBuf = in.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_COUNT);
        String serializationType = SerializationUtils.subString(serializationTypeByteBuf.toString(CharsetUtil.UTF_8));

        int dataLength = in.readInt();
        if (in.readableBytes() > dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        RpcType msgTypeEnum = RpcType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }

        RpcHeader header = new RpcHeader();
        header.setMagic(magic);
        header.setMsgType(msgType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setSerializationType(serializationType);
        header.setMsgLen(dataLength);
        Serialization serialization = getSerialization(serializationType);

        RpcRequest request;
        RpcResponse response;
        switch (msgTypeEnum) {
            case REQUEST:
//                request = serialization.deserialize(data, RpcRequest.class);
//                if (request != null) {
//                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
//                    protocol.setHeader(header);
//                    protocol.setBody(request);
//                    out.add(protocol);
//                }
//                break;
            case RESPONSE:
//                response = serialization.deserialize(data, RpcResponse.class);
//                if (response != null) {
//                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
//                    protocol.setHeader(header);
//                    protocol.setBody(response);
//                    out.add(protocol);
//                }
//                break;
            case HEARTBEAT_FROM_CONSUMER:
                // 服务消费者发送给服务提供者的心跳数据
                break;
            case HEARTBEAT_TO_CONSUMER:
                // 服务提供者响应服务消费者的心跳数据
                break;
            case HEARTBEAT_FROM_PROVIDER:
                // 服务提供者发送给服务消费者的心跳数据
                response = serialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT_TO_PROVIDER:
                // 服务消费者响应服务提供者的心跳数据
                request = serialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
        }
    }

}
