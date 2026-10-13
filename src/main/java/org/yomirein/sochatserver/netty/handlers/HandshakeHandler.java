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

        int length = byteBuf.readableBytes() - 4;
        byte[] bytes = new byte[length];

        byteBuf.skipBytes(4);
        byteBuf.readBytes(bytes);

        byte[][] keys = CryptoService.handshake(bytes);

        ByteBuf responseBuf = channelHandlerContext.alloc().buffer(32);
        responseBuf.writeBytes(keys[0]);

        Session session = new Session(channelHandlerContext.channel());
        session.setAesKey(keys[1]);

        sessionManager.addSession(channelHandlerContext.channel(), session);

        channelHandlerContext.writeAndFlush(responseBuf);

        channelHandlerContext.pipeline().remove(this);
    }
}
