package org.yomirein.sochatserver.utils;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyParser {

    // TODO: MAKE YOUR DAMN AES KEYS
    public static String convertPublicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytes);

        return publicKeyString;
    }

    public static PublicKey stringToPublicKeyED25519(String publicKeyString) throws Exception {
        return stringToPublicKey("Ed25519", publicKeyString);
    }

    public static PublicKey stringToPublicKeyX25519(String publicKeyString) throws Exception {
        return stringToPublicKey("X25519", publicKeyString);
    }

    public static PublicKey stringToPublicKey(String algorithm, String publicKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyString);

        KeyFactory kf = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return kf.generatePublic(keySpec);
    }

    public static int[] toUnsignedBytes(byte[] signedBytes) {
        int[] unsigned = new int[signedBytes.length];
        for (int i = 0; i < signedBytes.length; i++) {
            unsigned[i] = signedBytes[i] & 0xFF;  // превратить signed byte в 0..255
        }
        return unsigned;
    }
}
