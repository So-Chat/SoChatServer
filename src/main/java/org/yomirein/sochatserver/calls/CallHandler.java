package org.yomirein.sochatserver.calls;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.calls.p2p.IceCandidatePayload;
import org.yomirein.sochatserver.calls.p2p.P2PRoom;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.friendship.FriendshipService;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.utils.MessageSender;

import java.util.Optional;
import java.util.Set;

import static org.yomirein.sochatserver.utils.MessageSender.sendError;

/*
       Dope ### wizard tower on CallHandler
                    ^
                   | |
                -|^   ^|-
                |        |
              _|^        ^|_
              |____________|
                |       |
                |  ---- |
                |  |  | |
                | ------|
                |       |
                |       |
*/

@RequiredArgsConstructor
public class CallHandler {

    private final CallService callService;
    private final FriendshipService friendshipService;
    private final UserService userService;
    private final ChatService chatService;

    private final SessionManager sessionManager;


    public void call(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            Long toId = messagePacket.getPayload().get("callee_id").asLong();
            User toUser = userService.getUser(toId);

            if (!friendshipService.isFriends(userId, toUser.getId())) {
                MessagePacket messagePacket1 = MessageSender.buildBaseResponse(messagePacket, "You can't call people if they are not in your Friend list!").build();
                channelHandlerContext.channel().writeAndFlush(messagePacket1);
            }

            Set<Session> userSessions = sessionManager.getUserSessions(toUser);
            if (userSessions.isEmpty()) {
                MessagePacket messagePacket1 = MessageSender.buildBaseResponse(messagePacket, "User is offline").build();
                channelHandlerContext.channel().writeAndFlush(messagePacket1);
            }String offer = messagePacket.getPayload().get("sdp").asText();

            Chat chat = chatService.getChatByUsers(userId, toUser.getId());

            callService.offer(chat.getId(), sessionManager.getSession(channelHandlerContext.channel()), offer);

            MessagePacket messagePacket1 = MessageSender.buildBaseResponse(
                    messagePacket, "Someone calling you").put("chat_id", chat.getId()).build();
            MessageSender.notifyUser(userSessions, messagePacket1);
        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void acceptCall(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            Long toId = messagePacket.getPayload().get("call_id").asLong();
            User toUser = userService.getUser(toId);

            Chat chat = chatService.getChatByUsers(userId, toUser.getId());

            P2PRoom p2pRoom = callService.callRooms.get(chat.getId());

            callService.answer(chat.getId(), sessionManager.getSession(channelHandlerContext.channel()));

            MessagePacket offerPacket = new MessagePacket.Builder()
                    .type("call_accept")
                    .put("sdp", p2pRoom.getOfferSdp()).build();
            channelHandlerContext.channel().writeAndFlush(offerPacket);

            for (IceCandidatePayload iceCandidatePayload : p2pRoom.getCallerIce()) {
                MessagePacket icePacket = new MessagePacket.Builder()
                        .type("call_ice")
                        .put("candidate", iceCandidatePayload.getCandidate())
                        .put("sdp_mid", iceCandidatePayload.getSdpMid())
                        .put("sdp_mline_index", iceCandidatePayload.getSdpMLineIndex())
                        .build();
                channelHandlerContext.channel().writeAndFlush(icePacket);
            }


        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void answerRtc(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            Session userSession = sessionManager.getSession(channelHandlerContext.channel());
            P2PRoom p2pRoom = callService.findRoomBySession(userSession).orElseThrow(
                    () -> new IllegalStateException("Session is not in any call!")
            );


            Session otherSession = p2pRoom.getOther(userSession);

            String answer = messagePacket.getPayload().get("sdp").asText();

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type("call_answer")
                    .put("sdp", answer)
                    .build();

            otherSession.getChannel().writeAndFlush(answerPacket);
        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void iceRtc(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            Session userSession = sessionManager.getSession(channelHandlerContext.channel());
            P2PRoom p2pRoom = callService.findRoomBySession(userSession).orElseThrow(
                    () -> new IllegalStateException("Session is not in any call!")
            );

            Session otherSession = p2pRoom.getOther(userSession);

            String candidate = messagePacket.getPayload().get("candidate").asText();
            String sdpMid = messagePacket.getPayload().get("sdp_mid").asText();
            int sdpMLineIndex = messagePacket.getPayload().get("sdp_mline_index").asInt();

            if (otherSession == null) {
                IceCandidatePayload iceCandidatePayload = new IceCandidatePayload(candidate, sdpMid, sdpMLineIndex);
                p2pRoom.getCallerIce().add(iceCandidatePayload);
            }
            else {
                MessagePacket icePacket = new MessagePacket.Builder()
                        .type("call_ice")
                        .put("candidate", candidate)
                        .put("sdp_mid", sdpMid)
                        .put("sdp_mline_index", sdpMLineIndex)
                        .build();

                otherSession.getChannel().writeAndFlush(icePacket);
            }

        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void endCall(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            Session userSession = sessionManager.getSession(channelHandlerContext.channel());
            P2PRoom p2pRoom = callService.findRoomBySession(userSession).orElseThrow(
                    () -> new IllegalStateException("Session is not in any call!")
            );

            Chat chat = chatService.getChatByUsers(p2pRoom.getOther(userSession).getUser().getId(), userSession.getUser().getId());
            callService.deleteRoom(chat.getId());

            MessagePacket endCallPacket = new MessagePacket.Builder()
                    .type("call_end")
                    .put("server_message", "Call end")
                    .put("chat_id", chat.getId()).build();

            userSession.getChannel().writeAndFlush(endCallPacket);
            p2pRoom.getOther(userSession).getChannel().writeAndFlush(endCallPacket);

        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }



}
