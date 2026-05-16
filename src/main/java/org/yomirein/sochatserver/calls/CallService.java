package org.yomirein.sochatserver.calls;

import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CallService {

    private final SessionManager sessionManager;
    Map<Long, P2PRoom> callRooms = new HashMap<Long, P2PRoom>();

    public void offer(long chatId, Session session) {
        P2PRoom p2pRoom = new P2PRoom(session);
        callRooms.put(chatId, p2pRoom);
    }

    public void answer(long chatId, Session session) {
        P2PRoom p2pRoom = callRooms.get(chatId);
        p2pRoom.setSession2(session);
    }

    public void deleteRoom(long chatId) {
        callRooms.remove(chatId);
    }
}