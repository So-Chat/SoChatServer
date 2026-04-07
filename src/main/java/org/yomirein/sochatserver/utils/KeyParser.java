package org.yomirein.sochatserver.utils;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyParser {

    // Encode publicKey bytes to base64 string so we can send it
    public static String convertPublicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytes);

        return publicKeyString;
    }

    // Two methods that uses stringToPublicKey() just with different algorithms(Ed25519 and X25519)
    // Ed25519 for authorization and check for valid
    // X25519 for encrypting data
    public static PublicKey stringToPublicKeyED25519(String publicKeyString) throws Exception {
        return stringToPublicKey("Ed25519", publicKeyString);
    }
    public static PublicKey stringToPublicKeyX25519(String publicKeyString) throws Exception {
        return stringToPublicKey("X25519", publicKeyString);
    }

    // Decodes users string to PublicKey with specific Algorithm
    public static PublicKey stringToPublicKey(String algorithm, String publicKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyString);

        KeyFactory kf = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return kf.generatePublic(keySpec);
    }
}
