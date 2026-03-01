package org.yomirein.sochatserver.common.managers;

import lombok.Getter;
import org.yomirein.sochatserver.common.models.Challenge;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ChallengeManager {

    @Getter
    private final Map<Long, Challenge> challenges = new ConcurrentHashMap<>();

    public Challenge generateChallenge(long userId) {
        String challenge = UUID.randomUUID().toString();
        long expires = System.currentTimeMillis() + 120_000; // 2 minutes

        Challenge challengeObj = new Challenge(challenge, expires);
        challenges.put(userId, challengeObj);

        return challengeObj;
    }

    public Boolean verifyChallenge(byte[] message,
                                   byte[] signatureBytes,
                                   byte[] publicKeyBytes)
            throws Exception {

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PublicKey publicKey = keyFactory.generatePublic(
                spec
        );

        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(publicKey);
        verifier.update(message);

        return verifier.verify(signatureBytes);
    }

    public void startCleanupThread() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            challenges.entrySet().removeIf(entry -> entry.getValue().getExpireTime() < now);
        }, 60, 10, TimeUnit.SECONDS);
    }

    public boolean checkChallenge(Challenge challengeObj) {
        long now = System.currentTimeMillis();

        if (challenges.containsValue(challengeObj)) {
            if (challengeObj.getExpireTime() < now) {
                challenges.values().remove(challengeObj); // исправил remove
                return false;
            }
            return true;
        }
        return false;
    }
}
