package org.yomirein.sochatserver.netty.codec.crypto;

import java.util.List;

import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AesDecoder extends ByteToMessageDecoder {

    private final SessionManager sessionManager;

@Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        Session session = sessionManager.getSession(ctx.channel());
        byte[] decodedBytes = CryptoService.decrypt(session.getAesKey(), bytes);

        out.add(ctx.alloc().buffer().writeBytes(decodedBytes));
    }
}
