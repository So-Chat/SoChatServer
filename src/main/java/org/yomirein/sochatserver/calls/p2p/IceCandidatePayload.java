package org.yomirein.sochatserver.calls.p2p;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class IceCandidatePayload {
    String candidate;
    String sdpMid;
    int sdpMLineIndex;
}
