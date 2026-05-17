package org.yomirein.sochatserver.netty.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.calls.CallHandler;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.messages.MessageHandler;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.friendship.FriendsHandler;
import org.yomirein.sochatserver.users.UsersHandler;
import org.yomirein.sochatserver.utils.JwtService;

import static org.yomirein.sochatserver.utils.MessageSender.sendError;

// WsPacketHandler.java handles everything except authentication xd
@RequiredArgsConstructor
public class WsPacketHandler extends SimpleChannelInboundHandler<MessagePacket> {

    private final SessionManager sessionManager;

    private final AuthHandler authHandler;
    private final FriendsHandler friendsHandler;
    private final UsersHandler usersHandler;
    private final ChatHandler chatHandler;
    private final MessageHandler messageHandler;
    private final CallHandler callHandler;

    // Handling every packet, they separated by their appointment
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket) throws Exception {
        switch (messagePacket.getType()) {
            // ping-pong and authentication without withAuth
            // PING PONG
            case "ping": ping(channelHandlerContext.channel()); break;
            // Authentication
            case "authenticate": authHandler.authorize(channelHandlerContext, messagePacket); break;

            // FRIENDSHIP SERVICE
            case "friend_request": withAuth(channelHandlerContext, messagePacket, friendsHandler::requestSend); break;
            case "friend_accept": withAuth(channelHandlerContext, messagePacket, friendsHandler::requestAccept); break;

            case "friend_remove": withAuth(channelHandlerContext, messagePacket, friendsHandler::removeFriend); break;
            case "block": withAuth(channelHandlerContext, messagePacket, friendsHandler::blockUser); break;
            case "friend_decline": withAuth(channelHandlerContext, messagePacket, friendsHandler::requestDecline); break;

            case "relatives_list": withAuth(channelHandlerContext, messagePacket, friendsHandler::getRelatives); break;

            // USER SERVICE
            case "user_get": withAuth(channelHandlerContext, messagePacket, usersHandler::getUser); break;
            case "user_update_profile": withAuth(channelHandlerContext, messagePacket, usersHandler::changeProfile); break;

            // CHAT MANAGEMENT
            case "chat_create": withAuth(channelHandlerContext, messagePacket, chatHandler::createChat); break;
            case "chat_list": withAuth(channelHandlerContext, messagePacket, chatHandler::getUserChats); break;
            case "chat_get": withAuth(channelHandlerContext, messagePacket, chatHandler::getChat); break;

            case "chat_delete": withAuth(channelHandlerContext, messagePacket, chatHandler::deleteChat); break;
            case "chat_leave": withAuth(channelHandlerContext, messagePacket, chatHandler::removeParticipant); break;

            case "chat_get_users": withAuth(channelHandlerContext, messagePacket, chatHandler::getChatUsers); break;
            case "chat_add_participant": withAuth(channelHandlerContext, messagePacket, chatHandler::addParticipant); break;

            // MESSAGE MANAGEMENT
            case "message_send": withAuth(channelHandlerContext, messagePacket, messageHandler::sendMessage); break;
            case "message_edit": withAuth(channelHandlerContext, messagePacket, messageHandler::editMessage); break;
            case "message_delete": withAuth(channelHandlerContext, messagePacket, messageHandler::deleteMessage); break;

            case "message_read": withAuth(channelHandlerContext, messagePacket, messageHandler::setLastReadMessage); System.out.println("t"); break;

            case "message_list": withAuth(channelHandlerContext, messagePacket, messageHandler::getRecentMessages); break;
            case "message_get": withAuth(channelHandlerContext, messagePacket, messageHandler::getMessage); break;


            // CALLS MANAGEMENT
            case "call_offer": withAuth(channelHandlerContext, messagePacket, callHandler::call); break;
            case "call_accept": withAuth(channelHandlerContext, messagePacket, callHandler::acceptCall); break;

            case "call_answer": withAuth(channelHandlerContext, messagePacket, callHandler::answerRtc); break;
            case "call_ice": withAuth(channelHandlerContext, messagePacket, callHandler::iceRtc); break;
            case "call_end": withAuth(channelHandlerContext, messagePacket, callHandler::endCall); break;
        }
    }

    // Ping Pong answer to user
    public void ping(Channel channel) {
        MessagePacket packetMessage = new MessagePacket("pong");
        packetMessage.payload.put("success", true);

        channel.writeAndFlush(packetMessage);
    }

    // Check authentication within handling almost every packet
    private void withAuth(ChannelHandlerContext ctx, MessagePacket messagePacket, AuthenticatedHandler handler) throws Exception {
        Long userId = null;

        if (sessionManager.isAuthenticated(ctx.channel())) {
            Session session = sessionManager.getSession(ctx.channel());
            if (!JwtService.isTokenValid(session.getToken())) {
                sendError(ctx, messagePacket, "invalid_token");
                sessionManager.removeSession(ctx.channel());
                return;
            }
            userId = session.getUser().getId();
        }

        handler.handle(ctx, messagePacket, userId);
    }


    // Interface to easier handling every incoming packet
    @FunctionalInterface
    private interface AuthenticatedHandler {
        void handle(ChannelHandlerContext ctx, MessagePacket messagePacket, Long userId) throws Exception;
    }

    // Exceptions
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
