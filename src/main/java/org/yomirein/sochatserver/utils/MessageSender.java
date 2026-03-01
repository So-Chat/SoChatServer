package org.yomirein.sochatserver.utils;

import io.netty.channel.ChannelHandlerContext;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;

import java.util.Set;

public class MessageSender {

    public static void sendError(ChannelHandlerContext ctx,
                                 MessagePacket request,
                                 String errorMessage) {
        MessagePacket packet = new MessagePacket.Builder()
                .type(request.getType())
                .put("success", false)
                .put("requestId", request.getPayload().get("requestId").asText())
                .put("server_message", errorMessage)
                .build();

        ctx.channel().writeAndFlush(packet);
    }

    public static void notifyUser(User user, MessagePacket packet, SessionManager sessionManager) {
        Set<Session> sessions = sessionManager.getUserSessions(user);

        for (Session session : sessions) {
            session.getChannel().writeAndFlush(packet);
        }
    }
}
