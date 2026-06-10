package org.yomirein.sochatserver.search;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.users.User;

import java.util.List;

import static org.yomirein.sochatserver.utils.JsonConfig.*;
import static org.yomirein.sochatserver.utils.MessageSender.buildBaseResponse;
import static org.yomirein.sochatserver.utils.MessageSender.handleError;

@RequiredArgsConstructor
public class SearchHandler {

    private final SearchService searchService;

    public void searchUsers(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            List<User> usersList = searchService.findByUsername(getTextOrNull(messagePacket.getPayload(), "username"), getIntOrNull(messagePacket.getPayload(), "offset"));
            MessagePacket answerPacket = buildBaseResponse(messagePacket, "Got search users list successfully")
                    .put("users", MAPPER.writeValueAsString(usersList))
                    .build();

            channelHandlerContext.channel().writeAndFlush(answerPacket);

        } catch (Exception e) {
            handleError(channelHandlerContext, messagePacket, e);
        }

    }
}
