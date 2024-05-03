/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class PseudoRandomFunction {
    private PseudoRandomFunction() {
    }

    static byte[] hash(byte[] secret, byte[] label, byte[] seed, int length, String algo) {
        ObjectUtil.checkPositiveOrZero(length, "length");
        try {
            byte[] data;
            Mac hmac = Mac.getInstance(algo);
            hmac.init(new SecretKeySpec(secret, algo));
            int iterations = (int)Math.ceil((double)length / (double)hmac.getMacLength());
            byte[] expansion = EmptyArrays.EMPTY_BYTES;
            byte[] A = data = PseudoRandomFunction.concat(label, seed);
            for (int i = 0; i < iterations; ++i) {
                A = hmac.doFinal(A);
                expansion = PseudoRandomFunction.concat(expansion, hmac.doFinal(PseudoRandomFunction.concat(A, data)));
            }
            return Arrays.copyOf(expansion, length);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Could not find algo: " + algo, e);
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}

