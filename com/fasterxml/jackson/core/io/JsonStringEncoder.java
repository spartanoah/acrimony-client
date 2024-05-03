/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.UTF8Writer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import java.util.Arrays;

public final class JsonStringEncoder {
    private static final char[] HC = CharTypes.copyHexChars();
    private static final byte[] HB = CharTypes.copyHexBytes();
    private static final int SURR1_FIRST = 55296;
    private static final int SURR1_LAST = 56319;
    private static final int SURR2_FIRST = 56320;
    private static final int SURR2_LAST = 57343;
    private static final int INITIAL_CHAR_BUFFER_SIZE = 120;
    private static final int INITIAL_BYTE_BUFFER_SIZE = 200;
    private static final JsonStringEncoder instance = new JsonStringEncoder();

    public static JsonStringEncoder getInstance() {
        return instance;
    }

    public char[] quoteAsString(String input) {
        char[] outputBuffer = new char[120];
        int[] escCodes = CharTypes.get7BitOutputEscapes();
        int escCodeCount = escCodes.length;
        int inPtr = 0;
        int inputLen = input.length();
        TextBuffer textBuffer = null;
        int outPtr = 0;
        char[] qbuf = null;
        block0: while (inPtr < inputLen) {
            char d;
            int escCode;
            int length;
            char c;
            while ((c = input.charAt(inPtr)) >= escCodeCount || escCodes[c] == 0) {
                if (outPtr >= outputBuffer.length) {
                    if (textBuffer == null) {
                        textBuffer = TextBuffer.fromInitial(outputBuffer);
                    }
                    outputBuffer = textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outputBuffer[outPtr++] = c;
                if (++inPtr < inputLen) continue;
                break block0;
            }
            if (qbuf == null) {
                qbuf = this._qbuf();
            }
            int n = length = (escCode = escCodes[d = input.charAt(inPtr++)]) < 0 ? this._appendNumeric(d, qbuf) : this._appendNamed(escCode, qbuf);
            if (outPtr + length > outputBuffer.length) {
                int first = outputBuffer.length - outPtr;
                if (first > 0) {
                    System.arraycopy(qbuf, 0, outputBuffer, outPtr, first);
                }
                if (textBuffer == null) {
                    textBuffer = TextBuffer.fromInitial(outputBuffer);
                }
                outputBuffer = textBuffer.finishCurrentSegment();
                int second = length - first;
                System.arraycopy(qbuf, first, outputBuffer, 0, second);
                outPtr = second;
                continue;
            }
            System.arraycopy(qbuf, 0, outputBuffer, outPtr, length);
            outPtr += length;
        }
        if (textBuffer == null) {
            return Arrays.copyOfRange(outputBuffer, 0, outPtr);
        }
        textBuffer.setCurrentLength(outPtr);
        return textBuffer.contentsAsArray();
    }

    public char[] quoteAsString(CharSequence input) {
        if (input instanceof String) {
            return this.quoteAsString((String)input);
        }
        TextBuffer textBuffer = null;
        char[] outputBuffer = new char[120];
        int[] escCodes = CharTypes.get7BitOutputEscapes();
        int escCodeCount = escCodes.length;
        int inPtr = 0;
        int inputLen = input.length();
        int outPtr = 0;
        char[] qbuf = null;
        block0: while (inPtr < inputLen) {
            char d;
            int escCode;
            int length;
            char c;
            while ((c = input.charAt(inPtr)) >= escCodeCount || escCodes[c] == 0) {
                if (outPtr >= outputBuffer.length) {
                    if (textBuffer == null) {
                        textBuffer = TextBuffer.fromInitial(outputBuffer);
                    }
                    outputBuffer = textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outputBuffer[outPtr++] = c;
                if (++inPtr < inputLen) continue;
                break block0;
            }
            if (qbuf == null) {
                qbuf = this._qbuf();
            }
            int n = length = (escCode = escCodes[d = input.charAt(inPtr++)]) < 0 ? this._appendNumeric(d, qbuf) : this._appendNamed(escCode, qbuf);
            if (outPtr + length > outputBuffer.length) {
                int first = outputBuffer.length - outPtr;
                if (first > 0) {
                    System.arraycopy(qbuf, 0, outputBuffer, outPtr, first);
                }
                if (textBuffer == null) {
                    textBuffer = TextBuffer.fromInitial(outputBuffer);
                }
                outputBuffer = textBuffer.finishCurrentSegment();
                int second = length - first;
                System.arraycopy(qbuf, first, outputBuffer, 0, second);
                outPtr = second;
                continue;
            }
            System.arraycopy(qbuf, 0, outputBuffer, outPtr, length);
            outPtr += length;
        }
        if (textBuffer == null) {
            return Arrays.copyOfRange(outputBuffer, 0, outPtr);
        }
        textBuffer.setCurrentLength(outPtr);
        return textBuffer.contentsAsArray();
    }

    public void quoteAsString(CharSequence input, StringBuilder output) {
        int[] escCodes = CharTypes.get7BitOutputEscapes();
        int escCodeCount = escCodes.length;
        int inPtr = 0;
        int inputLen = input.length();
        char[] qbuf = null;
        block0: while (inPtr < inputLen) {
            char d;
            int escCode;
            char c;
            while ((c = input.charAt(inPtr)) >= escCodeCount || escCodes[c] == 0) {
                output.append(c);
                if (++inPtr < inputLen) continue;
                break block0;
            }
            if (qbuf == null) {
                qbuf = this._qbuf();
            }
            int length = (escCode = escCodes[d = input.charAt(inPtr++)]) < 0 ? this._appendNumeric(d, qbuf) : this._appendNamed(escCode, qbuf);
            output.append(qbuf, 0, length);
        }
    }

    public byte[] quoteAsUTF8(String text) {
        int inputPtr = 0;
        int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = new byte[200];
        ByteArrayBuilder bb = null;
        block0: while (inputPtr < inputEnd) {
            int ch;
            int[] escCodes = CharTypes.get7BitOutputEscapes();
            while ((ch = text.charAt(inputPtr)) <= 127 && escCodes[ch] == 0) {
                if (outputPtr >= outputBuffer.length) {
                    if (bb == null) {
                        bb = ByteArrayBuilder.fromInitial(outputBuffer, outputPtr);
                    }
                    outputBuffer = bb.finishCurrentSegment();
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)ch;
                if (++inputPtr < inputEnd) continue;
                break block0;
            }
            if (bb == null) {
                bb = ByteArrayBuilder.fromInitial(outputBuffer, outputPtr);
            }
            if (outputPtr >= outputBuffer.length) {
                outputBuffer = bb.finishCurrentSegment();
                outputPtr = 0;
            }
            if ((ch = text.charAt(inputPtr++)) <= 127) {
                int escape = escCodes[ch];
                outputPtr = this._appendByte(ch, escape, bb, outputPtr);
                outputBuffer = bb.getCurrentSegment();
                continue;
            }
            if (ch <= 2047) {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                ch = 0x80 | ch & 0x3F;
            } else if (ch < 55296 || ch > 57343) {
                outputBuffer[outputPtr++] = (byte)(0xE0 | ch >> 12);
                if (outputPtr >= outputBuffer.length) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | ch >> 6 & 0x3F);
                ch = 0x80 | ch & 0x3F;
            } else {
                if (ch > 56319) {
                    JsonStringEncoder._illegal(ch);
                }
                if (inputPtr >= inputEnd) {
                    JsonStringEncoder._illegal(ch);
                }
                if ((ch = JsonStringEncoder._convert(ch, text.charAt(inputPtr++))) > 0x10FFFF) {
                    JsonStringEncoder._illegal(ch);
                }
                outputBuffer[outputPtr++] = (byte)(0xF0 | ch >> 18);
                if (outputPtr >= outputBuffer.length) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | ch >> 12 & 0x3F);
                if (outputPtr >= outputBuffer.length) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | ch >> 6 & 0x3F);
                ch = 0x80 | ch & 0x3F;
            }
            if (outputPtr >= outputBuffer.length) {
                outputBuffer = bb.finishCurrentSegment();
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (byte)ch;
        }
        if (bb == null) {
            return Arrays.copyOfRange(outputBuffer, 0, outputPtr);
        }
        return bb.completeAndCoalesce(outputPtr);
    }

    public byte[] encodeAsUTF8(String text) {
        int inputPtr = 0;
        int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = new byte[200];
        int outputEnd = outputBuffer.length;
        ByteArrayBuilder bb = null;
        block0: while (inputPtr < inputEnd) {
            int c = text.charAt(inputPtr++);
            while (c <= 127) {
                if (outputPtr >= outputEnd) {
                    if (bb == null) {
                        bb = ByteArrayBuilder.fromInitial(outputBuffer, outputPtr);
                    }
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)c;
                if (inputPtr >= inputEnd) break block0;
                c = text.charAt(inputPtr++);
            }
            if (bb == null) {
                bb = ByteArrayBuilder.fromInitial(outputBuffer, outputPtr);
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = bb.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            if (c < 2048) {
                outputBuffer[outputPtr++] = (byte)(0xC0 | c >> 6);
            } else if (c < 55296 || c > 57343) {
                outputBuffer[outputPtr++] = (byte)(0xE0 | c >> 12);
                if (outputPtr >= outputEnd) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | c >> 6 & 0x3F);
            } else {
                if (c > 56319) {
                    JsonStringEncoder._illegal(c);
                }
                if (inputPtr >= inputEnd) {
                    JsonStringEncoder._illegal(c);
                }
                if ((c = JsonStringEncoder._convert(c, text.charAt(inputPtr++))) > 0x10FFFF) {
                    JsonStringEncoder._illegal(c);
                }
                outputBuffer[outputPtr++] = (byte)(0xF0 | c >> 18);
                if (outputPtr >= outputEnd) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | c >> 12 & 0x3F);
                if (outputPtr >= outputEnd) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | c >> 6 & 0x3F);
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = bb.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (byte)(0x80 | c & 0x3F);
        }
        if (bb == null) {
            return Arrays.copyOfRange(outputBuffer, 0, outputPtr);
        }
        return bb.completeAndCoalesce(outputPtr);
    }

    public byte[] encodeAsUTF8(CharSequence text) {
        int inputPtr = 0;
        int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = new byte[200];
        int outputEnd = outputBuffer.length;
        ByteArrayBuilder bb = null;
        block0: while (inputPtr < inputEnd) {
            int c = text.charAt(inputPtr++);
            while (c <= 127) {
                if (outputPtr >= outputEnd) {
                    if (bb == null) {
                        bb = ByteArrayBuilder.fromInitial(outputBuffer, outputPtr);
                    }
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)c;
                if (inputPtr >= inputEnd) break block0;
                c = text.charAt(inputPtr++);
            }
            if (bb == null) {
                bb = ByteArrayBuilder.fromInitial(outputBuffer, outputPtr);
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = bb.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            if (c < 2048) {
                outputBuffer[outputPtr++] = (byte)(0xC0 | c >> 6);
            } else if (c < 55296 || c > 57343) {
                outputBuffer[outputPtr++] = (byte)(0xE0 | c >> 12);
                if (outputPtr >= outputEnd) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | c >> 6 & 0x3F);
            } else {
                if (c > 56319) {
                    JsonStringEncoder._illegal(c);
                }
                if (inputPtr >= inputEnd) {
                    JsonStringEncoder._illegal(c);
                }
                if ((c = JsonStringEncoder._convert(c, text.charAt(inputPtr++))) > 0x10FFFF) {
                    JsonStringEncoder._illegal(c);
                }
                outputBuffer[outputPtr++] = (byte)(0xF0 | c >> 18);
                if (outputPtr >= outputEnd) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | c >> 12 & 0x3F);
                if (outputPtr >= outputEnd) {
                    outputBuffer = bb.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | c >> 6 & 0x3F);
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = bb.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (byte)(0x80 | c & 0x3F);
        }
        if (bb == null) {
            return Arrays.copyOfRange(outputBuffer, 0, outputPtr);
        }
        return bb.completeAndCoalesce(outputPtr);
    }

    private char[] _qbuf() {
        char[] qbuf = new char[6];
        qbuf[0] = 92;
        qbuf[2] = 48;
        qbuf[3] = 48;
        return qbuf;
    }

    private int _appendNumeric(int value, char[] qbuf) {
        qbuf[1] = 117;
        qbuf[4] = HC[value >> 4];
        qbuf[5] = HC[value & 0xF];
        return 6;
    }

    private int _appendNamed(int esc, char[] qbuf) {
        qbuf[1] = (char)esc;
        return 2;
    }

    private int _appendByte(int ch, int esc, ByteArrayBuilder bb, int ptr) {
        bb.setCurrentSegmentLength(ptr);
        bb.append(92);
        if (esc < 0) {
            bb.append(117);
            if (ch > 255) {
                int hi = ch >> 8;
                bb.append(HB[hi >> 4]);
                bb.append(HB[hi & 0xF]);
                ch &= 0xFF;
            } else {
                bb.append(48);
                bb.append(48);
            }
            bb.append(HB[ch >> 4]);
            bb.append(HB[ch & 0xF]);
        } else {
            bb.append((byte)esc);
        }
        return bb.getCurrentSegmentLength();
    }

    private static int _convert(int p1, int p2) {
        if (p2 < 56320 || p2 > 57343) {
            throw new IllegalArgumentException("Broken surrogate pair: first char 0x" + Integer.toHexString(p1) + ", second 0x" + Integer.toHexString(p2) + "; illegal combination");
        }
        return 65536 + (p1 - 55296 << 10) + (p2 - 56320);
    }

    private static void _illegal(int c) {
        throw new IllegalArgumentException(UTF8Writer.illegalSurrogateDesc(c));
    }
}

