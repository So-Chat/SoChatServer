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
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (!(msg instanceof ByteBuf buf) || !buf.isReadable()) {
            return false;
        }
        // 0x03 - Full json packet
        return buf.getByte(buf.readerIndex()) == 0x03;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byteBuf.readByte(); // Removing packetId
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
