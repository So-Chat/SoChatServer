package org.yomirein.sochatserver.crypto;

import java.security.SecureRandom;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public class AesCodec {

    private final int NONCE_BYTES_SIZE = 12;
    private final int MAC_BITS_SIZE = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] decrypt(byte[] aesKey, byte[] payload) throws InvalidCipherTextException {
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

    public byte[] encrypt(byte[] aesKey, byte[] text) throws InvalidCipherTextException {
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

}
