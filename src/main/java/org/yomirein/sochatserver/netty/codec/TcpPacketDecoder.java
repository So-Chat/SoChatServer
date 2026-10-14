package org.yomirein.sochatserver.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.yomirein.sochatserver.common.models.MessagePacket;

import static org.yomirein.sochatserver.utils.JsonConfig.MAPPER;

import java.util.List;

public class TcpPacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            throw new CorruptedFrameException("Packet too short");
        }

        int payloadLength = byteBuf.readableBytes() - 4;

        ByteBuf payload = byteBuf.readSlice(payloadLength);
        int requestId = byteBuf.readInt();

        MessagePacket packet = MAPPER.readValue(
                ByteBufUtil.getBytes(payload),
                MessagePacket.class
        );
        packet.setRequestId(requestId);
        list.add(packet);
    }
}
