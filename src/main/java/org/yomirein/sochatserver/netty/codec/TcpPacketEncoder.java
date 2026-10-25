package org.yomirein.sochatserver.netty.codec;

import static org.yomirein.sochatserver.utils.JsonConfig.MAPPER;

import org.yomirein.sochatserver.common.models.MessagePacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TcpPacketEncoder extends MessageToByteEncoder<MessagePacket> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, ByteBuf byteBuf) throws Exception {
        byte[] bytes = MAPPER.writeValueAsBytes(messagePacket);

        // Write packetId for full json(0x03)
        byteBuf.writeByte(0x03);

        byteBuf.writeBytes(bytes);
        byteBuf.writeInt(messagePacket.getRequestId());
    }
}
