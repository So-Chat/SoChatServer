package org.yomirein.sochatserver.netty.handlers;

import java.util.List;

import org.yomirein.sochatserver.netty.codec.crypto.CryptoService;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HandshakeHandler extends ByteToMessageDecoder {

    private final SessionManager sessionManager;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 36) {
            return;
        }

        int payloadLength = byteBuf.readableBytes() - Integer.BYTES;

        byte[] publicKey = new byte[payloadLength];
        byteBuf.readBytes(publicKey);

        int requestId = byteBuf.readInt();

        byte[][] keys = CryptoService.handshake(publicKey);

        Session session = new Session(channelHandlerContext.channel());
        session.setAesKey(keys[1]);

        sessionManager.addSession(channelHandlerContext.channel(), session);

        ByteBuf response = channelHandlerContext.alloc().buffer(36);
        response.writeBytes(keys[0]);
        response.writeInt(requestId);

        channelHandlerContext.writeAndFlush(response);

        channelHandlerContext.pipeline().remove(this);
    }
}
