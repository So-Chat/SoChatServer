package org.yomirein.sochatserver.calls;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.calls.p2p.IceCandidatePayload;
import org.yomirein.sochatserver.calls.p2p.P2PRoom;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.chats.ChatType;
import org.yomirein.sochatserver.chats.Participant;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.friendship.FriendshipService;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.utils.JwtService;
import org.yomirein.sochatserver.utils.MessageSender;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.yomirein.sochatserver.utils.MessageSender.notifyUser;
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

    // Getting turn credentials for calls
    public void turnCredentials(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            // If user already in call prevent to give any turn credential
            // THIS MISCONCEPTION WON'T WORK, TODO: NEED TO COMPLETELY REWORK SESSIONS
            /*
            if (callService.findRoomBySession(sessionManager.getSession(channelHandlerContext.channel())).isPresent()) {
                MessagePacket messagePacket1 = MessageSender.buildBaseResponse(messagePacket, "Can't get turn credentials because user already have one").build();
                channelHandlerContext.channel().writeAndFlush(messagePacket1);
                return;
            }*/

            TurnCredential turnCredential = JwtService.generateTurnCredential(userId);
            MessagePacket answerPacket = MessageSender.buildBaseResponse(messagePacket, "Got turn credentials successfully")
                    .put("username", turnCredential.username)
                    .put("credential", turnCredential.credential)
                    .build();
            channelHandlerContext.channel().writeAndFlush(answerPacket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void call(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            long chatId = messagePacket.getPayload().get("chat_id").asLong();
            long toId = messagePacket.getPayload().get("user_id").asLong();

            User toUser = userService.getUser(toId);
            Chat chat = chatService.getChat(chatId);

            if (chat.getChatType() != ChatType.PRIVATE) {
                MessagePacket messagePacket1 = MessageSender.buildBaseResponse(messagePacket, "You call in non private chat!").build();
                channelHandlerContext.channel().writeAndFlush(messagePacket1);
                return;
            }

            if (!friendshipService.isFriends(userId, toUser.getId())) {
                MessagePacket messagePacket1 = MessageSender.buildBaseResponse(messagePacket, "You can't call people if they are not in your Friend list!").build();
                channelHandlerContext.channel().writeAndFlush(messagePacket1);
                return;
            }

            Set<Session> userSessions = sessionManager.getUserSessions(toUser);
            if (userSessions.isEmpty()) {
                MessagePacket messagePacket1 = MessageSender.buildBaseResponse(messagePacket, "User is offline").build();
                channelHandlerContext.channel().writeAndFlush(messagePacket1);
            } String offer = messagePacket.getPayload().get("sdp").asText();

            callService.offer(chat.getId(), sessionManager.getSession(channelHandlerContext.channel()), offer);

            MessagePacket messagePacket1 = MessageSender.buildBaseResponse(
                    messagePacket, "Someone calling you").put("chat_id", chat.getId()).build();
            MessageSender.notifyUser(userSessions, messagePacket1);

            System.out.println("Room " + chat.getId() + " created");

        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void acceptCall(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            long chatId = messagePacket.getPayload().get("chat_id").asLong();

            System.out.println("Joining " + chatId);

            Chat chat = chatService.getChat(chatId);

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

            Chat chat = chatService.getChat(p2pRoom.getChatId());
            List<Participant> participants = chatService.getParticipantList(chat.getId());
            callService.deleteRoom(chat.getId());

            MessagePacket endCallPacket = MessageSender.buildBaseResponse(messagePacket, "Call end")
                    .put("chat_id", chat.getId()).put("success", true).build();

            for (Participant participant : participants) {
                Set<Session> participantSessions = sessionManager.getUserSessions(participant.getUserId());
                notifyUser(participantSessions, endCallPacket);
            }
        } catch (Exception e) {
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }



}
