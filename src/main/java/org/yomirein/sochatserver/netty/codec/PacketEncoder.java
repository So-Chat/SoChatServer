package org.yomirein.sochatserver.netty.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.List;

import static org.yomirein.sochatserver.utils.JsonConfig.MAPPER;


// Simple packet encoder that encodes all data
public class PacketEncoder extends MessageToMessageEncoder<MessagePacket> {
    // It uses abstract MessageToMessageEncoder with encode() in it

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, List<Object> list) throws Exception {
        // We simply just put Type and Payload from MessagePacket in ObjectNode and write it as text
        ObjectNode root = MAPPER.createObjectNode();

        root.put("type", messagePacket.getType());
        root.set("payload", messagePacket.getPayload());

        list.add(new TextWebSocketFrame(root.toString()));
    }
}