/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;

class FilePasswordProvider
implements PasswordProvider {
    private final Path passwordPath;

    public FilePasswordProvider(String passwordFile) throws NoSuchFileException {
        this.passwordPath = Paths.get(passwordFile, new String[0]);
        if (!Files.exists(this.passwordPath, new LinkOption[0])) {
            throw new NoSuchFileException("PasswordFile '" + passwordFile + "' does not exist");
        }
    }

    @Override
    public char[] getPassword() {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(this.passwordPath);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            CharBuffer decoded = Charset.defaultCharset().decode(bb);
            char[] result = new char[decoded.limit()];
            decoded.get(result, 0, result.length);
            decoded.rewind();
            decoded.put(new char[result.length]);
            char[] cArray = result;
            return cArray;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read password from " + this.passwordPath + ": " + e, e);
        } finally {
            if (bytes != null) {
                Arrays.fill(bytes, (byte)0);
            }
        }
    }
}

