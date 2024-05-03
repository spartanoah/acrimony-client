/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.viaversion.viaversion.libs.fastutil.booleans.BooleanArrays
 *  com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterable
 *  com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterator
 *  com.viaversion.viaversion.libs.fastutil.bytes.ByteArrays
 *  com.viaversion.viaversion.libs.fastutil.bytes.ByteIterable
 *  com.viaversion.viaversion.libs.fastutil.bytes.ByteIterator
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleArrays
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterable
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterator
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatArrays
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatIterable
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatIterator
 *  com.viaversion.viaversion.libs.fastutil.io.TextIO$BooleanReaderWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.TextIO$ByteReaderWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.TextIO$DoubleReaderWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.TextIO$FloatReaderWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.TextIO$LongReaderWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.TextIO$ShortReaderWrapper
 *  com.viaversion.viaversion.libs.fastutil.longs.LongArrays
 *  com.viaversion.viaversion.libs.fastutil.longs.LongIterable
 *  com.viaversion.viaversion.libs.fastutil.longs.LongIterator
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortArrays
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortIterable
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortIterator
 */
package com.viaversion.viaversion.libs.fastutil.io;

import com.viaversion.viaversion.libs.fastutil.BigArrays;
import com.viaversion.viaversion.libs.fastutil.booleans.BooleanArrays;
import com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterable;
import com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterator;
import com.viaversion.viaversion.libs.fastutil.bytes.ByteArrays;
import com.viaversion.viaversion.libs.fastutil.bytes.ByteIterable;
import com.viaversion.viaversion.libs.fastutil.bytes.ByteIterator;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleArrays;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterable;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterator;
import com.viaversion.viaversion.libs.fastutil.floats.FloatArrays;
import com.viaversion.viaversion.libs.fastutil.floats.FloatIterable;
import com.viaversion.viaversion.libs.fastutil.floats.FloatIterator;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrays;
import com.viaversion.viaversion.libs.fastutil.ints.IntIterable;
import com.viaversion.viaversion.libs.fastutil.ints.IntIterator;
import com.viaversion.viaversion.libs.fastutil.io.FastBufferedOutputStream;
import com.viaversion.viaversion.libs.fastutil.io.TextIO;
import com.viaversion.viaversion.libs.fastutil.longs.LongArrays;
import com.viaversion.viaversion.libs.fastutil.longs.LongIterable;
import com.viaversion.viaversion.libs.fastutil.longs.LongIterator;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortArrays;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortIterable;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortIterator;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;

public class TextIO {
    public static final int BUFFER_SIZE = 8192;

    private TextIO() {
    }

    public static int loadInts(BufferedReader reader, int[] array, int offset, int length) throws IOException {
        IntArrays.ensureOffsetLength(array, offset, length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Integer.parseInt(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadInts(BufferedReader reader, int[] array) throws IOException {
        return TextIO.loadInts(reader, array, 0, array.length);
    }

    public static int loadInts(File file, int[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadInts(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadInts(CharSequence filename, int[] array, int offset, int length) throws IOException {
        return TextIO.loadInts(new File(filename.toString()), array, offset, length);
    }

    public static int loadInts(File file, int[] array) throws IOException {
        return TextIO.loadInts(file, array, 0, array.length);
    }

    public static int loadInts(CharSequence filename, int[] array) throws IOException {
        return TextIO.loadInts(filename, array, 0, array.length);
    }

    public static void storeInts(int[] array, int offset, int length, PrintStream stream) {
        IntArrays.ensureOffsetLength(array, offset, length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeInts(int[] array, PrintStream stream) {
        TextIO.storeInts(array, 0, array.length, stream);
    }

    public static void storeInts(int[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeInts(array, offset, length, stream);
        stream.close();
    }

    public static void storeInts(int[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeInts(array, offset, length, new File(filename.toString()));
    }

    public static void storeInts(int[] array, File file) throws IOException {
        TextIO.storeInts(array, 0, array.length, file);
    }

    public static void storeInts(int[] array, CharSequence filename) throws IOException {
        TextIO.storeInts(array, 0, array.length, filename);
    }

    public static void storeInts(IntIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextInt());
        }
    }

    public static void storeInts(IntIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeInts(i, stream);
        stream.close();
    }

    public static void storeInts(IntIterator i, CharSequence filename) throws IOException {
        TextIO.storeInts(i, new File(filename.toString()));
    }

    public static long loadInts(BufferedReader reader, int[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                int[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Integer.parseInt(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadInts(BufferedReader reader, int[][] array) throws IOException {
        return TextIO.loadInts(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadInts(File file, int[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadInts(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadInts(CharSequence filename, int[][] array, long offset, long length) throws IOException {
        return TextIO.loadInts(new File(filename.toString()), array, offset, length);
    }

    public static long loadInts(File file, int[][] array) throws IOException {
        return TextIO.loadInts(file, array, 0L, BigArrays.length(array));
    }

    public static long loadInts(CharSequence filename, int[][] array) throws IOException {
        return TextIO.loadInts(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeInts(int[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeInts(int[][] array, PrintStream stream) {
        TextIO.storeInts(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeInts(int[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeInts(array, offset, length, stream);
        stream.close();
    }

    public static void storeInts(int[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeInts(array, offset, length, new File(filename.toString()));
    }

    public static void storeInts(int[][] array, File file) throws IOException {
        TextIO.storeInts(array, 0L, BigArrays.length(array), file);
    }

    public static void storeInts(int[][] array, CharSequence filename) throws IOException {
        TextIO.storeInts(array, 0L, BigArrays.length(array), filename);
    }

    public static IntIterator asIntIterator(BufferedReader reader) {
        return new IntReaderWrapper(reader);
    }

    public static IntIterator asIntIterator(File file) throws IOException {
        return new IntReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static IntIterator asIntIterator(CharSequence filename) throws IOException {
        return TextIO.asIntIterator(new File(filename.toString()));
    }

    public static IntIterable asIntIterable(File file) {
        return () -> {
            try {
                return TextIO.asIntIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static IntIterable asIntIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asIntIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadLongs(BufferedReader reader, long[] array, int offset, int length) throws IOException {
        LongArrays.ensureOffsetLength((long[])array, (int)offset, (int)length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Long.parseLong(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadLongs(BufferedReader reader, long[] array) throws IOException {
        return TextIO.loadLongs(reader, array, 0, array.length);
    }

    public static int loadLongs(File file, long[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadLongs(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadLongs(CharSequence filename, long[] array, int offset, int length) throws IOException {
        return TextIO.loadLongs(new File(filename.toString()), array, offset, length);
    }

    public static int loadLongs(File file, long[] array) throws IOException {
        return TextIO.loadLongs(file, array, 0, array.length);
    }

    public static int loadLongs(CharSequence filename, long[] array) throws IOException {
        return TextIO.loadLongs(filename, array, 0, array.length);
    }

    public static void storeLongs(long[] array, int offset, int length, PrintStream stream) {
        LongArrays.ensureOffsetLength((long[])array, (int)offset, (int)length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeLongs(long[] array, PrintStream stream) {
        TextIO.storeLongs(array, 0, array.length, stream);
    }

    public static void storeLongs(long[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeLongs(array, offset, length, stream);
        stream.close();
    }

    public static void storeLongs(long[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeLongs(array, offset, length, new File(filename.toString()));
    }

    public static void storeLongs(long[] array, File file) throws IOException {
        TextIO.storeLongs(array, 0, array.length, file);
    }

    public static void storeLongs(long[] array, CharSequence filename) throws IOException {
        TextIO.storeLongs(array, 0, array.length, filename);
    }

    public static void storeLongs(LongIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextLong());
        }
    }

    public static void storeLongs(LongIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeLongs(i, stream);
        stream.close();
    }

    public static void storeLongs(LongIterator i, CharSequence filename) throws IOException {
        TextIO.storeLongs(i, new File(filename.toString()));
    }

    public static long loadLongs(BufferedReader reader, long[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                long[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Long.parseLong(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadLongs(BufferedReader reader, long[][] array) throws IOException {
        return TextIO.loadLongs(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadLongs(File file, long[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadLongs(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadLongs(CharSequence filename, long[][] array, long offset, long length) throws IOException {
        return TextIO.loadLongs(new File(filename.toString()), array, offset, length);
    }

    public static long loadLongs(File file, long[][] array) throws IOException {
        return TextIO.loadLongs(file, array, 0L, BigArrays.length(array));
    }

    public static long loadLongs(CharSequence filename, long[][] array) throws IOException {
        return TextIO.loadLongs(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeLongs(long[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            long[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeLongs(long[][] array, PrintStream stream) {
        TextIO.storeLongs(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeLongs(long[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeLongs(array, offset, length, stream);
        stream.close();
    }

    public static void storeLongs(long[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeLongs(array, offset, length, new File(filename.toString()));
    }

    public static void storeLongs(long[][] array, File file) throws IOException {
        TextIO.storeLongs(array, 0L, BigArrays.length(array), file);
    }

    public static void storeLongs(long[][] array, CharSequence filename) throws IOException {
        TextIO.storeLongs(array, 0L, BigArrays.length(array), filename);
    }

    public static LongIterator asLongIterator(BufferedReader reader) {
        return new LongReaderWrapper(reader);
    }

    public static LongIterator asLongIterator(File file) throws IOException {
        return new LongReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static LongIterator asLongIterator(CharSequence filename) throws IOException {
        return TextIO.asLongIterator(new File(filename.toString()));
    }

    public static LongIterable asLongIterable(File file) {
        return () -> {
            try {
                return TextIO.asLongIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static LongIterable asLongIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asLongIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadDoubles(BufferedReader reader, double[] array, int offset, int length) throws IOException {
        DoubleArrays.ensureOffsetLength((double[])array, (int)offset, (int)length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Double.parseDouble(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadDoubles(BufferedReader reader, double[] array) throws IOException {
        return TextIO.loadDoubles(reader, array, 0, array.length);
    }

    public static int loadDoubles(File file, double[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadDoubles(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadDoubles(CharSequence filename, double[] array, int offset, int length) throws IOException {
        return TextIO.loadDoubles(new File(filename.toString()), array, offset, length);
    }

    public static int loadDoubles(File file, double[] array) throws IOException {
        return TextIO.loadDoubles(file, array, 0, array.length);
    }

    public static int loadDoubles(CharSequence filename, double[] array) throws IOException {
        return TextIO.loadDoubles(filename, array, 0, array.length);
    }

    public static void storeDoubles(double[] array, int offset, int length, PrintStream stream) {
        DoubleArrays.ensureOffsetLength((double[])array, (int)offset, (int)length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeDoubles(double[] array, PrintStream stream) {
        TextIO.storeDoubles(array, 0, array.length, stream);
    }

    public static void storeDoubles(double[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeDoubles(array, offset, length, stream);
        stream.close();
    }

    public static void storeDoubles(double[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeDoubles(array, offset, length, new File(filename.toString()));
    }

    public static void storeDoubles(double[] array, File file) throws IOException {
        TextIO.storeDoubles(array, 0, array.length, file);
    }

    public static void storeDoubles(double[] array, CharSequence filename) throws IOException {
        TextIO.storeDoubles(array, 0, array.length, filename);
    }

    public static void storeDoubles(DoubleIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextDouble());
        }
    }

    public static void storeDoubles(DoubleIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeDoubles(i, stream);
        stream.close();
    }

    public static void storeDoubles(DoubleIterator i, CharSequence filename) throws IOException {
        TextIO.storeDoubles(i, new File(filename.toString()));
    }

    public static long loadDoubles(BufferedReader reader, double[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                double[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Double.parseDouble(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadDoubles(BufferedReader reader, double[][] array) throws IOException {
        return TextIO.loadDoubles(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadDoubles(File file, double[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadDoubles(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadDoubles(CharSequence filename, double[][] array, long offset, long length) throws IOException {
        return TextIO.loadDoubles(new File(filename.toString()), array, offset, length);
    }

    public static long loadDoubles(File file, double[][] array) throws IOException {
        return TextIO.loadDoubles(file, array, 0L, BigArrays.length(array));
    }

    public static long loadDoubles(CharSequence filename, double[][] array) throws IOException {
        return TextIO.loadDoubles(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeDoubles(double[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            double[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeDoubles(double[][] array, PrintStream stream) {
        TextIO.storeDoubles(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeDoubles(double[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeDoubles(array, offset, length, stream);
        stream.close();
    }

    public static void storeDoubles(double[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeDoubles(array, offset, length, new File(filename.toString()));
    }

    public static void storeDoubles(double[][] array, File file) throws IOException {
        TextIO.storeDoubles(array, 0L, BigArrays.length(array), file);
    }

    public static void storeDoubles(double[][] array, CharSequence filename) throws IOException {
        TextIO.storeDoubles(array, 0L, BigArrays.length(array), filename);
    }

    public static DoubleIterator asDoubleIterator(BufferedReader reader) {
        return new DoubleReaderWrapper(reader);
    }

    public static DoubleIterator asDoubleIterator(File file) throws IOException {
        return new DoubleReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static DoubleIterator asDoubleIterator(CharSequence filename) throws IOException {
        return TextIO.asDoubleIterator(new File(filename.toString()));
    }

    public static DoubleIterable asDoubleIterable(File file) {
        return () -> {
            try {
                return TextIO.asDoubleIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static DoubleIterable asDoubleIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asDoubleIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadBooleans(BufferedReader reader, boolean[] array, int offset, int length) throws IOException {
        BooleanArrays.ensureOffsetLength((boolean[])array, (int)offset, (int)length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Boolean.parseBoolean(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadBooleans(BufferedReader reader, boolean[] array) throws IOException {
        return TextIO.loadBooleans(reader, array, 0, array.length);
    }

    public static int loadBooleans(File file, boolean[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadBooleans(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadBooleans(CharSequence filename, boolean[] array, int offset, int length) throws IOException {
        return TextIO.loadBooleans(new File(filename.toString()), array, offset, length);
    }

    public static int loadBooleans(File file, boolean[] array) throws IOException {
        return TextIO.loadBooleans(file, array, 0, array.length);
    }

    public static int loadBooleans(CharSequence filename, boolean[] array) throws IOException {
        return TextIO.loadBooleans(filename, array, 0, array.length);
    }

    public static void storeBooleans(boolean[] array, int offset, int length, PrintStream stream) {
        BooleanArrays.ensureOffsetLength((boolean[])array, (int)offset, (int)length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeBooleans(boolean[] array, PrintStream stream) {
        TextIO.storeBooleans(array, 0, array.length, stream);
    }

    public static void storeBooleans(boolean[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeBooleans(array, offset, length, stream);
        stream.close();
    }

    public static void storeBooleans(boolean[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeBooleans(array, offset, length, new File(filename.toString()));
    }

    public static void storeBooleans(boolean[] array, File file) throws IOException {
        TextIO.storeBooleans(array, 0, array.length, file);
    }

    public static void storeBooleans(boolean[] array, CharSequence filename) throws IOException {
        TextIO.storeBooleans(array, 0, array.length, filename);
    }

    public static void storeBooleans(BooleanIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextBoolean());
        }
    }

    public static void storeBooleans(BooleanIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeBooleans(i, stream);
        stream.close();
    }

    public static void storeBooleans(BooleanIterator i, CharSequence filename) throws IOException {
        TextIO.storeBooleans(i, new File(filename.toString()));
    }

    public static long loadBooleans(BufferedReader reader, boolean[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                boolean[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Boolean.parseBoolean(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadBooleans(BufferedReader reader, boolean[][] array) throws IOException {
        return TextIO.loadBooleans(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadBooleans(File file, boolean[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadBooleans(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadBooleans(CharSequence filename, boolean[][] array, long offset, long length) throws IOException {
        return TextIO.loadBooleans(new File(filename.toString()), array, offset, length);
    }

    public static long loadBooleans(File file, boolean[][] array) throws IOException {
        return TextIO.loadBooleans(file, array, 0L, BigArrays.length(array));
    }

    public static long loadBooleans(CharSequence filename, boolean[][] array) throws IOException {
        return TextIO.loadBooleans(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeBooleans(boolean[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            boolean[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeBooleans(boolean[][] array, PrintStream stream) {
        TextIO.storeBooleans(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeBooleans(boolean[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeBooleans(array, offset, length, stream);
        stream.close();
    }

    public static void storeBooleans(boolean[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeBooleans(array, offset, length, new File(filename.toString()));
    }

    public static void storeBooleans(boolean[][] array, File file) throws IOException {
        TextIO.storeBooleans(array, 0L, BigArrays.length(array), file);
    }

    public static void storeBooleans(boolean[][] array, CharSequence filename) throws IOException {
        TextIO.storeBooleans(array, 0L, BigArrays.length(array), filename);
    }

    public static BooleanIterator asBooleanIterator(BufferedReader reader) {
        return new BooleanReaderWrapper(reader);
    }

    public static BooleanIterator asBooleanIterator(File file) throws IOException {
        return new BooleanReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static BooleanIterator asBooleanIterator(CharSequence filename) throws IOException {
        return TextIO.asBooleanIterator(new File(filename.toString()));
    }

    public static BooleanIterable asBooleanIterable(File file) {
        return () -> {
            try {
                return TextIO.asBooleanIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static BooleanIterable asBooleanIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asBooleanIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadBytes(BufferedReader reader, byte[] array, int offset, int length) throws IOException {
        ByteArrays.ensureOffsetLength((byte[])array, (int)offset, (int)length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Byte.parseByte(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadBytes(BufferedReader reader, byte[] array) throws IOException {
        return TextIO.loadBytes(reader, array, 0, array.length);
    }

    public static int loadBytes(File file, byte[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadBytes(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadBytes(CharSequence filename, byte[] array, int offset, int length) throws IOException {
        return TextIO.loadBytes(new File(filename.toString()), array, offset, length);
    }

    public static int loadBytes(File file, byte[] array) throws IOException {
        return TextIO.loadBytes(file, array, 0, array.length);
    }

    public static int loadBytes(CharSequence filename, byte[] array) throws IOException {
        return TextIO.loadBytes(filename, array, 0, array.length);
    }

    public static void storeBytes(byte[] array, int offset, int length, PrintStream stream) {
        ByteArrays.ensureOffsetLength((byte[])array, (int)offset, (int)length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeBytes(byte[] array, PrintStream stream) {
        TextIO.storeBytes(array, 0, array.length, stream);
    }

    public static void storeBytes(byte[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeBytes(array, offset, length, stream);
        stream.close();
    }

    public static void storeBytes(byte[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeBytes(array, offset, length, new File(filename.toString()));
    }

    public static void storeBytes(byte[] array, File file) throws IOException {
        TextIO.storeBytes(array, 0, array.length, file);
    }

    public static void storeBytes(byte[] array, CharSequence filename) throws IOException {
        TextIO.storeBytes(array, 0, array.length, filename);
    }

    public static void storeBytes(ByteIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextByte());
        }
    }

    public static void storeBytes(ByteIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeBytes(i, stream);
        stream.close();
    }

    public static void storeBytes(ByteIterator i, CharSequence filename) throws IOException {
        TextIO.storeBytes(i, new File(filename.toString()));
    }

    public static long loadBytes(BufferedReader reader, byte[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                byte[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Byte.parseByte(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadBytes(BufferedReader reader, byte[][] array) throws IOException {
        return TextIO.loadBytes(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadBytes(File file, byte[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadBytes(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadBytes(CharSequence filename, byte[][] array, long offset, long length) throws IOException {
        return TextIO.loadBytes(new File(filename.toString()), array, offset, length);
    }

    public static long loadBytes(File file, byte[][] array) throws IOException {
        return TextIO.loadBytes(file, array, 0L, BigArrays.length(array));
    }

    public static long loadBytes(CharSequence filename, byte[][] array) throws IOException {
        return TextIO.loadBytes(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeBytes(byte[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            byte[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeBytes(byte[][] array, PrintStream stream) {
        TextIO.storeBytes(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeBytes(byte[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeBytes(array, offset, length, stream);
        stream.close();
    }

    public static void storeBytes(byte[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeBytes(array, offset, length, new File(filename.toString()));
    }

    public static void storeBytes(byte[][] array, File file) throws IOException {
        TextIO.storeBytes(array, 0L, BigArrays.length(array), file);
    }

    public static void storeBytes(byte[][] array, CharSequence filename) throws IOException {
        TextIO.storeBytes(array, 0L, BigArrays.length(array), filename);
    }

    public static ByteIterator asByteIterator(BufferedReader reader) {
        return new ByteReaderWrapper(reader);
    }

    public static ByteIterator asByteIterator(File file) throws IOException {
        return new ByteReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static ByteIterator asByteIterator(CharSequence filename) throws IOException {
        return TextIO.asByteIterator(new File(filename.toString()));
    }

    public static ByteIterable asByteIterable(File file) {
        return () -> {
            try {
                return TextIO.asByteIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static ByteIterable asByteIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asByteIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadShorts(BufferedReader reader, short[] array, int offset, int length) throws IOException {
        ShortArrays.ensureOffsetLength((short[])array, (int)offset, (int)length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Short.parseShort(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadShorts(BufferedReader reader, short[] array) throws IOException {
        return TextIO.loadShorts(reader, array, 0, array.length);
    }

    public static int loadShorts(File file, short[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadShorts(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadShorts(CharSequence filename, short[] array, int offset, int length) throws IOException {
        return TextIO.loadShorts(new File(filename.toString()), array, offset, length);
    }

    public static int loadShorts(File file, short[] array) throws IOException {
        return TextIO.loadShorts(file, array, 0, array.length);
    }

    public static int loadShorts(CharSequence filename, short[] array) throws IOException {
        return TextIO.loadShorts(filename, array, 0, array.length);
    }

    public static void storeShorts(short[] array, int offset, int length, PrintStream stream) {
        ShortArrays.ensureOffsetLength((short[])array, (int)offset, (int)length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeShorts(short[] array, PrintStream stream) {
        TextIO.storeShorts(array, 0, array.length, stream);
    }

    public static void storeShorts(short[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeShorts(array, offset, length, stream);
        stream.close();
    }

    public static void storeShorts(short[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeShorts(array, offset, length, new File(filename.toString()));
    }

    public static void storeShorts(short[] array, File file) throws IOException {
        TextIO.storeShorts(array, 0, array.length, file);
    }

    public static void storeShorts(short[] array, CharSequence filename) throws IOException {
        TextIO.storeShorts(array, 0, array.length, filename);
    }

    public static void storeShorts(ShortIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextShort());
        }
    }

    public static void storeShorts(ShortIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeShorts(i, stream);
        stream.close();
    }

    public static void storeShorts(ShortIterator i, CharSequence filename) throws IOException {
        TextIO.storeShorts(i, new File(filename.toString()));
    }

    public static long loadShorts(BufferedReader reader, short[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                short[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Short.parseShort(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadShorts(BufferedReader reader, short[][] array) throws IOException {
        return TextIO.loadShorts(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadShorts(File file, short[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadShorts(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadShorts(CharSequence filename, short[][] array, long offset, long length) throws IOException {
        return TextIO.loadShorts(new File(filename.toString()), array, offset, length);
    }

    public static long loadShorts(File file, short[][] array) throws IOException {
        return TextIO.loadShorts(file, array, 0L, BigArrays.length(array));
    }

    public static long loadShorts(CharSequence filename, short[][] array) throws IOException {
        return TextIO.loadShorts(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeShorts(short[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            short[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeShorts(short[][] array, PrintStream stream) {
        TextIO.storeShorts(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeShorts(short[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeShorts(array, offset, length, stream);
        stream.close();
    }

    public static void storeShorts(short[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeShorts(array, offset, length, new File(filename.toString()));
    }

    public static void storeShorts(short[][] array, File file) throws IOException {
        TextIO.storeShorts(array, 0L, BigArrays.length(array), file);
    }

    public static void storeShorts(short[][] array, CharSequence filename) throws IOException {
        TextIO.storeShorts(array, 0L, BigArrays.length(array), filename);
    }

    public static ShortIterator asShortIterator(BufferedReader reader) {
        return new ShortReaderWrapper(reader);
    }

    public static ShortIterator asShortIterator(File file) throws IOException {
        return new ShortReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static ShortIterator asShortIterator(CharSequence filename) throws IOException {
        return TextIO.asShortIterator(new File(filename.toString()));
    }

    public static ShortIterable asShortIterable(File file) {
        return () -> {
            try {
                return TextIO.asShortIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static ShortIterable asShortIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asShortIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadFloats(BufferedReader reader, float[] array, int offset, int length) throws IOException {
        FloatArrays.ensureOffsetLength((float[])array, (int)offset, (int)length);
        int i = 0;
        try {
            String s;
            for (i = 0; i < length && (s = reader.readLine()) != null; ++i) {
                array[i + offset] = Float.parseFloat(s.trim());
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadFloats(BufferedReader reader, float[] array) throws IOException {
        return TextIO.loadFloats(reader, array, 0, array.length);
    }

    public static int loadFloats(File file, float[] array, int offset, int length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int result = TextIO.loadFloats(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static int loadFloats(CharSequence filename, float[] array, int offset, int length) throws IOException {
        return TextIO.loadFloats(new File(filename.toString()), array, offset, length);
    }

    public static int loadFloats(File file, float[] array) throws IOException {
        return TextIO.loadFloats(file, array, 0, array.length);
    }

    public static int loadFloats(CharSequence filename, float[] array) throws IOException {
        return TextIO.loadFloats(filename, array, 0, array.length);
    }

    public static void storeFloats(float[] array, int offset, int length, PrintStream stream) {
        FloatArrays.ensureOffsetLength((float[])array, (int)offset, (int)length);
        for (int i = 0; i < length; ++i) {
            stream.println(array[offset + i]);
        }
    }

    public static void storeFloats(float[] array, PrintStream stream) {
        TextIO.storeFloats(array, 0, array.length, stream);
    }

    public static void storeFloats(float[] array, int offset, int length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeFloats(array, offset, length, stream);
        stream.close();
    }

    public static void storeFloats(float[] array, int offset, int length, CharSequence filename) throws IOException {
        TextIO.storeFloats(array, offset, length, new File(filename.toString()));
    }

    public static void storeFloats(float[] array, File file) throws IOException {
        TextIO.storeFloats(array, 0, array.length, file);
    }

    public static void storeFloats(float[] array, CharSequence filename) throws IOException {
        TextIO.storeFloats(array, 0, array.length, filename);
    }

    public static void storeFloats(FloatIterator i, PrintStream stream) {
        while (i.hasNext()) {
            stream.println(i.nextFloat());
        }
    }

    public static void storeFloats(FloatIterator i, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeFloats(i, stream);
        stream.close();
    }

    public static void storeFloats(FloatIterator i, CharSequence filename) throws IOException {
        TextIO.storeFloats(i, new File(filename.toString()));
    }

    public static long loadFloats(BufferedReader reader, float[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                float[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    String s = reader.readLine();
                    if (s == null) {
                        return c;
                    }
                    t[d] = Float.parseFloat(s.trim());
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadFloats(BufferedReader reader, float[][] array) throws IOException {
        return TextIO.loadFloats(reader, array, 0L, BigArrays.length(array));
    }

    public static long loadFloats(File file, float[][] array, long offset, long length) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long result = TextIO.loadFloats(reader, array, offset, length);
        reader.close();
        return result;
    }

    public static long loadFloats(CharSequence filename, float[][] array, long offset, long length) throws IOException {
        return TextIO.loadFloats(new File(filename.toString()), array, offset, length);
    }

    public static long loadFloats(File file, float[][] array) throws IOException {
        return TextIO.loadFloats(file, array, 0L, BigArrays.length(array));
    }

    public static long loadFloats(CharSequence filename, float[][] array) throws IOException {
        return TextIO.loadFloats(filename, array, 0L, BigArrays.length(array));
    }

    public static void storeFloats(float[][] array, long offset, long length, PrintStream stream) {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            float[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                stream.println(t[d]);
            }
        }
    }

    public static void storeFloats(float[][] array, PrintStream stream) {
        TextIO.storeFloats(array, 0L, BigArrays.length(array), stream);
    }

    public static void storeFloats(float[][] array, long offset, long length, File file) throws IOException {
        PrintStream stream = new PrintStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        TextIO.storeFloats(array, offset, length, stream);
        stream.close();
    }

    public static void storeFloats(float[][] array, long offset, long length, CharSequence filename) throws IOException {
        TextIO.storeFloats(array, offset, length, new File(filename.toString()));
    }

    public static void storeFloats(float[][] array, File file) throws IOException {
        TextIO.storeFloats(array, 0L, BigArrays.length(array), file);
    }

    public static void storeFloats(float[][] array, CharSequence filename) throws IOException {
        TextIO.storeFloats(array, 0L, BigArrays.length(array), filename);
    }

    public static FloatIterator asFloatIterator(BufferedReader reader) {
        return new FloatReaderWrapper(reader);
    }

    public static FloatIterator asFloatIterator(File file) throws IOException {
        return new FloatReaderWrapper(new BufferedReader(new FileReader(file)));
    }

    public static FloatIterator asFloatIterator(CharSequence filename) throws IOException {
        return TextIO.asFloatIterator(new File(filename.toString()));
    }

    public static FloatIterable asFloatIterable(File file) {
        return () -> {
            try {
                return TextIO.asFloatIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static FloatIterable asFloatIterable(CharSequence filename) {
        return () -> {
            try {
                return TextIO.asFloatIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static final class IntReaderWrapper
    implements IntIterator {
        private final BufferedReader reader;
        private boolean toAdvance = true;
        private String s;
        private int next;

        public IntReaderWrapper(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public boolean hasNext() {
            if (!this.toAdvance) {
                return this.s != null;
            }
            this.toAdvance = false;
            try {
                this.s = this.reader.readLine();
            } catch (EOFException eOFException) {
            } catch (IOException rethrow) {
                throw new RuntimeException(rethrow);
            }
            if (this.s == null) {
                return false;
            }
            this.next = Integer.parseInt(this.s.trim());
            return true;
        }

        @Override
        public int nextInt() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.toAdvance = true;
            return this.next;
        }
    }
}

