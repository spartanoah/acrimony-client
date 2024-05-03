/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.codec.digest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

public class DigestUtils {
    private static final int STREAM_BUFFER_LENGTH = 1024;

    private static byte[] digest(MessageDigest digest, InputStream data) throws IOException {
        return DigestUtils.updateDigest(digest, data).digest();
    }

    public static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static MessageDigest getMd2Digest() {
        return DigestUtils.getDigest("MD2");
    }

    public static MessageDigest getMd5Digest() {
        return DigestUtils.getDigest("MD5");
    }

    public static MessageDigest getSha1Digest() {
        return DigestUtils.getDigest("SHA-1");
    }

    public static MessageDigest getSha256Digest() {
        return DigestUtils.getDigest("SHA-256");
    }

    public static MessageDigest getSha384Digest() {
        return DigestUtils.getDigest("SHA-384");
    }

    public static MessageDigest getSha512Digest() {
        return DigestUtils.getDigest("SHA-512");
    }

    @Deprecated
    public static MessageDigest getShaDigest() {
        return DigestUtils.getSha1Digest();
    }

    public static byte[] md2(byte[] data) {
        return DigestUtils.getMd2Digest().digest(data);
    }

    public static byte[] md2(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getMd2Digest(), data);
    }

    public static byte[] md2(String data) {
        return DigestUtils.md2(StringUtils.getBytesUtf8(data));
    }

    public static String md2Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.md2(data));
    }

    public static String md2Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.md2(data));
    }

    public static String md2Hex(String data) {
        return Hex.encodeHexString(DigestUtils.md2(data));
    }

    public static byte[] md5(byte[] data) {
        return DigestUtils.getMd5Digest().digest(data);
    }

    public static byte[] md5(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getMd5Digest(), data);
    }

    public static byte[] md5(String data) {
        return DigestUtils.md5(StringUtils.getBytesUtf8(data));
    }

    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    public static String md5Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    public static String md5Hex(String data) {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    @Deprecated
    public static byte[] sha(byte[] data) {
        return DigestUtils.sha1(data);
    }

    @Deprecated
    public static byte[] sha(InputStream data) throws IOException {
        return DigestUtils.sha1(data);
    }

    @Deprecated
    public static byte[] sha(String data) {
        return DigestUtils.sha1(data);
    }

    public static byte[] sha1(byte[] data) {
        return DigestUtils.getSha1Digest().digest(data);
    }

    public static byte[] sha1(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha1Digest(), data);
    }

    public static byte[] sha1(String data) {
        return DigestUtils.sha1(StringUtils.getBytesUtf8(data));
    }

    public static String sha1Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha1(data));
    }

    public static String sha1Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha1(data));
    }

    public static String sha1Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha1(data));
    }

    public static byte[] sha256(byte[] data) {
        return DigestUtils.getSha256Digest().digest(data);
    }

    public static byte[] sha256(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha256Digest(), data);
    }

    public static byte[] sha256(String data) {
        return DigestUtils.sha256(StringUtils.getBytesUtf8(data));
    }

    public static String sha256Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha256(data));
    }

    public static String sha256Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha256(data));
    }

    public static String sha256Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha256(data));
    }

    public static byte[] sha384(byte[] data) {
        return DigestUtils.getSha384Digest().digest(data);
    }

    public static byte[] sha384(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha384Digest(), data);
    }

    public static byte[] sha384(String data) {
        return DigestUtils.sha384(StringUtils.getBytesUtf8(data));
    }

    public static String sha384Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha384(data));
    }

    public static String sha384Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha384(data));
    }

    public static String sha384Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha384(data));
    }

    public static byte[] sha512(byte[] data) {
        return DigestUtils.getSha512Digest().digest(data);
    }

    public static byte[] sha512(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha512Digest(), data);
    }

    public static byte[] sha512(String data) {
        return DigestUtils.sha512(StringUtils.getBytesUtf8(data));
    }

    public static String sha512Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha512(data));
    }

    public static String sha512Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha512(data));
    }

    public static String sha512Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha512(data));
    }

    @Deprecated
    public static String shaHex(byte[] data) {
        return DigestUtils.sha1Hex(data);
    }

    @Deprecated
    public static String shaHex(InputStream data) throws IOException {
        return DigestUtils.sha1Hex(data);
    }

    @Deprecated
    public static String shaHex(String data) {
        return DigestUtils.sha1Hex(data);
    }

    public static MessageDigest updateDigest(MessageDigest messageDigest, byte[] valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    public static MessageDigest updateDigest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[1024];
        int read = data.read(buffer, 0, 1024);
        while (read > -1) {
            digest.update(buffer, 0, read);
            read = data.read(buffer, 0, 1024);
        }
        return digest;
    }

    public static MessageDigest updateDigest(MessageDigest messageDigest, String valueToDigest) {
        messageDigest.update(StringUtils.getBytesUtf8(valueToDigest));
        return messageDigest;
    }
}

