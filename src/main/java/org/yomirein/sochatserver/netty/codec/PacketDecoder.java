package org.yomirein.sochatserver.netty.codec;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.List;

public class PacketDecoder extends MessageToMessageDecoder<Object> {

    ObjectMapper objectMapper = JsonConfig.MAPPER;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object msg, List<Object> out) throws Exception {
        if (msg instanceof WebSocketFrame frame) {
            if (frame instanceof PingWebSocketFrame || frame instanceof PongWebSocketFrame) {
                System.out.println("PONG");
                out.add(frame.retain());
            } else if (frame instanceof TextWebSocketFrame textFrame) {
                String text = textFrame.text();
                JsonNode root = objectMapper.readTree(text);
                String type = root.get("type").asText();
                ObjectNode payload = (ObjectNode) root.get("payload");
                out.add(new MessagePacket(type, payload));
            }
        }
    }
}