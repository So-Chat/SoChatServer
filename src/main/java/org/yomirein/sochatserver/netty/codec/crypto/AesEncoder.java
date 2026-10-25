package org.yomirein.sochatserver.netty.codec.crypto;

import java.security.SecureRandom;

import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AesEncoder extends MessageToByteEncoder<ByteBuf> {

    private final SessionManager sessionManager;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        Session session = sessionManager.getSession(ctx.channel());
        byte[] encodedBytes = CryptoService.encrypt(secureRandom, session.getAesKey(), bytes);

        out.writeBytes(encodedBytes);
    }

}
