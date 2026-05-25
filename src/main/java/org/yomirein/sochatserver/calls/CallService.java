package org.yomirein.sochatserver.calls;

import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.calls.p2p.P2PRoom;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CallService {

    private final SessionManager sessionManager;
    Map<Long, P2PRoom> callRooms = new HashMap<Long, P2PRoom>();

    public void offer(long chatId, Session session, String offerSdp) {
        P2PRoom p2pRoom = new P2PRoom(chatId, session);
        p2pRoom.setOfferSdp(offerSdp);

        callRooms.put(chatId, p2pRoom);
    }

    public void answer(long chatId, Session session) {
        P2PRoom p2pRoom = callRooms.get(chatId);
        p2pRoom.setSession2(session);
    }

    public void deleteRoom(long chatId) { callRooms.remove(chatId); }

    public Optional<P2PRoom> findRoomBySession(Session session) {
        for (P2PRoom room : callRooms.values()) {
            if (room.getSession1() == session || room.getSession2() == session) {
                return Optional.of(room);
            }
        }
        return Optional.empty();
    }

    public boolean isChatInCall(long chatId) { return callRooms.containsKey(chatId); }
}