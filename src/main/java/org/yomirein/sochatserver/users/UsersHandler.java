package org.yomirein.sochatserver.users;

import java.util.Optional;
import java.util.Set;

import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.persistance.api.repositories.TrustKeysRepository;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.utils.JsonConfig;
import static org.yomirein.sochatserver.utils.MessageSender.notifyUser;
import static org.yomirein.sochatserver.utils.MessageSender.sendError;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UsersHandler {

    private final SessionManager sessionManager;

    private final TrustKeysRepository trustKeysRepository;

    private final UserService userService;

    public void getUser(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            User otherUser;
            if (messagePacket.getPayload().hasNonNull("username")) {
                String username = messagePacket.getPayload().get("username").asText();
                otherUser = userService.getUser(username);
            } else if (messagePacket.getPayload().hasNonNull("id")) {
                long id = messagePacket.getPayload().get("id").asLong();
                otherUser = userService.getUser(id);
            } else {
                throw new IllegalArgumentException("Neither username nor id present in payload");
            }

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Got fingerprint successfully!")
                    .put("user", JsonConfig.MAPPER.writeValueAsString(otherUser))
                    .build();

            Optional<String> trustKeyCheck = trustKeysRepository.getEncryptedKeyByUserIds(otherUser.getId(), userId);

            if (trustKeyCheck.isPresent()){
                String trustKey = trustKeyCheck.get();
                answerPacket.payload.put("fingerprint", JsonConfig.MAPPER.writeValueAsString(trustKey));
            }

            channelHandlerContext.channel().writeAndFlush(answerPacket);

        } catch (JsonProcessingException | IllegalArgumentException e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void changeProfile(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {

            String username = messagePacket.payload.has("username")
                    && !messagePacket.payload.get("username").isNull()
                    ? messagePacket.payload.get("username").asText()
                    : null;

            if (username != null && username.isBlank()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }

            String nickname = messagePacket.payload.has("nickname") && !messagePacket.payload.get("nickname").isNull()
                    ? messagePacket.payload.get("nickname").asText()
                    : null;

            String description = messagePacket.payload.has("description") && !messagePacket.payload.get("description").isNull()
                    ? messagePacket.payload.get("description").asText()
                    : null;

            String avatarId = messagePacket.payload.has("avatar_id") && !messagePacket.payload.get("avatar_id").isNull()
                    ? messagePacket.payload.get("avatar_id").asText()
                    : null;


            userService.changeProfile(userId, username, nickname, description, avatarId);
            User user = userService.getUser(userId);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Changed profile successfully")
                    .put("username", user.getUsername())
                    .put("nickname", user.getNickname())
                    .put("description", user.getDescription())
                    .put("avatar_id", user.getAvatarId())
                    .build();

            notifyUser(user, answerPacket, sessionManager);


        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            User user = userService.getUser(userId);
            Set<Session> userSessions = sessionManager.getUserSessions(user);
            userService.deleteUser(userId);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Deleted user successfully.")
                    .put("user_id", userId)
                    .build();

            notifyUser(userSessions, answerPacket);
        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
