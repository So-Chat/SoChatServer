package org.yomirein.sochatserver.netty.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.List;

public class PacketEncoder extends MessageToMessageEncoder<MessagePacket> {
    ObjectMapper objectMapper = JsonConfig.MAPPER;


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, List<Object> list) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("type", messagePacket.getType());
        root.set("payload", messagePacket.getPayload());

        list.add(new TextWebSocketFrame(root.toString()));
    }
}