package org.yomirein.sochatserver.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.yomirein.sochatserver.common.models.MessagePacket;

import static org.yomirein.sochatserver.utils.JsonConfig.MAPPER;

import java.util.List;

public class TcpPacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // Getting Bytes from received message and let bytebuf read it
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        // Making packet from bytes and pass it to next handler for work
        MessagePacket packet = MAPPER.readValue(bytes, MessagePacket.class);
        list.add(packet);
    }
}
