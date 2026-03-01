package org.yomirein.sochatserver.common.models;

import lombok.Getter;
import lombok.Setter;

public class Challenge {
    @Getter @Setter
    String challenge;

    @Getter @Setter
    long expireTime;

    public Challenge(String challenge, long expireTime) {
        this.challenge = challenge;
        this.expireTime = expireTime;

    }

}
