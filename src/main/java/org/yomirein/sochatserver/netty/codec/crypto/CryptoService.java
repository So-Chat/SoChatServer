package org.yomirein.sochatserver.netty.codec.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

public class CryptoService {

    private static final int NONCE_BYTES_SIZE = 12;
    private static final int MAC_BITS_SIZE = 128;

    public static byte[][] handshake(byte[] userPublicKeyBytes) {
        X25519PublicKeyParameters userPublic = new X25519PublicKeyParameters(userPublicKeyBytes, 0);

        X25519KeyPairGenerator keyGen = new X25519KeyPairGenerator();
        keyGen.init(new X25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair serverPair = keyGen.generateKeyPair();

        X25519PrivateKeyParameters myPrivate = (X25519PrivateKeyParameters) serverPair.getPrivate();
        X25519PublicKeyParameters myPublic = (X25519PublicKeyParameters) serverPair.getPublic();

        X25519Agreement agreement = new X25519Agreement();
        agreement.init(myPrivate);
        byte[] sharedSecret = new byte[agreement.getAgreementSize()];
        agreement.calculateAgreement(userPublic, sharedSecret, 0);

        byte[] myPublicBytes = myPublic.getEncoded();
        byte[] finalAesKey = deriveAesKey(sharedSecret);

        return new byte[][] { myPublicBytes, finalAesKey };
    }

    public static byte[] deriveAesKey(byte[] sharedSecret) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());

        byte[] salt = new byte[0];
        byte[] info = "SoChat-E2E-AES-Key".getBytes(StandardCharsets.UTF_8);

        hkdf.init(new HKDFParameters(sharedSecret, salt, info));

        byte[] finalAesKey = new byte[32];
        hkdf.generateBytes(finalAesKey, 0, finalAesKey.length);
        return finalAesKey;
    }


    public static byte[] encrypt(SecureRandom secureRandom, byte[] aesKey, byte[] text) throws InvalidCipherTextException {
        byte[] nonce = new byte[NONCE_BYTES_SIZE];
        secureRandom.nextBytes(nonce);

        AEADBlockCipher cipher = GCMBlockCipher.newInstance(AESEngine.newInstance());
        KeyParameter keyParameters = new KeyParameter(aesKey);
        AEADParameters aeadParameters = new AEADParameters(keyParameters, MAC_BITS_SIZE, nonce);

        cipher.init(true, aeadParameters);

        byte[] cipherText = new byte[cipher.getOutputSize(text.length)];
        int len = cipher.processBytes(text, 0, text.length, cipherText, 0);
        cipher.doFinal(cipherText, len);

        byte[] finalPayload = new byte[NONCE_BYTES_SIZE + cipherText.length];
        System.arraycopy(nonce, 0, finalPayload, 0, NONCE_BYTES_SIZE);
        System.arraycopy(cipherText, 0, finalPayload, NONCE_BYTES_SIZE, cipherText.length);

        return finalPayload;
    }

    public static byte[] decrypt(byte[] aesKey, byte[] payload) throws InvalidCipherTextException {
        if (payload.length < NONCE_BYTES_SIZE + (MAC_BITS_SIZE / 8)) {
            throw new IllegalArgumentException("Payload is too short");
        }

        byte[] nonce = new byte[NONCE_BYTES_SIZE];
        System.arraycopy(payload, 0, nonce, 0, NONCE_BYTES_SIZE);

        int cipherTextLength = payload.length - NONCE_BYTES_SIZE;
        byte[] cipherText = new byte[cipherTextLength];
        System.arraycopy(payload, NONCE_BYTES_SIZE, cipherText, 0, cipherTextLength);

        AEADBlockCipher cipher = GCMBlockCipher.newInstance(AESEngine.newInstance());
        KeyParameter keyParameters = new KeyParameter(aesKey);
        AEADParameters aeadParameters = new AEADParameters(keyParameters, MAC_BITS_SIZE, nonce, null);

        cipher.init(false, aeadParameters);

        byte[] text = new byte[cipher.getOutputSize(cipherText.length)];
        int len = cipher.processBytes(cipherText, 0, cipherText.length, text, 0);

        cipher.doFinal(text, len);

        return text;
    }
}
