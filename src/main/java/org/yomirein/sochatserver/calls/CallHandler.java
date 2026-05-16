package org.yomirein.sochatserver.calls;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.SessionManager;

@RequiredArgsConstructor
public class CallHandler {

    private final CallService callService;
    private final SessionManager sessionManager;

    public void call(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {

    }

    public void answer(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {

    }

    public void ice(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {

    }

    public void endCall(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {

    }



}
