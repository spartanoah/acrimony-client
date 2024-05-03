/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.MergedStream;
import com.fasterxml.jackson.core.io.UTF32Reader;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;
import com.fasterxml.jackson.core.json.UTF8StreamJsonParser;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class ByteSourceJsonBootstrapper {
    public static final byte UTF8_BOM_1 = -17;
    public static final byte UTF8_BOM_2 = -69;
    public static final byte UTF8_BOM_3 = -65;
    private final IOContext _context;
    private final InputStream _in;
    private final byte[] _inputBuffer;
    private int _inputPtr;
    private int _inputEnd;
    private final boolean _bufferRecyclable;
    private boolean _bigEndian = true;
    private int _bytesPerChar;

    public ByteSourceJsonBootstrapper(IOContext ctxt, InputStream in) {
        this._context = ctxt;
        this._in = in;
        this._inputBuffer = ctxt.allocReadIOBuffer();
        this._inputPtr = 0;
        this._inputEnd = 0;
        this._bufferRecyclable = true;
    }

    public ByteSourceJsonBootstrapper(IOContext ctxt, byte[] inputBuffer, int inputStart, int inputLen) {
        this._context = ctxt;
        this._in = null;
        this._inputBuffer = inputBuffer;
        this._inputPtr = inputStart;
        this._inputEnd = inputStart + inputLen;
        this._bufferRecyclable = false;
    }

    public JsonEncoding detectEncoding() throws IOException {
        JsonEncoding enc;
        int i16;
        boolean foundEncoding = false;
        if (this.ensureLoaded(4)) {
            int quad = this._inputBuffer[this._inputPtr] << 24 | (this._inputBuffer[this._inputPtr + 1] & 0xFF) << 16 | (this._inputBuffer[this._inputPtr + 2] & 0xFF) << 8 | this._inputBuffer[this._inputPtr + 3] & 0xFF;
            if (this.handleBOM(quad)) {
                foundEncoding = true;
            } else if (this.checkUTF32(quad)) {
                foundEncoding = true;
            } else if (this.checkUTF16(quad >>> 16)) {
                foundEncoding = true;
            }
        } else if (this.ensureLoaded(2) && this.checkUTF16(i16 = (this._inputBuffer[this._inputPtr] & 0xFF) << 8 | this._inputBuffer[this._inputPtr + 1] & 0xFF)) {
            foundEncoding = true;
        }
        if (!foundEncoding) {
            enc = JsonEncoding.UTF8;
        } else {
            switch (this._bytesPerChar) {
                case 1: {
                    enc = JsonEncoding.UTF8;
                    break;
                }
                case 2: {
                    enc = this._bigEndian ? JsonEncoding.UTF16_BE : JsonEncoding.UTF16_LE;
                    break;
                }
                case 4: {
                    enc = this._bigEndian ? JsonEncoding.UTF32_BE : JsonEncoding.UTF32_LE;
                    break;
                }
                default: {
                    throw new RuntimeException("Internal error");
                }
            }
        }
        this._context.setEncoding(enc);
        return enc;
    }

    public static int skipUTF8BOM(DataInput input) throws IOException {
        int b = input.readUnsignedByte();
        if (b != 239) {
            return b;
        }
        b = input.readUnsignedByte();
        if (b != 187) {
            throw new IOException("Unexpected byte 0x" + Integer.toHexString(b) + " following 0xEF; should get 0xBB as part of UTF-8 BOM");
        }
        b = input.readUnsignedByte();
        if (b != 191) {
            throw new IOException("Unexpected byte 0x" + Integer.toHexString(b) + " following 0xEF 0xBB; should get 0xBF as part of UTF-8 BOM");
        }
        return input.readUnsignedByte();
    }

    public Reader constructReader() throws IOException {
        JsonEncoding enc = this._context.getEncoding();
        switch (enc.bits()) {
            case 8: 
            case 16: {
                InputStream in = this._in;
                if (in == null) {
                    in = new ByteArrayInputStream(this._inputBuffer, this._inputPtr, this._inputEnd);
                } else if (this._inputPtr < this._inputEnd) {
                    in = new MergedStream(this._context, in, this._inputBuffer, this._inputPtr, this._inputEnd);
                }
                return new InputStreamReader(in, enc.getJavaName());
            }
            case 32: {
                return new UTF32Reader(this._context, this._in, this._inputBuffer, this._inputPtr, this._inputEnd, this._context.getEncoding().isBigEndian());
            }
        }
        throw new RuntimeException("Internal error");
    }

    public JsonParser constructParser(int parserFeatures, ObjectCodec codec, ByteQuadsCanonicalizer rootByteSymbols, CharsToNameCanonicalizer rootCharSymbols, int factoryFeatures) throws IOException {
        int prevInputPtr = this._inputPtr;
        JsonEncoding enc = this.detectEncoding();
        int bytesProcessed = this._inputPtr - prevInputPtr;
        if (enc == JsonEncoding.UTF8 && JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(factoryFeatures)) {
            ByteQuadsCanonicalizer can = rootByteSymbols.makeChild(factoryFeatures);
            return new UTF8StreamJsonParser(this._context, parserFeatures, this._in, codec, can, this._inputBuffer, this._inputPtr, this._inputEnd, bytesProcessed, this._bufferRecyclable);
        }
        return new ReaderBasedJsonParser(this._context, parserFeatures, this.constructReader(), codec, rootCharSymbols.makeChild(factoryFeatures));
    }

    public static MatchStrength hasJSONFormat(InputAccessor acc) throws IOException {
        int ch;
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        byte b = acc.nextByte();
        if (b == -17) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != -69) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != -65) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
        }
        if ((ch = ByteSourceJsonBootstrapper.skipSpace(acc, b)) < 0) {
            return MatchStrength.INCONCLUSIVE;
        }
        if (ch == 123) {
            ch = ByteSourceJsonBootstrapper.skipSpace(acc);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (ch == 34 || ch == 125) {
                return MatchStrength.SOLID_MATCH;
            }
            return MatchStrength.NO_MATCH;
        }
        if (ch == 91) {
            ch = ByteSourceJsonBootstrapper.skipSpace(acc);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (ch == 93 || ch == 91) {
                return MatchStrength.SOLID_MATCH;
            }
            return MatchStrength.SOLID_MATCH;
        }
        MatchStrength strength = MatchStrength.WEAK_MATCH;
        if (ch == 34) {
            return strength;
        }
        if (ch <= 57 && ch >= 48) {
            return strength;
        }
        if (ch == 45) {
            ch = ByteSourceJsonBootstrapper.skipSpace(acc);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            return ch <= 57 && ch >= 48 ? strength : MatchStrength.NO_MATCH;
        }
        if (ch == 110) {
            return ByteSourceJsonBootstrapper.tryMatch(acc, "ull", strength);
        }
        if (ch == 116) {
            return ByteSourceJsonBootstrapper.tryMatch(acc, "rue", strength);
        }
        if (ch == 102) {
            return ByteSourceJsonBootstrapper.tryMatch(acc, "alse", strength);
        }
        return MatchStrength.NO_MATCH;
    }

    private static MatchStrength tryMatch(InputAccessor acc, String matchStr, MatchStrength fullMatchStrength) throws IOException {
        int len = matchStr.length();
        for (int i = 0; i < len; ++i) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() == matchStr.charAt(i)) continue;
            return MatchStrength.NO_MATCH;
        }
        return fullMatchStrength;
    }

    private static int skipSpace(InputAccessor acc) throws IOException {
        if (!acc.hasMoreBytes()) {
            return -1;
        }
        return ByteSourceJsonBootstrapper.skipSpace(acc, acc.nextByte());
    }

    private static int skipSpace(InputAccessor acc, byte b) throws IOException {
        int ch;
        while ((ch = b & 0xFF) == 32 || ch == 13 || ch == 10 || ch == 9) {
            if (!acc.hasMoreBytes()) {
                return -1;
            }
            b = acc.nextByte();
        }
        return ch;
    }

    private boolean handleBOM(int quad) throws IOException {
        switch (quad) {
            case 65279: {
                this._bigEndian = true;
                this._inputPtr += 4;
                this._bytesPerChar = 4;
                return true;
            }
            case -131072: {
                this._inputPtr += 4;
                this._bytesPerChar = 4;
                this._bigEndian = false;
                return true;
            }
            case 65534: {
                this.reportWeirdUCS4("2143");
                break;
            }
            case -16842752: {
                this.reportWeirdUCS4("3412");
                break;
            }
        }
        int msw = quad >>> 16;
        if (msw == 65279) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = true;
            return true;
        }
        if (msw == 65534) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = false;
            return true;
        }
        if (quad >>> 8 == 0xEFBBBF) {
            this._inputPtr += 3;
            this._bytesPerChar = 1;
            this._bigEndian = true;
            return true;
        }
        return false;
    }

    private boolean checkUTF32(int quad) throws IOException {
        if (quad >> 8 == 0) {
            this._bigEndian = true;
        } else if ((quad & 0xFFFFFF) == 0) {
            this._bigEndian = false;
        } else if ((quad & 0xFF00FFFF) == 0) {
            this.reportWeirdUCS4("3412");
        } else if ((quad & 0xFFFF00FF) == 0) {
            this.reportWeirdUCS4("2143");
        } else {
            return false;
        }
        this._bytesPerChar = 4;
        return true;
    }

    private boolean checkUTF16(int i16) {
        if ((i16 & 0xFF00) == 0) {
            this._bigEndian = true;
        } else if ((i16 & 0xFF) == 0) {
            this._bigEndian = false;
        } else {
            return false;
        }
        this._bytesPerChar = 2;
        return true;
    }

    private void reportWeirdUCS4(String type) throws IOException {
        throw new CharConversionException("Unsupported UCS-4 endianness (" + type + ") detected");
    }

    protected boolean ensureLoaded(int minimum) throws IOException {
        int count;
        for (int gotten = this._inputEnd - this._inputPtr; gotten < minimum; gotten += count) {
            count = this._in == null ? -1 : this._in.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (count < 1) {
                return false;
            }
            this._inputEnd += count;
        }
        return true;
    }
}

