package org.yomirein.sochatserver.utils;

import io.netty.channel.ChannelHandlerContext;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;

import java.util.Set;

// A little class for handlers where it generates easy messages to send users
public class MessageSender {

    // Send error
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

    // To reduce repetition
    public static void handleError(ChannelHandlerContext ctx, MessagePacket packet, Exception e) {
        e.printStackTrace();
        sendError(ctx, packet, e.getMessage());
    }

    // Generate simple response
    public static MessagePacket.Builder buildBaseResponse(MessagePacket request, String message) {
        if (request.getPayload().get("requestId") != null) {
            return new MessagePacket.Builder()
                    .type(request.getType())
                    .put("success", true)
                    .put("requestId", request.getPayload().get("requestId").asText())
                    .put("server_message", message);
        }
        return new MessagePacket.Builder()
                .type(request.getType())
                .put("server_message", message);
    }

    // Notify every connected user on specific account
    public static void notifyUser(User user, MessagePacket packet, SessionManager sessionManager) {
        Set<Session> sessions = sessionManager.getUserSessions(user);

        for (Session session : sessions) {
            session.getChannel().writeAndFlush(packet);
        }
    }
}
