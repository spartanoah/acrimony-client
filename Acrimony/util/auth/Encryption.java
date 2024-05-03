/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private SecretKeySpec keySpec;
    private byte[] AESKey;
    private Cipher encryptionCipher;
    private Cipher decryptionCipher;
    private String key;

    public Encryption(String key) {
        this.key = key;
        try {
            this.a();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void a() throws NoSuchAlgorithmException {
        MessageDigest sha = null;
        this.AESKey = this.key.getBytes(StandardCharsets.UTF_8);
        sha = MessageDigest.getInstance("SHA-1");
        this.AESKey = sha.digest(this.AESKey);
        this.AESKey = Arrays.copyOf(this.AESKey, 16);
        this.keySpec = new SecretKeySpec(this.AESKey, "AES");
        try {
            this.encryptionCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            this.encryptionCipher.init(1, this.keySpec);
            this.decryptionCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            this.decryptionCipher.init(2, this.keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String encryptAES(String input) {
        try {
            return Base64.getEncoder().encodeToString(this.encryptionCipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            return null;
        }
    }

    public String decryptAES(String input) {
        try {
            return new String(this.decryptionCipher.doFinal(Base64.getDecoder().decode(input)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            return null;
        }
    }

    public String getKey() {
        return this.key;
    }
}

