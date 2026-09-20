package org.yomirein.sochatserver.netty.codec;

import org.yomirein.sochatserver.common.models.MessagePacket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TcpPacketEncoder extends MessageToByteEncoder<MessagePacket> {
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, ByteBuf byteBuf) throws Exception {
        // Mapping our packet to bytes
        byte[] bytes = objectMapper.writeValueAsBytes(messagePacket);

        // Writing the length of our converted packet
        byteBuf.writeInt(bytes.length);

        // Writing converted packed itself
        byteBuf.writeBytes(bytes);
    }
}
