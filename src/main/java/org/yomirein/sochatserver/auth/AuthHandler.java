package org.yomirein.sochatserver.auth;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserRepository;
import org.yomirein.sochatserver.utils.JsonConfig;
import org.yomirein.sochatserver.utils.JwtService;

import java.util.Optional;

import static org.yomirein.sochatserver.utils.MessageSender.sendError;


@RequiredArgsConstructor
public class AuthHandler {

    private final UserRepository userRepository;

    private final SessionManager sessionManager;


    public void authorize(ChannelHandlerContext ctx, MessagePacket messagePacket) throws Exception {
        String token = messagePacket.getPayload().get("token").asText();

        Optional<User> userCheck = userRepository.findByName(JwtService.extractUsername(token));

        if (userCheck.isEmpty()){
            sendError(ctx, messagePacket, "User not found");
            return;
        }

        User user = userCheck.get();

        if (JwtService.isTokenValid(token)){
            Session session = new Session(token, user, ctx.channel());

            sessionManager.addSession(
                    user,
                    ctx.channel(),
                    session
            );
            MessagePacket messagePacket1 = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("user", JsonConfig.MAPPER.writeValueAsString(user))
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .build();

            ctx.channel().writeAndFlush(messagePacket1);
        }
        else {sendError(ctx, messagePacket, "Invalid Token");}
    }

}
