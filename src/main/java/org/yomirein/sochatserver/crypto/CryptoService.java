package org.yomirein.sochatserver.crypto;

import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

public class CryptoService {

    public void handshake(byte[] userPublicKeyBytes) {
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


        // Leave that part for handshake handler in future
        byte[] myPublicBytes = myPublic.getEncoded();
        /* For decoder
        Byteb responseBuf = ctx.alloc().buffer(32);
        responseBuf.writeBytes(myPublicBytes);
        ctx.writeAndFlush(responseBuf);
        */
        byte[] finalAesKey = deriveAesKey(sharedSecret);

    }

    public static byte[] deriveAesKey(byte[] sharedSecret) {
            HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());

            byte[] salt = new byte[0];
            byte[] info = "SoChat-E2E-AES-Key".getBytes();

            hkdf.init(new HKDFParameters(sharedSecret, salt, info));

            byte[] finalAesKey = new byte[32];
            hkdf.generateBytes(finalAesKey, 0, finalAesKey.length);

            return finalAesKey;
        }

}
