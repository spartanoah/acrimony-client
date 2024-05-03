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

public class EncryptionUtil {
    private static SecretKeySpec keySpec;
    private static byte[] key;

    public static void genKeyAES(String input) throws NoSuchAlgorithmException {
        MessageDigest sha = null;
        key = input.getBytes(StandardCharsets.UTF_8);
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        keySpec = new SecretKeySpec(key, "AES");
    }

    public static String encryptAES(String input, String key) {
        try {
            EncryptionUtil.genKeyAES(key);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            return null;
        }
    }

    public static String decryptAES(String input, String key) {
        try {
            EncryptionUtil.genKeyAES(key);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, keySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(input)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            return null;
        }
    }

    public static String hashMD5(String input) {
        try {
            byte[] byteData;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte aByteData : byteData = md.digest()) {
                String hex = Integer.toHexString(0xFF & aByteData);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}

