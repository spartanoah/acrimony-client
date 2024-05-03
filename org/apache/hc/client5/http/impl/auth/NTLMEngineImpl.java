/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.impl.auth.NTLMEngine;
import org.apache.hc.client5.http.impl.auth.NTLMEngineException;
import org.apache.hc.client5.http.utils.ByteArrayBuilder;

final class NTLMEngineImpl
implements NTLMEngine {
    private static final Charset UNICODE_LITTLE_UNMARKED = Charset.forName("UnicodeLittleUnmarked");
    private static final Charset DEFAULT_CHARSET = StandardCharsets.US_ASCII;
    static final int FLAG_REQUEST_UNICODE_ENCODING = 1;
    static final int FLAG_REQUEST_OEM_ENCODING = 2;
    static final int FLAG_REQUEST_TARGET = 4;
    static final int FLAG_REQUEST_SIGN = 16;
    static final int FLAG_REQUEST_SEAL = 32;
    static final int FLAG_REQUEST_LAN_MANAGER_KEY = 128;
    static final int FLAG_REQUEST_NTLMv1 = 512;
    static final int FLAG_DOMAIN_PRESENT = 4096;
    static final int FLAG_WORKSTATION_PRESENT = 8192;
    static final int FLAG_REQUEST_ALWAYS_SIGN = 32768;
    static final int FLAG_REQUEST_NTLM2_SESSION = 524288;
    static final int FLAG_REQUEST_VERSION = 0x2000000;
    static final int FLAG_TARGETINFO_PRESENT = 0x800000;
    static final int FLAG_REQUEST_128BIT_KEY_EXCH = 0x20000000;
    static final int FLAG_REQUEST_EXPLICIT_KEY_EXCH = 0x40000000;
    static final int FLAG_REQUEST_56BIT_ENCRYPTION = Integer.MIN_VALUE;
    static final int MSV_AV_EOL = 0;
    static final int MSV_AV_NB_COMPUTER_NAME = 1;
    static final int MSV_AV_NB_DOMAIN_NAME = 2;
    static final int MSV_AV_DNS_COMPUTER_NAME = 3;
    static final int MSV_AV_DNS_DOMAIN_NAME = 4;
    static final int MSV_AV_DNS_TREE_NAME = 5;
    static final int MSV_AV_FLAGS = 6;
    static final int MSV_AV_TIMESTAMP = 7;
    static final int MSV_AV_SINGLE_HOST = 8;
    static final int MSV_AV_TARGET_NAME = 9;
    static final int MSV_AV_CHANNEL_BINDINGS = 10;
    static final int MSV_AV_FLAGS_ACCOUNT_AUTH_CONSTAINED = 1;
    static final int MSV_AV_FLAGS_MIC = 2;
    static final int MSV_AV_FLAGS_UNTRUSTED_TARGET_SPN = 4;
    private static final SecureRandom RND_GEN;
    private static final byte[] SIGNATURE;
    private static final byte[] SIGN_MAGIC_SERVER;
    private static final byte[] SIGN_MAGIC_CLIENT;
    private static final byte[] SEAL_MAGIC_SERVER;
    private static final byte[] SEAL_MAGIC_CLIENT;
    private static final byte[] MAGIC_TLS_SERVER_ENDPOINT;
    private static final String TYPE_1_MESSAGE;

    private static byte[] getNullTerminatedAsciiString(String source) {
        byte[] bytesWithoutNull = source.getBytes(StandardCharsets.US_ASCII);
        byte[] target = new byte[bytesWithoutNull.length + 1];
        System.arraycopy(bytesWithoutNull, 0, target, 0, bytesWithoutNull.length);
        target[bytesWithoutNull.length] = 0;
        return target;
    }

    NTLMEngineImpl() {
    }

    static String getResponseFor(String message, String username, char[] password, String host, String domain) throws NTLMEngineException {
        String response;
        if (message == null || message.trim().equals("")) {
            response = NTLMEngineImpl.getType1Message(host, domain);
        } else {
            Type2Message t2m = new Type2Message(message);
            response = NTLMEngineImpl.getType3Message(username, password, host, domain, t2m.getChallenge(), t2m.getFlags(), t2m.getTarget(), t2m.getTargetInfo());
        }
        return response;
    }

    static String getResponseFor(String message, String username, char[] password, String host, String domain, Certificate peerServerCertificate) throws NTLMEngineException {
        String response;
        if (message == null || message.trim().equals("")) {
            response = new Type1Message(host, domain).getResponse();
        } else {
            Type1Message t1m = new Type1Message(host, domain);
            Type2Message t2m = new Type2Message(message);
            response = NTLMEngineImpl.getType3Message(username, password, host, domain, t2m.getChallenge(), t2m.getFlags(), t2m.getTarget(), t2m.getTargetInfo(), peerServerCertificate, t1m.getBytes(), t2m.getBytes());
        }
        return response;
    }

    static String getType1Message(String host, String domain) {
        return TYPE_1_MESSAGE;
    }

    static String getType3Message(String user, char[] password, String host, String domain, byte[] nonce, int type2Flags, String target, byte[] targetInformation) throws NTLMEngineException {
        return new Type3Message(domain, host, user, password, nonce, type2Flags, target, targetInformation).getResponse();
    }

    static String getType3Message(String user, char[] password, String host, String domain, byte[] nonce, int type2Flags, String target, byte[] targetInformation, Certificate peerServerCertificate, byte[] type1Message, byte[] type2Message) throws NTLMEngineException {
        return new Type3Message(domain, host, user, password, nonce, type2Flags, target, targetInformation, peerServerCertificate, type1Message, type2Message).getResponse();
    }

    private static int readULong(byte[] src, int index) {
        if (src.length < index + 4) {
            return 0;
        }
        return src[index] & 0xFF | (src[index + 1] & 0xFF) << 8 | (src[index + 2] & 0xFF) << 16 | (src[index + 3] & 0xFF) << 24;
    }

    private static int readUShort(byte[] src, int index) {
        if (src.length < index + 2) {
            return 0;
        }
        return src[index] & 0xFF | (src[index + 1] & 0xFF) << 8;
    }

    private static byte[] readSecurityBuffer(byte[] src, int index) {
        int length = NTLMEngineImpl.readUShort(src, index);
        int offset = NTLMEngineImpl.readULong(src, index + 4);
        if (src.length < offset + length) {
            return new byte[length];
        }
        byte[] buffer = new byte[length];
        System.arraycopy(src, offset, buffer, 0, length);
        return buffer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static byte[] makeRandomChallenge(Random random) {
        byte[] rval = new byte[8];
        Random random2 = random;
        synchronized (random2) {
            random.nextBytes(rval);
        }
        return rval;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static byte[] makeSecondaryKey(Random random) {
        byte[] rval = new byte[16];
        Random random2 = random;
        synchronized (random2) {
            random.nextBytes(rval);
        }
        return rval;
    }

    static byte[] hmacMD5(byte[] value, byte[] key) {
        HMACMD5 hmacMD5 = new HMACMD5(key);
        hmacMD5.update(value);
        return hmacMD5.getOutput();
    }

    static byte[] RC4(byte[] value, byte[] key) throws NTLMEngineException {
        try {
            Cipher rc4 = Cipher.getInstance("RC4");
            rc4.init(1, new SecretKeySpec(key, "RC4"));
            return rc4.doFinal(value);
        } catch (Exception e) {
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    static byte[] ntlm2SessionResponse(byte[] ntlmHash, byte[] challenge, byte[] clientChallenge) throws NTLMEngineException {
        try {
            MessageDigest md5 = NTLMEngineImpl.getMD5();
            md5.update(challenge);
            md5.update(clientChallenge);
            byte[] digest = md5.digest();
            byte[] sessionHash = new byte[8];
            System.arraycopy(digest, 0, sessionHash, 0, 8);
            return NTLMEngineImpl.lmResponse(ntlmHash, sessionHash);
        } catch (Exception e) {
            if (e instanceof NTLMEngineException) {
                throw (NTLMEngineException)e;
            }
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    private static byte[] lmHash(char[] password) throws NTLMEngineException {
        try {
            char[] tmp = new char[password.length];
            for (int i = 0; i < password.length; ++i) {
                tmp[i] = Character.toUpperCase(password[i]);
            }
            byte[] oemPassword = new ByteArrayBuilder().append(tmp).toByteArray();
            int length = Math.min(oemPassword.length, 14);
            byte[] keyBytes = new byte[14];
            System.arraycopy(oemPassword, 0, keyBytes, 0, length);
            Key lowKey = NTLMEngineImpl.createDESKey(keyBytes, 0);
            Key highKey = NTLMEngineImpl.createDESKey(keyBytes, 7);
            byte[] magicConstant = "KGS!@#$%".getBytes(StandardCharsets.US_ASCII);
            Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
            des.init(1, lowKey);
            byte[] lowHash = des.doFinal(magicConstant);
            des.init(1, highKey);
            byte[] highHash = des.doFinal(magicConstant);
            byte[] lmHash = new byte[16];
            System.arraycopy(lowHash, 0, lmHash, 0, 8);
            System.arraycopy(highHash, 0, lmHash, 8, 8);
            return lmHash;
        } catch (Exception e) {
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    private static byte[] ntlmHash(char[] password) throws NTLMEngineException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        byte[] unicodePassword = new ByteArrayBuilder().charset(UNICODE_LITTLE_UNMARKED).append(password).toByteArray();
        MD4 md4 = new MD4();
        md4.update(unicodePassword);
        return md4.getOutput();
    }

    private static byte[] lmv2Hash(String domain, String user, byte[] ntlmHash) throws NTLMEngineException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        HMACMD5 hmacMD5 = new HMACMD5(ntlmHash);
        hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        if (domain != null) {
            hmacMD5.update(domain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        }
        return hmacMD5.getOutput();
    }

    private static byte[] ntlmv2Hash(String domain, String user, byte[] ntlmHash) throws NTLMEngineException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        HMACMD5 hmacMD5 = new HMACMD5(ntlmHash);
        hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        if (domain != null) {
            hmacMD5.update(domain.getBytes(UNICODE_LITTLE_UNMARKED));
        }
        return hmacMD5.getOutput();
    }

    private static byte[] lmResponse(byte[] hash, byte[] challenge) throws NTLMEngineException {
        try {
            byte[] keyBytes = new byte[21];
            System.arraycopy(hash, 0, keyBytes, 0, 16);
            Key lowKey = NTLMEngineImpl.createDESKey(keyBytes, 0);
            Key middleKey = NTLMEngineImpl.createDESKey(keyBytes, 7);
            Key highKey = NTLMEngineImpl.createDESKey(keyBytes, 14);
            Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
            des.init(1, lowKey);
            byte[] lowResponse = des.doFinal(challenge);
            des.init(1, middleKey);
            byte[] middleResponse = des.doFinal(challenge);
            des.init(1, highKey);
            byte[] highResponse = des.doFinal(challenge);
            byte[] lmResponse = new byte[24];
            System.arraycopy(lowResponse, 0, lmResponse, 0, 8);
            System.arraycopy(middleResponse, 0, lmResponse, 8, 8);
            System.arraycopy(highResponse, 0, lmResponse, 16, 8);
            return lmResponse;
        } catch (Exception e) {
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    private static byte[] lmv2Response(byte[] hash, byte[] challenge, byte[] clientData) {
        HMACMD5 hmacMD5 = new HMACMD5(hash);
        hmacMD5.update(challenge);
        hmacMD5.update(clientData);
        byte[] mac = hmacMD5.getOutput();
        byte[] lmv2Response = new byte[mac.length + clientData.length];
        System.arraycopy(mac, 0, lmv2Response, 0, mac.length);
        System.arraycopy(clientData, 0, lmv2Response, mac.length, clientData.length);
        return lmv2Response;
    }

    private static byte[] encodeLong(int value) {
        byte[] enc = new byte[4];
        NTLMEngineImpl.encodeLong(enc, 0, value);
        return enc;
    }

    private static void encodeLong(byte[] buf, int offset, int value) {
        buf[offset + 0] = (byte)(value & 0xFF);
        buf[offset + 1] = (byte)(value >> 8 & 0xFF);
        buf[offset + 2] = (byte)(value >> 16 & 0xFF);
        buf[offset + 3] = (byte)(value >> 24 & 0xFF);
    }

    private static byte[] createBlob(byte[] clientChallenge, byte[] targetInformation, byte[] timestamp) {
        byte[] blobSignature = new byte[]{1, 1, 0, 0};
        byte[] reserved = new byte[]{0, 0, 0, 0};
        byte[] unknown1 = new byte[]{0, 0, 0, 0};
        byte[] unknown2 = new byte[]{0, 0, 0, 0};
        byte[] blob = new byte[blobSignature.length + reserved.length + timestamp.length + 8 + unknown1.length + targetInformation.length + unknown2.length];
        int offset = 0;
        System.arraycopy(blobSignature, 0, blob, offset, blobSignature.length);
        System.arraycopy(reserved, 0, blob, offset += blobSignature.length, reserved.length);
        System.arraycopy(timestamp, 0, blob, offset += reserved.length, timestamp.length);
        System.arraycopy(clientChallenge, 0, blob, offset += timestamp.length, 8);
        System.arraycopy(unknown1, 0, blob, offset += 8, unknown1.length);
        System.arraycopy(targetInformation, 0, blob, offset += unknown1.length, targetInformation.length);
        System.arraycopy(unknown2, 0, blob, offset += targetInformation.length, unknown2.length);
        offset += unknown2.length;
        return blob;
    }

    private static Key createDESKey(byte[] bytes, int offset) {
        byte[] keyBytes = new byte[7];
        System.arraycopy(bytes, offset, keyBytes, 0, 7);
        byte[] material = new byte[]{keyBytes[0], (byte)(keyBytes[0] << 7 | (keyBytes[1] & 0xFF) >>> 1), (byte)(keyBytes[1] << 6 | (keyBytes[2] & 0xFF) >>> 2), (byte)(keyBytes[2] << 5 | (keyBytes[3] & 0xFF) >>> 3), (byte)(keyBytes[3] << 4 | (keyBytes[4] & 0xFF) >>> 4), (byte)(keyBytes[4] << 3 | (keyBytes[5] & 0xFF) >>> 5), (byte)(keyBytes[5] << 2 | (keyBytes[6] & 0xFF) >>> 6), (byte)(keyBytes[6] << 1)};
        NTLMEngineImpl.oddParity(material);
        return new SecretKeySpec(material, "DES");
    }

    private static void oddParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            boolean needsParity;
            byte b = bytes[i];
            boolean bl = needsParity = ((b >>> 7 ^ b >>> 6 ^ b >>> 5 ^ b >>> 4 ^ b >>> 3 ^ b >>> 2 ^ b >>> 1) & 1) == 0;
            if (needsParity) {
                int n = i;
                bytes[n] = (byte)(bytes[n] | 1);
                continue;
            }
            int n = i;
            bytes[n] = (byte)(bytes[n] & 0xFFFFFFFE);
        }
    }

    private static Charset getCharset(int flags) throws NTLMEngineException {
        if ((flags & 1) == 0) {
            return DEFAULT_CHARSET;
        }
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        return UNICODE_LITTLE_UNMARKED;
    }

    static void writeUShort(byte[] buffer, int value, int offset) {
        buffer[offset] = (byte)(value & 0xFF);
        buffer[offset + 1] = (byte)(value >> 8 & 0xFF);
    }

    static void writeULong(byte[] buffer, int value, int offset) {
        buffer[offset] = (byte)(value & 0xFF);
        buffer[offset + 1] = (byte)(value >> 8 & 0xFF);
        buffer[offset + 2] = (byte)(value >> 16 & 0xFF);
        buffer[offset + 3] = (byte)(value >> 24 & 0xFF);
    }

    static int F(int x, int y, int z) {
        return x & y | ~x & z;
    }

    static int G(int x, int y, int z) {
        return x & y | x & z | y & z;
    }

    static int H(int x, int y, int z) {
        return x ^ y ^ z;
    }

    static int rotintlft(int val2, int numbits) {
        return val2 << numbits | val2 >>> 32 - numbits;
    }

    static MessageDigest getMD5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("MD5 message digest doesn't seem to exist - fatal error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String generateType1Msg(String domain, String workstation) throws NTLMEngineException {
        return NTLMEngineImpl.getType1Message(workstation, domain);
    }

    @Override
    public String generateType3Msg(String username, char[] password, String domain, String workstation, String challenge) throws NTLMEngineException {
        Type2Message t2m = new Type2Message(challenge);
        return NTLMEngineImpl.getType3Message(username, password, workstation, domain, t2m.getChallenge(), t2m.getFlags(), t2m.getTarget(), t2m.getTargetInfo());
    }

    static {
        SecureRandom rnd = null;
        try {
            rnd = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception exception) {
            // empty catch block
        }
        RND_GEN = rnd;
        SIGNATURE = NTLMEngineImpl.getNullTerminatedAsciiString("NTLMSSP");
        SIGN_MAGIC_SERVER = NTLMEngineImpl.getNullTerminatedAsciiString("session key to server-to-client signing key magic constant");
        SIGN_MAGIC_CLIENT = NTLMEngineImpl.getNullTerminatedAsciiString("session key to client-to-server signing key magic constant");
        SEAL_MAGIC_SERVER = NTLMEngineImpl.getNullTerminatedAsciiString("session key to server-to-client sealing key magic constant");
        SEAL_MAGIC_CLIENT = NTLMEngineImpl.getNullTerminatedAsciiString("session key to client-to-server sealing key magic constant");
        MAGIC_TLS_SERVER_ENDPOINT = "tls-server-end-point:".getBytes(StandardCharsets.US_ASCII);
        TYPE_1_MESSAGE = new Type1Message().getResponse();
    }

    static class HMACMD5 {
        final byte[] ipad;
        final byte[] opad;
        final MessageDigest md5;

        HMACMD5(byte[] input) {
            int i;
            byte[] key = input;
            this.md5 = NTLMEngineImpl.getMD5();
            this.ipad = new byte[64];
            this.opad = new byte[64];
            int keyLength = key.length;
            if (keyLength > 64) {
                this.md5.update(key);
                key = this.md5.digest();
                keyLength = key.length;
            }
            for (i = 0; i < keyLength; ++i) {
                this.ipad[i] = (byte)(key[i] ^ 0x36);
                this.opad[i] = (byte)(key[i] ^ 0x5C);
            }
            while (i < 64) {
                this.ipad[i] = 54;
                this.opad[i] = 92;
                ++i;
            }
            this.md5.reset();
            this.md5.update(this.ipad);
        }

        byte[] getOutput() {
            byte[] digest = this.md5.digest();
            this.md5.update(this.opad);
            return this.md5.digest(digest);
        }

        void update(byte[] input) {
            this.md5.update(input);
        }

        void update(byte[] input, int offset, int length) {
            this.md5.update(input, offset, length);
        }
    }

    static class MD4 {
        int A = 1732584193;
        int B = -271733879;
        int C = -1732584194;
        int D = 271733878;
        long count = 0L;
        final byte[] dataBuffer = new byte[64];

        MD4() {
        }

        void update(byte[] input) {
            int transferAmt;
            int curBufferPos = (int)(this.count & 0x3FL);
            int inputIndex = 0;
            while (input.length - inputIndex + curBufferPos >= this.dataBuffer.length) {
                transferAmt = this.dataBuffer.length - curBufferPos;
                System.arraycopy(input, inputIndex, this.dataBuffer, curBufferPos, transferAmt);
                this.count += (long)transferAmt;
                curBufferPos = 0;
                inputIndex += transferAmt;
                this.processBuffer();
            }
            if (inputIndex < input.length) {
                transferAmt = input.length - inputIndex;
                System.arraycopy(input, inputIndex, this.dataBuffer, curBufferPos, transferAmt);
                this.count += (long)transferAmt;
                curBufferPos += transferAmt;
            }
        }

        byte[] getOutput() {
            int bufferIndex = (int)(this.count & 0x3FL);
            int padLen = bufferIndex < 56 ? 56 - bufferIndex : 120 - bufferIndex;
            byte[] postBytes = new byte[padLen + 8];
            postBytes[0] = -128;
            for (int i = 0; i < 8; ++i) {
                postBytes[padLen + i] = (byte)(this.count * 8L >>> 8 * i);
            }
            this.update(postBytes);
            byte[] result = new byte[16];
            NTLMEngineImpl.writeULong(result, this.A, 0);
            NTLMEngineImpl.writeULong(result, this.B, 4);
            NTLMEngineImpl.writeULong(result, this.C, 8);
            NTLMEngineImpl.writeULong(result, this.D, 12);
            return result;
        }

        void processBuffer() {
            int[] d = new int[16];
            for (int i = 0; i < 16; ++i) {
                d[i] = (this.dataBuffer[i * 4] & 0xFF) + ((this.dataBuffer[i * 4 + 1] & 0xFF) << 8) + ((this.dataBuffer[i * 4 + 2] & 0xFF) << 16) + ((this.dataBuffer[i * 4 + 3] & 0xFF) << 24);
            }
            int AA = this.A;
            int BB = this.B;
            int CC = this.C;
            int DD = this.D;
            this.round1(d);
            this.round2(d);
            this.round3(d);
            this.A += AA;
            this.B += BB;
            this.C += CC;
            this.D += DD;
        }

        void round1(int[] d) {
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[0], 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[1], 7);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[2], 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[3], 19);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[4], 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[5], 7);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[6], 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[7], 19);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[8], 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[9], 7);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[10], 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[11], 19);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[12], 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[13], 7);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[14], 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[15], 19);
        }

        void round2(int[] d) {
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[0] + 1518500249, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[4] + 1518500249, 5);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[8] + 1518500249, 9);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[12] + 1518500249, 13);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[1] + 1518500249, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[5] + 1518500249, 5);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[9] + 1518500249, 9);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[13] + 1518500249, 13);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[2] + 1518500249, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[6] + 1518500249, 5);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[10] + 1518500249, 9);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[14] + 1518500249, 13);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[3] + 1518500249, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[7] + 1518500249, 5);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[11] + 1518500249, 9);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[15] + 1518500249, 13);
        }

        void round3(int[] d) {
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[0] + 1859775393, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[8] + 1859775393, 9);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[4] + 1859775393, 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[12] + 1859775393, 15);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[2] + 1859775393, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[10] + 1859775393, 9);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[6] + 1859775393, 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[14] + 1859775393, 15);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[1] + 1859775393, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[9] + 1859775393, 9);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[5] + 1859775393, 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[13] + 1859775393, 15);
            this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[3] + 1859775393, 3);
            this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[11] + 1859775393, 9);
            this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[7] + 1859775393, 11);
            this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[15] + 1859775393, 15);
        }
    }

    static class Type3Message
    extends NTLMMessage {
        final byte[] type1Message;
        final byte[] type2Message;
        final int type2Flags;
        final byte[] domainBytes;
        final byte[] hostBytes;
        final byte[] userBytes;
        byte[] lmResp;
        byte[] ntResp;
        final byte[] sessionKey;
        final byte[] exportedSessionKey;
        final boolean computeMic;

        Type3Message(String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation) throws NTLMEngineException {
            this(domain, host, user, password, nonce, type2Flags, target, targetInformation, null, null, null);
        }

        Type3Message(Random random, long currentTime, String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation) throws NTLMEngineException {
            this(random, currentTime, domain, host, user, password, nonce, type2Flags, target, targetInformation, null, null, null);
        }

        Type3Message(String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation, Certificate peerServerCertificate, byte[] type1Message, byte[] type2Message) throws NTLMEngineException {
            this(RND_GEN, System.currentTimeMillis(), domain, host, user, password, nonce, type2Flags, target, targetInformation, peerServerCertificate, type1Message, type2Message);
        }

        Type3Message(Random random, long currentTime, String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation, Certificate peerServerCertificate, byte[] type1Message, byte[] type2Message) throws NTLMEngineException {
            byte[] userSessionKey;
            if (random == null) {
                throw new NTLMEngineException("Random generator not available");
            }
            this.type2Flags = type2Flags;
            this.type1Message = type1Message;
            this.type2Message = type2Message;
            String unqualifiedHost = host;
            String unqualifiedDomain = domain;
            byte[] responseTargetInformation = targetInformation;
            if (peerServerCertificate != null) {
                responseTargetInformation = this.addGssMicAvsToTargetInfo(targetInformation, peerServerCertificate);
                this.computeMic = true;
            } else {
                this.computeMic = false;
            }
            CipherGen gen = new CipherGen(random, currentTime, unqualifiedDomain, user, password, nonce, target, responseTargetInformation);
            try {
                if ((type2Flags & 0x800000) != 0 && targetInformation != null && target != null) {
                    this.ntResp = gen.getNTLMv2Response();
                    this.lmResp = gen.getLMv2Response();
                    userSessionKey = (type2Flags & 0x80) != 0 ? gen.getLanManagerSessionKey() : gen.getNTLMv2UserSessionKey();
                } else if ((type2Flags & 0x80000) != 0) {
                    this.ntResp = gen.getNTLM2SessionResponse();
                    this.lmResp = gen.getLM2SessionResponse();
                    userSessionKey = (type2Flags & 0x80) != 0 ? gen.getLanManagerSessionKey() : gen.getNTLM2SessionResponseUserSessionKey();
                } else {
                    this.ntResp = gen.getNTLMResponse();
                    this.lmResp = gen.getLMResponse();
                    userSessionKey = (type2Flags & 0x80) != 0 ? gen.getLanManagerSessionKey() : gen.getNTLMUserSessionKey();
                }
            } catch (NTLMEngineException e) {
                this.ntResp = new byte[0];
                this.lmResp = gen.getLMResponse();
                userSessionKey = (type2Flags & 0x80) != 0 ? gen.getLanManagerSessionKey() : gen.getLMUserSessionKey();
            }
            if ((type2Flags & 0x10) != 0) {
                if ((type2Flags & 0x40000000) != 0) {
                    this.exportedSessionKey = gen.getSecondaryKey();
                    this.sessionKey = NTLMEngineImpl.RC4(this.exportedSessionKey, userSessionKey);
                } else {
                    this.sessionKey = userSessionKey;
                    this.exportedSessionKey = this.sessionKey;
                }
            } else {
                if (this.computeMic) {
                    throw new NTLMEngineException("Cannot sign/seal: no exported session key");
                }
                this.sessionKey = null;
                this.exportedSessionKey = null;
            }
            Charset charset = NTLMEngineImpl.getCharset(type2Flags);
            this.hostBytes = unqualifiedHost != null ? unqualifiedHost.getBytes(charset) : null;
            this.domainBytes = unqualifiedDomain != null ? unqualifiedDomain.toUpperCase(Locale.ROOT).getBytes(charset) : null;
            this.userBytes = user.getBytes(charset);
        }

        public byte[] getEncryptedRandomSessionKey() {
            return this.sessionKey;
        }

        public byte[] getExportedSessionKey() {
            return this.exportedSessionKey;
        }

        @Override
        void buildMessage() {
            int ntRespLen = this.ntResp.length;
            int lmRespLen = this.lmResp.length;
            int domainLen = this.domainBytes != null ? this.domainBytes.length : 0;
            int hostLen = this.hostBytes != null ? this.hostBytes.length : 0;
            int userLen = this.userBytes.length;
            int sessionKeyLen = this.sessionKey != null ? this.sessionKey.length : 0;
            int lmRespOffset = 72 + (this.computeMic ? 16 : 0);
            int ntRespOffset = lmRespOffset + lmRespLen;
            int domainOffset = ntRespOffset + ntRespLen;
            int userOffset = domainOffset + domainLen;
            int hostOffset = userOffset + userLen;
            int sessionKeyOffset = hostOffset + hostLen;
            int finalLength = sessionKeyOffset + sessionKeyLen;
            this.prepareResponse(finalLength, 3);
            this.addUShort(lmRespLen);
            this.addUShort(lmRespLen);
            this.addULong(lmRespOffset);
            this.addUShort(ntRespLen);
            this.addUShort(ntRespLen);
            this.addULong(ntRespOffset);
            this.addUShort(domainLen);
            this.addUShort(domainLen);
            this.addULong(domainOffset);
            this.addUShort(userLen);
            this.addUShort(userLen);
            this.addULong(userOffset);
            this.addUShort(hostLen);
            this.addUShort(hostLen);
            this.addULong(hostOffset);
            this.addUShort(sessionKeyLen);
            this.addUShort(sessionKeyLen);
            this.addULong(sessionKeyOffset);
            this.addULong(this.type2Flags);
            this.addUShort(261);
            this.addULong(2600);
            this.addUShort(3840);
            int micPosition = -1;
            if (this.computeMic) {
                micPosition = this.currentOutputPosition;
                this.currentOutputPosition += 16;
            }
            this.addBytes(this.lmResp);
            this.addBytes(this.ntResp);
            this.addBytes(this.domainBytes);
            this.addBytes(this.userBytes);
            this.addBytes(this.hostBytes);
            if (this.sessionKey != null) {
                this.addBytes(this.sessionKey);
            }
            if (this.computeMic) {
                HMACMD5 hmacMD5 = new HMACMD5(this.exportedSessionKey);
                hmacMD5.update(this.type1Message);
                hmacMD5.update(this.type2Message);
                hmacMD5.update(this.messageContents);
                byte[] mic = hmacMD5.getOutput();
                System.arraycopy(mic, 0, this.messageContents, micPosition, mic.length);
            }
        }

        private byte[] addGssMicAvsToTargetInfo(byte[] originalTargetInfo, Certificate peerServerCertificate) throws NTLMEngineException {
            byte[] channelBindingsHash;
            byte[] newTargetInfo = new byte[originalTargetInfo.length + 8 + 20];
            int appendLength = originalTargetInfo.length - 4;
            System.arraycopy(originalTargetInfo, 0, newTargetInfo, 0, appendLength);
            NTLMEngineImpl.writeUShort(newTargetInfo, 6, appendLength);
            NTLMEngineImpl.writeUShort(newTargetInfo, 4, appendLength + 2);
            NTLMEngineImpl.writeULong(newTargetInfo, 2, appendLength + 4);
            NTLMEngineImpl.writeUShort(newTargetInfo, 10, appendLength + 8);
            NTLMEngineImpl.writeUShort(newTargetInfo, 16, appendLength + 10);
            try {
                byte[] certBytes = peerServerCertificate.getEncoded();
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] certHashBytes = sha256.digest(certBytes);
                byte[] channelBindingStruct = new byte[20 + MAGIC_TLS_SERVER_ENDPOINT.length + certHashBytes.length];
                NTLMEngineImpl.writeULong(channelBindingStruct, 53, 16);
                System.arraycopy(MAGIC_TLS_SERVER_ENDPOINT, 0, channelBindingStruct, 20, MAGIC_TLS_SERVER_ENDPOINT.length);
                System.arraycopy(certHashBytes, 0, channelBindingStruct, 20 + MAGIC_TLS_SERVER_ENDPOINT.length, certHashBytes.length);
                MessageDigest md5 = NTLMEngineImpl.getMD5();
                channelBindingsHash = md5.digest(channelBindingStruct);
            } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
                throw new NTLMEngineException(e.getMessage(), e);
            }
            System.arraycopy(channelBindingsHash, 0, newTargetInfo, appendLength + 12, 16);
            return newTargetInfo;
        }
    }

    static class Type2Message
    extends NTLMMessage {
        final byte[] challenge = new byte[8];
        String target;
        byte[] targetInfo;
        final int flags;

        Type2Message(String messageBody) throws NTLMEngineException {
            this(Base64.decodeBase64(messageBody.getBytes(DEFAULT_CHARSET)));
        }

        Type2Message(byte[] message) throws NTLMEngineException {
            super(message, 2);
            byte[] bytes;
            this.readBytes(this.challenge, 24);
            this.flags = this.readULong(20);
            this.target = null;
            if (this.getMessageLength() >= 20 && (bytes = this.readSecurityBuffer(12)).length != 0) {
                this.target = new String(bytes, NTLMEngineImpl.getCharset(this.flags));
            }
            this.targetInfo = null;
            if (this.getMessageLength() >= 48 && (bytes = this.readSecurityBuffer(40)).length != 0) {
                this.targetInfo = bytes;
            }
        }

        byte[] getChallenge() {
            return this.challenge;
        }

        String getTarget() {
            return this.target;
        }

        byte[] getTargetInfo() {
            return this.targetInfo;
        }

        int getFlags() {
            return this.flags;
        }
    }

    static class Type1Message
    extends NTLMMessage {
        private final byte[] hostBytes;
        private final byte[] domainBytes;
        private final int flags;

        Type1Message(String domain, String host) {
            this(domain, host, null);
        }

        Type1Message(String domain, String host, Integer flags) {
            this.flags = flags == null ? this.getDefaultFlags() : flags.intValue();
            String unqualifiedHost = host;
            String unqualifiedDomain = domain;
            this.hostBytes = unqualifiedHost != null ? unqualifiedHost.getBytes(UNICODE_LITTLE_UNMARKED) : null;
            this.domainBytes = unqualifiedDomain != null ? unqualifiedDomain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED) : null;
        }

        Type1Message() {
            this.hostBytes = null;
            this.domainBytes = null;
            this.flags = this.getDefaultFlags();
        }

        private int getDefaultFlags() {
            return -1576500735;
        }

        @Override
        void buildMessage() {
            int domainBytesLength = 0;
            if (this.domainBytes != null) {
                domainBytesLength = this.domainBytes.length;
            }
            int hostBytesLength = 0;
            if (this.hostBytes != null) {
                hostBytesLength = this.hostBytes.length;
            }
            int finalLength = 40 + hostBytesLength + domainBytesLength;
            this.prepareResponse(finalLength, 1);
            this.addULong(this.flags);
            this.addUShort(domainBytesLength);
            this.addUShort(domainBytesLength);
            this.addULong(hostBytesLength + 32 + 8);
            this.addUShort(hostBytesLength);
            this.addUShort(hostBytesLength);
            this.addULong(40);
            this.addUShort(261);
            this.addULong(2600);
            this.addUShort(3840);
            if (this.hostBytes != null) {
                this.addBytes(this.hostBytes);
            }
            if (this.domainBytes != null) {
                this.addBytes(this.domainBytes);
            }
        }
    }

    static class NTLMMessage {
        byte[] messageContents = null;
        int currentOutputPosition = 0;

        NTLMMessage() {
        }

        NTLMMessage(String messageBody, int expectedType) throws NTLMEngineException {
            this(Base64.decodeBase64(messageBody.getBytes(DEFAULT_CHARSET)), expectedType);
        }

        NTLMMessage(byte[] message, int expectedType) throws NTLMEngineException {
            this.messageContents = message;
            if (this.messageContents.length < SIGNATURE.length) {
                throw new NTLMEngineException("NTLM message decoding error - packet too short");
            }
            for (int i = 0; i < SIGNATURE.length; ++i) {
                if (this.messageContents[i] == SIGNATURE[i]) continue;
                throw new NTLMEngineException("NTLM message expected - instead got unrecognized bytes");
            }
            int type = this.readULong(SIGNATURE.length);
            if (type != expectedType) {
                throw new NTLMEngineException("NTLM type " + Integer.toString(expectedType) + " message expected - instead got type " + Integer.toString(type));
            }
            this.currentOutputPosition = this.messageContents.length;
        }

        int getPreambleLength() {
            return SIGNATURE.length + 4;
        }

        int getMessageLength() {
            return this.currentOutputPosition;
        }

        byte readByte(int position) throws NTLMEngineException {
            if (this.messageContents.length < position + 1) {
                throw new NTLMEngineException("NTLM: Message too short");
            }
            return this.messageContents[position];
        }

        void readBytes(byte[] buffer, int position) throws NTLMEngineException {
            if (this.messageContents.length < position + buffer.length) {
                throw new NTLMEngineException("NTLM: Message too short");
            }
            System.arraycopy(this.messageContents, position, buffer, 0, buffer.length);
        }

        int readUShort(int position) {
            return NTLMEngineImpl.readUShort(this.messageContents, position);
        }

        int readULong(int position) {
            return NTLMEngineImpl.readULong(this.messageContents, position);
        }

        byte[] readSecurityBuffer(int position) {
            return NTLMEngineImpl.readSecurityBuffer(this.messageContents, position);
        }

        void prepareResponse(int maxlength, int messageType) {
            this.messageContents = new byte[maxlength];
            this.currentOutputPosition = 0;
            this.addBytes(SIGNATURE);
            this.addULong(messageType);
        }

        void addByte(byte b) {
            this.messageContents[this.currentOutputPosition] = b;
            ++this.currentOutputPosition;
        }

        void addBytes(byte[] bytes) {
            if (bytes == null) {
                return;
            }
            byte[] arr$ = bytes;
            int len$ = arr$.length;
            for (int i$ = 0; i$ < len$; ++i$) {
                byte b;
                this.messageContents[this.currentOutputPosition] = b = arr$[i$];
                ++this.currentOutputPosition;
            }
        }

        void addUShort(int value) {
            this.addByte((byte)(value & 0xFF));
            this.addByte((byte)(value >> 8 & 0xFF));
        }

        void addULong(int value) {
            this.addByte((byte)(value & 0xFF));
            this.addByte((byte)(value >> 8 & 0xFF));
            this.addByte((byte)(value >> 16 & 0xFF));
            this.addByte((byte)(value >> 24 & 0xFF));
        }

        public String getResponse() {
            return new String(Base64.encodeBase64(this.getBytes()), StandardCharsets.US_ASCII);
        }

        public byte[] getBytes() {
            if (this.messageContents == null) {
                this.buildMessage();
            }
            if (this.messageContents.length > this.currentOutputPosition) {
                byte[] tmp = new byte[this.currentOutputPosition];
                System.arraycopy(this.messageContents, 0, tmp, 0, this.currentOutputPosition);
                this.messageContents = tmp;
            }
            return this.messageContents;
        }

        void buildMessage() {
            throw new RuntimeException("Message builder not implemented for " + this.getClass().getName());
        }
    }

    static class Handle {
        private final byte[] signingKey;
        private byte[] sealingKey;
        private final Cipher rc4;
        final Mode mode;
        private final boolean isConnection;
        int sequenceNumber = 0;

        Handle(byte[] exportedSessionKey, Mode mode, boolean isConnection) throws NTLMEngineException {
            this.isConnection = isConnection;
            this.mode = mode;
            try {
                MessageDigest signMd5 = NTLMEngineImpl.getMD5();
                MessageDigest sealMd5 = NTLMEngineImpl.getMD5();
                signMd5.update(exportedSessionKey);
                sealMd5.update(exportedSessionKey);
                if (mode == Mode.CLIENT) {
                    signMd5.update(SIGN_MAGIC_CLIENT);
                    sealMd5.update(SEAL_MAGIC_CLIENT);
                } else {
                    signMd5.update(SIGN_MAGIC_SERVER);
                    sealMd5.update(SEAL_MAGIC_SERVER);
                }
                this.signingKey = signMd5.digest();
                this.sealingKey = sealMd5.digest();
            } catch (Exception e) {
                throw new NTLMEngineException(e.getMessage(), e);
            }
            this.rc4 = this.initCipher();
        }

        public byte[] getSigningKey() {
            return this.signingKey;
        }

        public byte[] getSealingKey() {
            return this.sealingKey;
        }

        private Cipher initCipher() throws NTLMEngineException {
            Cipher cipher;
            try {
                cipher = Cipher.getInstance("RC4");
                if (this.mode == Mode.CLIENT) {
                    cipher.init(1, new SecretKeySpec(this.sealingKey, "RC4"));
                } else {
                    cipher.init(2, new SecretKeySpec(this.sealingKey, "RC4"));
                }
            } catch (Exception e) {
                throw new NTLMEngineException(e.getMessage(), e);
            }
            return cipher;
        }

        private void advanceMessageSequence() throws NTLMEngineException {
            if (!this.isConnection) {
                MessageDigest sealMd5 = NTLMEngineImpl.getMD5();
                sealMd5.update(this.sealingKey);
                byte[] seqNumBytes = new byte[4];
                NTLMEngineImpl.writeULong(seqNumBytes, this.sequenceNumber, 0);
                sealMd5.update(seqNumBytes);
                this.sealingKey = sealMd5.digest();
                this.initCipher();
            }
            ++this.sequenceNumber;
        }

        private byte[] encrypt(byte[] data) {
            return this.rc4.update(data);
        }

        private byte[] decrypt(byte[] data) {
            return this.rc4.update(data);
        }

        private byte[] computeSignature(byte[] message) {
            byte[] sig = new byte[16];
            sig[0] = 1;
            sig[1] = 0;
            sig[2] = 0;
            sig[3] = 0;
            HMACMD5 hmacMD5 = new HMACMD5(this.signingKey);
            hmacMD5.update(NTLMEngineImpl.encodeLong(this.sequenceNumber));
            hmacMD5.update(message);
            byte[] hmac = hmacMD5.getOutput();
            byte[] trimmedHmac = new byte[8];
            System.arraycopy(hmac, 0, trimmedHmac, 0, 8);
            byte[] encryptedHmac = this.encrypt(trimmedHmac);
            System.arraycopy(encryptedHmac, 0, sig, 4, 8);
            NTLMEngineImpl.encodeLong(sig, 12, this.sequenceNumber);
            return sig;
        }

        private boolean validateSignature(byte[] signature, byte[] message) {
            byte[] computedSignature = this.computeSignature(message);
            return Arrays.equals(signature, computedSignature);
        }

        public byte[] signAndEncryptMessage(byte[] cleartextMessage) throws NTLMEngineException {
            byte[] encryptedMessage = this.encrypt(cleartextMessage);
            byte[] signature = this.computeSignature(cleartextMessage);
            byte[] outMessage = new byte[signature.length + encryptedMessage.length];
            System.arraycopy(signature, 0, outMessage, 0, signature.length);
            System.arraycopy(encryptedMessage, 0, outMessage, signature.length, encryptedMessage.length);
            this.advanceMessageSequence();
            return outMessage;
        }

        public byte[] decryptAndVerifySignedMessage(byte[] inMessage) throws NTLMEngineException {
            byte[] signature = new byte[16];
            System.arraycopy(inMessage, 0, signature, 0, signature.length);
            byte[] encryptedMessage = new byte[inMessage.length - 16];
            System.arraycopy(inMessage, 16, encryptedMessage, 0, encryptedMessage.length);
            byte[] cleartextMessage = this.decrypt(encryptedMessage);
            if (!this.validateSignature(signature, cleartextMessage)) {
                throw new NTLMEngineException("Wrong signature");
            }
            this.advanceMessageSequence();
            return cleartextMessage;
        }
    }

    static enum Mode {
        CLIENT,
        SERVER;

    }

    static class CipherGen {
        final Random random;
        final long currentTime;
        final String domain;
        final String user;
        final char[] password;
        final byte[] challenge;
        final String target;
        final byte[] targetInformation;
        byte[] clientChallenge;
        byte[] clientChallenge2;
        byte[] secondaryKey;
        byte[] timestamp;
        byte[] lmHash = null;
        byte[] lmResponse = null;
        byte[] ntlmHash = null;
        byte[] ntlmResponse = null;
        byte[] ntlmv2Hash = null;
        byte[] lmv2Hash = null;
        byte[] lmv2Response = null;
        byte[] ntlmv2Blob = null;
        byte[] ntlmv2Response = null;
        byte[] ntlm2SessionResponse = null;
        byte[] lm2SessionResponse = null;
        byte[] lmUserSessionKey = null;
        byte[] ntlmUserSessionKey = null;
        byte[] ntlmv2UserSessionKey = null;
        byte[] ntlm2SessionResponseUserSessionKey = null;
        byte[] lanManagerSessionKey = null;

        public CipherGen(Random random, long currentTime, String domain, String user, char[] password, byte[] challenge, String target, byte[] targetInformation, byte[] clientChallenge, byte[] clientChallenge2, byte[] secondaryKey, byte[] timestamp) {
            this.random = random;
            this.currentTime = currentTime;
            this.domain = domain;
            this.target = target;
            this.user = user;
            this.password = password;
            this.challenge = challenge;
            this.targetInformation = targetInformation;
            this.clientChallenge = clientChallenge;
            this.clientChallenge2 = clientChallenge2;
            this.secondaryKey = secondaryKey;
            this.timestamp = timestamp;
        }

        public CipherGen(Random random, long currentTime, String domain, String user, char[] password, byte[] challenge, String target, byte[] targetInformation) {
            this(random, currentTime, domain, user, password, challenge, target, targetInformation, null, null, null, null);
        }

        public byte[] getClientChallenge() {
            if (this.clientChallenge == null) {
                this.clientChallenge = NTLMEngineImpl.makeRandomChallenge(this.random);
            }
            return this.clientChallenge;
        }

        public byte[] getClientChallenge2() {
            if (this.clientChallenge2 == null) {
                this.clientChallenge2 = NTLMEngineImpl.makeRandomChallenge(this.random);
            }
            return this.clientChallenge2;
        }

        public byte[] getSecondaryKey() {
            if (this.secondaryKey == null) {
                this.secondaryKey = NTLMEngineImpl.makeSecondaryKey(this.random);
            }
            return this.secondaryKey;
        }

        public byte[] getLMHash() throws NTLMEngineException {
            if (this.lmHash == null) {
                this.lmHash = NTLMEngineImpl.lmHash(this.password);
            }
            return this.lmHash;
        }

        public byte[] getLMResponse() throws NTLMEngineException {
            if (this.lmResponse == null) {
                this.lmResponse = NTLMEngineImpl.lmResponse(this.getLMHash(), this.challenge);
            }
            return this.lmResponse;
        }

        public byte[] getNTLMHash() throws NTLMEngineException {
            if (this.ntlmHash == null) {
                this.ntlmHash = NTLMEngineImpl.ntlmHash(this.password);
            }
            return this.ntlmHash;
        }

        public byte[] getNTLMResponse() throws NTLMEngineException {
            if (this.ntlmResponse == null) {
                this.ntlmResponse = NTLMEngineImpl.lmResponse(this.getNTLMHash(), this.challenge);
            }
            return this.ntlmResponse;
        }

        public byte[] getLMv2Hash() throws NTLMEngineException {
            if (this.lmv2Hash == null) {
                this.lmv2Hash = NTLMEngineImpl.lmv2Hash(this.domain, this.user, this.getNTLMHash());
            }
            return this.lmv2Hash;
        }

        public byte[] getNTLMv2Hash() throws NTLMEngineException {
            if (this.ntlmv2Hash == null) {
                this.ntlmv2Hash = NTLMEngineImpl.ntlmv2Hash(this.domain, this.user, this.getNTLMHash());
            }
            return this.ntlmv2Hash;
        }

        public byte[] getTimestamp() {
            if (this.timestamp == null) {
                long time = this.currentTime;
                time += 11644473600000L;
                time *= 10000L;
                this.timestamp = new byte[8];
                for (int i = 0; i < 8; ++i) {
                    this.timestamp[i] = (byte)time;
                    time >>>= 8;
                }
            }
            return this.timestamp;
        }

        public byte[] getNTLMv2Blob() {
            if (this.ntlmv2Blob == null) {
                this.ntlmv2Blob = NTLMEngineImpl.createBlob(this.getClientChallenge2(), this.targetInformation, this.getTimestamp());
            }
            return this.ntlmv2Blob;
        }

        public byte[] getNTLMv2Response() throws NTLMEngineException {
            if (this.ntlmv2Response == null) {
                this.ntlmv2Response = NTLMEngineImpl.lmv2Response(this.getNTLMv2Hash(), this.challenge, this.getNTLMv2Blob());
            }
            return this.ntlmv2Response;
        }

        public byte[] getLMv2Response() throws NTLMEngineException {
            if (this.lmv2Response == null) {
                this.lmv2Response = NTLMEngineImpl.lmv2Response(this.getLMv2Hash(), this.challenge, this.getClientChallenge());
            }
            return this.lmv2Response;
        }

        public byte[] getNTLM2SessionResponse() throws NTLMEngineException {
            if (this.ntlm2SessionResponse == null) {
                this.ntlm2SessionResponse = NTLMEngineImpl.ntlm2SessionResponse(this.getNTLMHash(), this.challenge, this.getClientChallenge());
            }
            return this.ntlm2SessionResponse;
        }

        public byte[] getLM2SessionResponse() {
            if (this.lm2SessionResponse == null) {
                byte[] clntChallenge = this.getClientChallenge();
                this.lm2SessionResponse = new byte[24];
                System.arraycopy(clntChallenge, 0, this.lm2SessionResponse, 0, clntChallenge.length);
                Arrays.fill(this.lm2SessionResponse, clntChallenge.length, this.lm2SessionResponse.length, (byte)0);
            }
            return this.lm2SessionResponse;
        }

        public byte[] getLMUserSessionKey() throws NTLMEngineException {
            if (this.lmUserSessionKey == null) {
                this.lmUserSessionKey = new byte[16];
                System.arraycopy(this.getLMHash(), 0, this.lmUserSessionKey, 0, 8);
                Arrays.fill(this.lmUserSessionKey, 8, 16, (byte)0);
            }
            return this.lmUserSessionKey;
        }

        public byte[] getNTLMUserSessionKey() throws NTLMEngineException {
            if (this.ntlmUserSessionKey == null) {
                MD4 md4 = new MD4();
                md4.update(this.getNTLMHash());
                this.ntlmUserSessionKey = md4.getOutput();
            }
            return this.ntlmUserSessionKey;
        }

        public byte[] getNTLMv2UserSessionKey() throws NTLMEngineException {
            if (this.ntlmv2UserSessionKey == null) {
                byte[] ntlmv2hash = this.getNTLMv2Hash();
                byte[] truncatedResponse = new byte[16];
                System.arraycopy(this.getNTLMv2Response(), 0, truncatedResponse, 0, 16);
                this.ntlmv2UserSessionKey = NTLMEngineImpl.hmacMD5(truncatedResponse, ntlmv2hash);
            }
            return this.ntlmv2UserSessionKey;
        }

        public byte[] getNTLM2SessionResponseUserSessionKey() throws NTLMEngineException {
            if (this.ntlm2SessionResponseUserSessionKey == null) {
                byte[] ntlm2SessionResponseNonce = this.getLM2SessionResponse();
                byte[] sessionNonce = new byte[this.challenge.length + ntlm2SessionResponseNonce.length];
                System.arraycopy(this.challenge, 0, sessionNonce, 0, this.challenge.length);
                System.arraycopy(ntlm2SessionResponseNonce, 0, sessionNonce, this.challenge.length, ntlm2SessionResponseNonce.length);
                this.ntlm2SessionResponseUserSessionKey = NTLMEngineImpl.hmacMD5(sessionNonce, this.getNTLMUserSessionKey());
            }
            return this.ntlm2SessionResponseUserSessionKey;
        }

        public byte[] getLanManagerSessionKey() throws NTLMEngineException {
            if (this.lanManagerSessionKey == null) {
                try {
                    byte[] keyBytes = new byte[14];
                    System.arraycopy(this.getLMHash(), 0, keyBytes, 0, 8);
                    Arrays.fill(keyBytes, 8, keyBytes.length, (byte)-67);
                    Key lowKey = NTLMEngineImpl.createDESKey(keyBytes, 0);
                    Key highKey = NTLMEngineImpl.createDESKey(keyBytes, 7);
                    byte[] truncatedResponse = new byte[8];
                    System.arraycopy(this.getLMResponse(), 0, truncatedResponse, 0, truncatedResponse.length);
                    Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
                    des.init(1, lowKey);
                    byte[] lowPart = des.doFinal(truncatedResponse);
                    des = Cipher.getInstance("DES/ECB/NoPadding");
                    des.init(1, highKey);
                    byte[] highPart = des.doFinal(truncatedResponse);
                    this.lanManagerSessionKey = new byte[16];
                    System.arraycopy(lowPart, 0, this.lanManagerSessionKey, 0, lowPart.length);
                    System.arraycopy(highPart, 0, this.lanManagerSessionKey, lowPart.length, highPart.length);
                } catch (Exception e) {
                    throw new NTLMEngineException(e.getMessage(), e);
                }
            }
            return this.lanManagerSessionKey;
        }
    }
}

