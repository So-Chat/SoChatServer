package org.yomirein.sochatserver.netty.handlers;

import java.util.List;

import org.yomirein.sochatserver.sessions.SessionManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;;

@AllArgsConstructor
public class HeartbeatHandler extends MessageToMessageDecoder<ByteBuf> {

    private final SessionManager sessionManager;
    /*
        0x01 - Ping
        0x02 - Pong
    */
    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (!(msg instanceof ByteBuf buf) || !buf.isReadable()) {
            return false;
        }
        byte packetId = buf.getByte(buf.readerIndex());

        return packetId == 0x01 || packetId == 0x02;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte packetId = byteBuf.readByte();
        switch (packetId) {
            case 0x01: {
                channelHandlerContext.writeAndFlush(channelHandlerContext.alloc().buffer(1).writeByte(0x02));
                break;
            }
            case 0x02: default: {
                break;
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent e) {

            if (e.state() == IdleState.WRITER_IDLE) {
                channelHandlerContext.writeAndFlush(channelHandlerContext.alloc().buffer(1).writeByte(0x01));
            }
            else if (e.state() == IdleState.READER_IDLE) {
                System.out.println("User idling");
                if (sessionManager.isAuthenticated(channelHandlerContext.channel())) {
                    sessionManager.removeSession(channelHandlerContext.channel());
                }
                channelHandlerContext.close();
            }
        } else {
            super.userEventTriggered(channelHandlerContext, evt);
        }
    }
}
