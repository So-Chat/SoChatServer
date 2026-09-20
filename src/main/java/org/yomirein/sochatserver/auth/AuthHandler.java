package org.yomirein.sochatserver.auth;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

import org.yomirein.sochatserver.common.models.Challenge;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.utils.JsonConfig;
import org.yomirein.sochatserver.utils.JwtService;

import static org.yomirein.sochatserver.utils.MessageSender.buildBaseResponse;
import static org.yomirein.sochatserver.utils.MessageSender.sendError;


@RequiredArgsConstructor
public class AuthHandler {

    private final UserService userService;
    private final SessionManager sessionManager;
    private final AuthService authService;

    public void authorize(ChannelHandlerContext ctx, MessagePacket messagePacket) throws Exception {
        String token = messagePacket.getPayload().get("token").asText();

        User user;
        try {
            user = userService.getUser(JwtService.extractUsername(token));
        } catch (RuntimeException e) {
            sendError(ctx, messagePacket, "User not found");
            return;
        }

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

    public void register(ChannelHandlerContext ctx, MessagePacket messagePacket) throws Exception {
        authService.register(
            messagePacket.getPayload().get("username").asText(),
            messagePacket.getPayload().get("ed25519PublicKey").asText(),
            messagePacket.getPayload().get("x25519PublicKey").asText()
        );

        MessagePacket registerResponse = buildBaseResponse(messagePacket, "User created successfully").build();

        // Send answer
        ctx.channel().writeAndFlush(registerResponse);
    }

    public void login(ChannelHandlerContext ctx, MessagePacket messagePacket) throws Exception {
        // Sends challenge to complete in 5 minutes after sending from server
        // AuthService creates challenge
        Challenge challenge = authService.createChallenge(messagePacket.getPayload().get("username").asText());

        MessagePacket challengeResponse = buildBaseResponse(messagePacket, "Signature challenge with your private key")
                .put("success", true)
                .put("challenge", challenge.getChallenge())
                .put("expire_time", challenge.getExpireTime())
                .build();

        ctx.channel().writeAndFlush(challengeResponse);

    }

    public void verify(ChannelHandlerContext ctx, MessagePacket messagePacket) throws Exception {

        String tokenString = authService.login(messagePacket.getPayload().get("username").asText(),
            messagePacket.getPayload().get("signature").asText(),
            messagePacket.getPayload().get("challenge").asText());

        MessagePacket loginResponse = buildBaseResponse(messagePacket, "Login success")
                .put("token", tokenString)
                .build();


        // Send answer
        ctx.channel().writeAndFlush(loginResponse);
    }
}
