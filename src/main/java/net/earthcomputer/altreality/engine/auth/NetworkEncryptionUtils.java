package net.earthcomputer.altreality.engine.auth;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class NetworkEncryptionUtils {
    public static SecretKey generateKey() throws NetworkEncryptionException {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(128);
            return gen.generateKey();
        } catch (Exception var1) {
            throw new NetworkEncryptionException(var1);
        }
    }

    public static KeyPair generateServerKeyPair() throws NetworkEncryptionException {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new NetworkEncryptionException(e);
        }
    }

    public static byte[] generateServerId(String baseServerId, PublicKey publicKey, SecretKey secretKey) throws NetworkEncryptionException {
        try {
            return hash(baseServerId.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (Exception e) {
            throw new NetworkEncryptionException(e);
        }
    }

    private static byte[] hash(byte[]... bs) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        for (byte[] cs : bs) {
            digest.update(cs);
        }

        return digest.digest();
    }

    public static PublicKey readEncodedPublicKey(byte[] bs) throws NetworkEncryptionException {
        try {
            EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bs);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception e) {
            throw new NetworkEncryptionException(e);
        }
    }

    public static SecretKey decryptSecretKey(PrivateKey privateKey, byte[] encryptedSecretKey) throws NetworkEncryptionException {
        byte[] bs = decrypt(privateKey, encryptedSecretKey);

        try {
            return new SecretKeySpec(bs, "AES");
        } catch (Exception e) {
            throw new NetworkEncryptionException(e);
        }
    }

    public static byte[] encrypt(Key key, byte[] data) throws NetworkEncryptionException {
        return crypt(Cipher.ENCRYPT_MODE, key, data);
    }

    public static byte[] decrypt(Key key, byte[] data) throws NetworkEncryptionException {
        return crypt(Cipher.DECRYPT_MODE, key, data);
    }

    private static byte[] crypt(int opMode, Key key, byte[] data) throws NetworkEncryptionException {
        try {
            return crypt(opMode, key.getAlgorithm(), key).doFinal(data);
        } catch (Exception e) {
            throw new NetworkEncryptionException(e);
        }
    }

    private static Cipher crypt(int opMode, String algorithm, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(opMode, key);
        return cipher;
    }
}
