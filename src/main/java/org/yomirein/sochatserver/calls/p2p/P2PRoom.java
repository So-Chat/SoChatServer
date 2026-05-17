package org.yomirein.sochatserver.calls.p2p;

import lombok.Getter;
import lombok.Setter;
import org.yomirein.sochatserver.sessions.Session;

import java.util.ArrayList;
import java.util.List;

public class P2PRoom {
    @Getter @Setter
    private Session session1;

    @Getter @Setter
    private Session session2;

    @Getter @Setter
    private String offerSdp;

    @Getter @Setter
    private List<IceCandidatePayload> callerIce = new ArrayList<>();

    @Getter @Setter
    private List<IceCandidatePayload> calleeIce = new ArrayList<>();

    public P2PRoom(Session session1) {
        this.session1 = session1;
    }

    public Session getOther(Session session) {
        if (session == session1) return session2;
        if (session == session2) return session1;
        return null;
    }

}
