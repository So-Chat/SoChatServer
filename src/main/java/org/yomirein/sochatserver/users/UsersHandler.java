package org.yomirein.sochatserver.users;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.common.repos.TrustKeysRepository;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class UsersHandler {

    private final SessionManager sessionManager;

    private final UserRepository userRepository;
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

        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendError(ChannelHandlerContext ctx,
                           MessagePacket request,
                           String errorMessage) {
        System.out.println("айй бляяя");
        MessagePacket packet = new MessagePacket.Builder()
                .type(request.getType())
                .put("success", false)
                .put("requestId", request.getPayload().get("requestId").asText())
                .put("server_message", errorMessage)
                .build();

        ctx.channel().writeAndFlush(packet);
    }

    private void notifyUser(User user, MessagePacket packet) {

        Set<Session> sessions = sessionManager.getUserSessions(user);

        for (Session session : sessions)
            session.getChannel().writeAndFlush(packet);
    }

}