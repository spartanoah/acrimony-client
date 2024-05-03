/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.viaversion.viaversion.libs.fastutil.booleans.BooleanBigArrays
 *  com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterable
 *  com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterator
 *  com.viaversion.viaversion.libs.fastutil.bytes.ByteBigArrays
 *  com.viaversion.viaversion.libs.fastutil.bytes.ByteIterable
 *  com.viaversion.viaversion.libs.fastutil.bytes.ByteIterator
 *  com.viaversion.viaversion.libs.fastutil.chars.CharBigArrays
 *  com.viaversion.viaversion.libs.fastutil.chars.CharIterable
 *  com.viaversion.viaversion.libs.fastutil.chars.CharIterator
 *  com.viaversion.viaversion.libs.fastutil.chars.CharMappedBigList
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleBigArrays
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterable
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterator
 *  com.viaversion.viaversion.libs.fastutil.doubles.DoubleMappedBigList
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatBigArrays
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatIterable
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatIterator
 *  com.viaversion.viaversion.libs.fastutil.floats.FloatMappedBigList
 *  com.viaversion.viaversion.libs.fastutil.ints.IntBigArrays
 *  com.viaversion.viaversion.libs.fastutil.ints.IntMappedBigList
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$BooleanDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$ByteDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$CharDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$CharDataNioInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$DoubleDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$DoubleDataNioInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$FloatDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$FloatDataNioInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$LongDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$LongDataNioInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$ShortDataInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.io.BinIO$ShortDataNioInputWrapper
 *  com.viaversion.viaversion.libs.fastutil.longs.LongBigArrays
 *  com.viaversion.viaversion.libs.fastutil.longs.LongIterable
 *  com.viaversion.viaversion.libs.fastutil.longs.LongIterator
 *  com.viaversion.viaversion.libs.fastutil.longs.LongMappedBigList
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortBigArrays
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortIterable
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortIterator
 *  com.viaversion.viaversion.libs.fastutil.shorts.ShortMappedBigList
 */
package com.viaversion.viaversion.libs.fastutil.io;

import com.viaversion.viaversion.libs.fastutil.Arrays;
import com.viaversion.viaversion.libs.fastutil.BigArrays;
import com.viaversion.viaversion.libs.fastutil.booleans.BooleanBigArrays;
import com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterable;
import com.viaversion.viaversion.libs.fastutil.booleans.BooleanIterator;
import com.viaversion.viaversion.libs.fastutil.bytes.ByteBigArrays;
import com.viaversion.viaversion.libs.fastutil.bytes.ByteIterable;
import com.viaversion.viaversion.libs.fastutil.bytes.ByteIterator;
import com.viaversion.viaversion.libs.fastutil.chars.CharBigArrays;
import com.viaversion.viaversion.libs.fastutil.chars.CharIterable;
import com.viaversion.viaversion.libs.fastutil.chars.CharIterator;
import com.viaversion.viaversion.libs.fastutil.chars.CharMappedBigList;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleBigArrays;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterable;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleIterator;
import com.viaversion.viaversion.libs.fastutil.doubles.DoubleMappedBigList;
import com.viaversion.viaversion.libs.fastutil.floats.FloatBigArrays;
import com.viaversion.viaversion.libs.fastutil.floats.FloatIterable;
import com.viaversion.viaversion.libs.fastutil.floats.FloatIterator;
import com.viaversion.viaversion.libs.fastutil.floats.FloatMappedBigList;
import com.viaversion.viaversion.libs.fastutil.ints.IntBigArrays;
import com.viaversion.viaversion.libs.fastutil.ints.IntIterable;
import com.viaversion.viaversion.libs.fastutil.ints.IntIterator;
import com.viaversion.viaversion.libs.fastutil.ints.IntMappedBigList;
import com.viaversion.viaversion.libs.fastutil.io.BinIO;
import com.viaversion.viaversion.libs.fastutil.io.FastBufferedInputStream;
import com.viaversion.viaversion.libs.fastutil.io.FastBufferedOutputStream;
import com.viaversion.viaversion.libs.fastutil.longs.LongBigArrays;
import com.viaversion.viaversion.libs.fastutil.longs.LongIterable;
import com.viaversion.viaversion.libs.fastutil.longs.LongIterator;
import com.viaversion.viaversion.libs.fastutil.longs.LongMappedBigList;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortBigArrays;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortIterable;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortIterator;
import com.viaversion.viaversion.libs.fastutil.shorts.ShortMappedBigList;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;

public class BinIO {
    public static int BUFFER_SIZE = 8192;
    private static final int MAX_IO_LENGTH = 0x100000;

    private BinIO() {
    }

    public static void storeObject(Object o, File file) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        oos.writeObject(o);
        oos.close();
    }

    public static void storeObject(Object o, CharSequence filename) throws IOException {
        BinIO.storeObject(o, new File(filename.toString()));
    }

    public static Object loadObject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FastBufferedInputStream(new FileInputStream(file)));
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public static Object loadObject(CharSequence filename) throws IOException, ClassNotFoundException {
        return BinIO.loadObject(new File(filename.toString()));
    }

    public static void storeObject(Object o, OutputStream s) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FastBufferedOutputStream(s));
        oos.writeObject(o);
        oos.flush();
    }

    public static Object loadObject(InputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FastBufferedInputStream(s));
        Object result = ois.readObject();
        return result;
    }

    public static int loadBooleans(DataInput dataInput, boolean[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readBoolean();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadBooleans(DataInput dataInput, boolean[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readBoolean();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadBooleans(File file, boolean[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        DataInputStream dis = new DataInputStream(new FastBufferedInputStream(new FileInputStream(file)));
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dis.readBoolean();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        dis.close();
        return i;
    }

    public static int loadBooleans(CharSequence filename, boolean[] array, int offset, int length) throws IOException {
        return BinIO.loadBooleans(new File(filename.toString()), array, offset, length);
    }

    public static int loadBooleans(File file, boolean[] array) throws IOException {
        return BinIO.loadBooleans(file, array, 0, array.length);
    }

    public static int loadBooleans(CharSequence filename, boolean[] array) throws IOException {
        return BinIO.loadBooleans(new File(filename.toString()), array);
    }

    public static boolean[] loadBooleans(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        long length = fis.getChannel().size();
        if (length > Integer.MAX_VALUE) {
            fis.close();
            throw new IllegalArgumentException("File too long: " + fis.getChannel().size() + " bytes (" + length + " elements)");
        }
        boolean[] array = new boolean[(int)length];
        DataInputStream dis = new DataInputStream(new FastBufferedInputStream(fis));
        int i = 0;
        while ((long)i < length) {
            array[i] = dis.readBoolean();
            ++i;
        }
        dis.close();
        return array;
    }

    public static boolean[] loadBooleans(CharSequence filename) throws IOException {
        return BinIO.loadBooleans(new File(filename.toString()));
    }

    public static void storeBooleans(boolean[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeBoolean(array[offset + i]);
        }
    }

    public static void storeBooleans(boolean[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeBoolean(array[i]);
        }
    }

    public static void storeBooleans(boolean[] array, int offset, int length, File file) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        DataOutputStream dos = new DataOutputStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        for (int i = 0; i < length; ++i) {
            dos.writeBoolean(array[offset + i]);
        }
        dos.close();
    }

    public static void storeBooleans(boolean[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeBooleans(array, offset, length, new File(filename.toString()));
    }

    public static void storeBooleans(boolean[] array, File file) throws IOException {
        BinIO.storeBooleans(array, 0, array.length, file);
    }

    public static void storeBooleans(boolean[] array, CharSequence filename) throws IOException {
        BinIO.storeBooleans(array, new File(filename.toString()));
    }

    public static long loadBooleans(DataInput dataInput, boolean[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                boolean[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readBoolean();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadBooleans(DataInput dataInput, boolean[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                boolean[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readBoolean();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadBooleans(File file, boolean[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(new FastBufferedInputStream(fis));
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                boolean[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dis.readBoolean();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        dis.close();
        return c;
    }

    public static long loadBooleans(CharSequence filename, boolean[][] array, long offset, long length) throws IOException {
        return BinIO.loadBooleans(new File(filename.toString()), array, offset, length);
    }

    public static long loadBooleans(File file, boolean[][] array) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(new FastBufferedInputStream(fis));
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                boolean[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dis.readBoolean();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        dis.close();
        return c;
    }

    public static long loadBooleans(CharSequence filename, boolean[][] array) throws IOException {
        return BinIO.loadBooleans(new File(filename.toString()), array);
    }

    public static boolean[][] loadBooleansBig(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        long length = fis.getChannel().size();
        boolean[][] array = BooleanBigArrays.newBigArray((long)length);
        DataInputStream dis = new DataInputStream(new FastBufferedInputStream(fis));
        for (int i = 0; i < array.length; ++i) {
            boolean[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                t[d] = dis.readBoolean();
            }
        }
        dis.close();
        return array;
    }

    public static boolean[][] loadBooleansBig(CharSequence filename) throws IOException {
        return BinIO.loadBooleansBig(new File(filename.toString()));
    }

    public static void storeBooleans(boolean[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            boolean[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeBoolean(t[d]);
            }
        }
    }

    public static void storeBooleans(boolean[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            boolean[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeBoolean(t[d]);
            }
        }
    }

    public static void storeBooleans(boolean[][] array, long offset, long length, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            boolean[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dos.writeBoolean(t[d]);
            }
        }
        dos.close();
    }

    public static void storeBooleans(boolean[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeBooleans(array, offset, length, new File(filename.toString()));
    }

    public static void storeBooleans(boolean[][] array, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        for (int i = 0; i < array.length; ++i) {
            boolean[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dos.writeBoolean(t[d]);
            }
        }
        dos.close();
    }

    public static void storeBooleans(boolean[][] array, CharSequence filename) throws IOException {
        BinIO.storeBooleans(array, new File(filename.toString()));
    }

    public static void storeBooleans(BooleanIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeBoolean(i.nextBoolean());
        }
    }

    public static void storeBooleans(BooleanIterator i, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        while (i.hasNext()) {
            dos.writeBoolean(i.nextBoolean());
        }
        dos.close();
    }

    public static void storeBooleans(BooleanIterator i, CharSequence filename) throws IOException {
        BinIO.storeBooleans(i, new File(filename.toString()));
    }

    public static BooleanIterator asBooleanIterator(DataInput dataInput) {
        return new BooleanDataInputWrapper(dataInput);
    }

    public static BooleanIterator asBooleanIterator(File file) throws IOException {
        return new BooleanDataInputWrapper((DataInput)new DataInputStream(new FastBufferedInputStream(new FileInputStream(file))));
    }

    public static BooleanIterator asBooleanIterator(CharSequence filename) throws IOException {
        return BinIO.asBooleanIterator(new File(filename.toString()));
    }

    public static BooleanIterable asBooleanIterable(File file) {
        return () -> {
            try {
                return BinIO.asBooleanIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static BooleanIterable asBooleanIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asBooleanIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static int read(InputStream is, byte[] a, int offset, int length) throws IOException {
        int result;
        if (length == 0) {
            return 0;
        }
        int read = 0;
        do {
            if ((result = is.read(a, offset + read, Math.min(length - read, 0x100000))) >= 0) continue;
            return read;
        } while ((read += result) < length);
        return read;
    }

    private static void write(OutputStream outputStream, byte[] a, int offset, int length) throws IOException {
        for (int written = 0; written < length; written += Math.min(length - written, 0x100000)) {
            outputStream.write(a, offset + written, Math.min(length - written, 0x100000));
        }
    }

    private static void write(DataOutput dataOutput, byte[] a, int offset, int length) throws IOException {
        for (int written = 0; written < length; written += Math.min(length - written, 0x100000)) {
            dataOutput.write(a, offset + written, Math.min(length - written, 0x100000));
        }
    }

    public static int loadBytes(InputStream inputStream, byte[] array, int offset, int length) throws IOException {
        return BinIO.read(inputStream, array, offset, length);
    }

    public static int loadBytes(InputStream inputStream, byte[] array) throws IOException {
        return BinIO.read(inputStream, array, 0, array.length);
    }

    public static void storeBytes(byte[] array, int offset, int length, OutputStream outputStream) throws IOException {
        BinIO.write(outputStream, array, offset, length);
    }

    public static void storeBytes(byte[] array, OutputStream outputStream) throws IOException {
        BinIO.write(outputStream, array, 0, array.length);
    }

    private static long read(InputStream is, byte[][] a, long offset, long length) throws IOException {
        if (length == 0L) {
            return 0L;
        }
        long read = 0L;
        int segment = BigArrays.segment(offset);
        int displacement = BigArrays.displacement(offset);
        do {
            int result;
            if ((result = is.read(a[segment], displacement, (int)Math.min((long)(a[segment].length - displacement), Math.min(length - read, 0x100000L)))) < 0) {
                return read;
            }
            read += (long)result;
            if ((displacement += result) != a[segment].length) continue;
            ++segment;
            displacement = 0;
        } while (read < length);
        return read;
    }

    private static void write(OutputStream outputStream, byte[][] a, long offset, long length) throws IOException {
        if (length == 0L) {
            return;
        }
        long written = 0L;
        int segment = BigArrays.segment(offset);
        int displacement = BigArrays.displacement(offset);
        do {
            int toWrite = (int)Math.min((long)(a[segment].length - displacement), Math.min(length - written, 0x100000L));
            outputStream.write(a[segment], displacement, toWrite);
            written += (long)toWrite;
            if ((displacement += toWrite) != a[segment].length) continue;
            ++segment;
            displacement = 0;
        } while (written < length);
    }

    private static void write(DataOutput dataOutput, byte[][] a, long offset, long length) throws IOException {
        if (length == 0L) {
            return;
        }
        long written = 0L;
        int segment = BigArrays.segment(offset);
        int displacement = BigArrays.displacement(offset);
        do {
            int toWrite = (int)Math.min((long)(a[segment].length - displacement), Math.min(length - written, 0x100000L));
            dataOutput.write(a[segment], displacement, toWrite);
            written += (long)toWrite;
            if ((displacement += toWrite) != a[segment].length) continue;
            ++segment;
            displacement = 0;
        } while (written < length);
    }

    public static long loadBytes(InputStream inputStream, byte[][] array, long offset, long length) throws IOException {
        return BinIO.read(inputStream, array, offset, length);
    }

    public static long loadBytes(InputStream inputStream, byte[][] array) throws IOException {
        return BinIO.read(inputStream, array, 0L, BigArrays.length(array));
    }

    public static void storeBytes(byte[][] array, long offset, long length, OutputStream outputStream) throws IOException {
        BinIO.write(outputStream, array, offset, length);
    }

    public static void storeBytes(byte[][] array, OutputStream outputStream) throws IOException {
        BinIO.write(outputStream, array, 0L, BigArrays.length(array));
    }

    public static int loadBytes(ReadableByteChannel channel, byte[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        int read = 0;
        while (true) {
            buffer.clear();
            buffer.limit(Math.min(buffer.capacity(), length));
            int r = channel.read(buffer);
            if (r <= 0) {
                return read;
            }
            read += r;
            buffer.flip();
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadBytes(ReadableByteChannel channel, byte[] array) throws IOException {
        return BinIO.loadBytes(channel, array, 0, array.length);
    }

    public static void storeBytes(byte[] array, int offset, int length, WritableByteChannel channel) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            channel.write(buffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeBytes(byte[] array, WritableByteChannel channel) throws IOException {
        BinIO.storeBytes(array, 0, array.length, channel);
    }

    public static long loadBytes(ReadableByteChannel channel, byte[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            byte[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadBytes(channel, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadBytes(ReadableByteChannel channel, byte[][] array) throws IOException {
        return BinIO.loadBytes(channel, array, 0L, BigArrays.length(array));
    }

    public static void storeBytes(byte[][] array, long offset, long length, WritableByteChannel channel) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeBytes(array[i], s, l - s, channel);
        }
    }

    public static void storeBytes(byte[][] array, WritableByteChannel channel) throws IOException {
        for (byte[] t : array) {
            BinIO.storeBytes(t, channel);
        }
    }

    public static int loadBytes(DataInput dataInput, byte[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readByte();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadBytes(DataInput dataInput, byte[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readByte();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadBytes(File file, byte[] array, int offset, int length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int result = BinIO.loadBytes((ReadableByteChannel)channel, array, offset, length);
        channel.close();
        return result;
    }

    public static int loadBytes(CharSequence filename, byte[] array, int offset, int length) throws IOException {
        return BinIO.loadBytes(new File(filename.toString()), array, offset, length);
    }

    public static int loadBytes(File file, byte[] array) throws IOException {
        return BinIO.loadBytes(file, array, 0, array.length);
    }

    public static int loadBytes(CharSequence filename, byte[] array) throws IOException {
        return BinIO.loadBytes(new File(filename.toString()), array);
    }

    public static byte[] loadBytes(File file) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size();
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        byte[] array = new byte[(int)length];
        if ((long)BinIO.loadBytes((ReadableByteChannel)channel, array) < length) {
            throw new EOFException();
        }
        return array;
    }

    public static byte[] loadBytes(CharSequence filename) throws IOException {
        return BinIO.loadBytes(new File(filename.toString()));
    }

    public static void storeBytes(byte[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        BinIO.write(dataOutput, array, offset, length);
    }

    public static void storeBytes(byte[] array, DataOutput dataOutput) throws IOException {
        BinIO.write(dataOutput, array, 0, array.length);
    }

    public static void storeBytes(byte[] array, int offset, int length, File file) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeBytes(array, offset, length, (WritableByteChannel)channel);
        channel.close();
    }

    public static void storeBytes(byte[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeBytes(array, offset, length, new File(filename.toString()));
    }

    public static void storeBytes(byte[] array, File file) throws IOException {
        BinIO.storeBytes(array, 0, array.length, file);
    }

    public static void storeBytes(byte[] array, CharSequence filename) throws IOException {
        BinIO.storeBytes(array, new File(filename.toString()));
    }

    public static long loadBytes(DataInput dataInput, byte[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                byte[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readByte();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadBytes(DataInput dataInput, byte[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                byte[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readByte();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadBytes(File file, byte[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadBytes((ReadableByteChannel)channel, array, offset, length);
        return read;
    }

    public static long loadBytes(CharSequence filename, byte[][] array, long offset, long length) throws IOException {
        return BinIO.loadBytes(new File(filename.toString()), array, offset, length);
    }

    public static long loadBytes(File file, byte[][] array) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadBytes((ReadableByteChannel)channel, array);
        return read;
    }

    public static long loadBytes(CharSequence filename, byte[][] array) throws IOException {
        return BinIO.loadBytes(new File(filename.toString()), array);
    }

    public static byte[][] loadBytesBig(File file) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size();
        byte[][] array = ByteBigArrays.newBigArray((long)length);
        BinIO.loadBytes((ReadableByteChannel)channel, array);
        channel.close();
        return array;
    }

    public static byte[][] loadBytesBig(CharSequence filename) throws IOException {
        return BinIO.loadBytesBig(new File(filename.toString()));
    }

    public static void storeBytes(byte[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        BinIO.write(dataOutput, array, offset, length);
    }

    public static void storeBytes(byte[][] array, DataOutput dataOutput) throws IOException {
        BinIO.write(dataOutput, array, 0L, BigArrays.length(array));
    }

    public static void storeBytes(byte[][] array, long offset, long length, File file) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeBytes(array, offset, length, (WritableByteChannel)channel);
        channel.close();
    }

    public static void storeBytes(byte[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeBytes(array, offset, length, new File(filename.toString()));
    }

    public static void storeBytes(byte[][] array, File file) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeBytes(array, (WritableByteChannel)channel);
        channel.close();
    }

    public static void storeBytes(byte[][] array, CharSequence filename) throws IOException {
        BinIO.storeBytes(array, new File(filename.toString()));
    }

    public static void storeBytes(ByteIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeByte(i.nextByte());
        }
    }

    public static void storeBytes(ByteIterator i, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FastBufferedOutputStream(new FileOutputStream(file)));
        while (i.hasNext()) {
            dos.writeByte(i.nextByte());
        }
        dos.close();
    }

    public static void storeBytes(ByteIterator i, CharSequence filename) throws IOException {
        BinIO.storeBytes(i, new File(filename.toString()));
    }

    public static ByteIterator asByteIterator(DataInput dataInput) {
        return new ByteDataInputWrapper(dataInput);
    }

    public static ByteIterator asByteIterator(File file) throws IOException {
        return new ByteDataInputWrapper((DataInput)new DataInputStream(new FastBufferedInputStream(new FileInputStream(file))));
    }

    public static ByteIterator asByteIterator(CharSequence filename) throws IOException {
        return BinIO.asByteIterator(new File(filename.toString()));
    }

    public static ByteIterable asByteIterable(File file) {
        return () -> {
            try {
                return BinIO.asByteIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static ByteIterable asByteIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asByteIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadChars(ReadableByteChannel channel, ByteOrder byteOrder, char[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        CharBuffer buffer = byteBuffer.asCharBuffer();
        int read = 0;
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min((long)buffer.capacity(), (long)length << CharMappedBigList.LOG2_BYTES));
            int r = channel.read(byteBuffer);
            if (r <= 0) {
                return read;
            }
            read += (r >>>= CharMappedBigList.LOG2_BYTES);
            buffer.clear();
            buffer.limit(r);
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadChars(ReadableByteChannel channel, ByteOrder byteOrder, char[] array) throws IOException {
        return BinIO.loadChars(channel, byteOrder, array, 0, array.length);
    }

    public static int loadChars(File file, ByteOrder byteOrder, char[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int read = BinIO.loadChars((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static int loadChars(CharSequence filename, ByteOrder byteOrder, char[] array, int offset, int length) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static int loadChars(File file, ByteOrder byteOrder, char[] array) throws IOException {
        return BinIO.loadChars(file, byteOrder, array, 0, array.length);
    }

    public static int loadChars(CharSequence filename, ByteOrder byteOrder, char[] array) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), byteOrder, array);
    }

    public static char[] loadChars(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 2L;
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        char[] array = new char[(int)length];
        if ((long)BinIO.loadChars((ReadableByteChannel)channel, byteOrder, array) < length) {
            throw new EOFException();
        }
        channel.close();
        return array;
    }

    public static char[] loadChars(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), byteOrder);
    }

    public static void storeChars(char[] array, int offset, int length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        CharBuffer buffer = byteBuffer.asCharBuffer();
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            byteBuffer.clear();
            byteBuffer.limit(buffer.limit() << CharMappedBigList.LOG2_BYTES);
            channel.write(byteBuffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeChars(char[] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(array, 0, array.length, channel, byteOrder);
    }

    public static void storeChars(char[] array, int offset, int length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeChars(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeChars(char[] array, int offset, int length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeChars(char[] array, File file, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(array, 0, array.length, file, byteOrder);
    }

    public static void storeChars(char[] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(array, new File(filename.toString()), byteOrder);
    }

    public static long loadChars(ReadableByteChannel channel, ByteOrder byteOrder, char[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            char[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadChars(channel, byteOrder, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadChars(ReadableByteChannel channel, ByteOrder byteOrder, char[][] array) throws IOException {
        return BinIO.loadChars(channel, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadChars(File file, ByteOrder byteOrder, char[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadChars((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static long loadChars(CharSequence filename, ByteOrder byteOrder, char[][] array, long offset, long length) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static long loadChars(File file, ByteOrder byteOrder, char[][] array) throws IOException {
        return BinIO.loadChars(file, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadChars(CharSequence filename, ByteOrder byteOrder, char[][] array) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), byteOrder, array);
    }

    public static char[][] loadCharsBig(File file, ByteOrder byteOrder) throws IOException {
        char[][] array;
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 2L;
        for (char[] t : array = CharBigArrays.newBigArray((long)length)) {
            BinIO.loadChars((ReadableByteChannel)channel, byteOrder, t);
        }
        channel.close();
        return array;
    }

    public static char[][] loadCharsBig(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadCharsBig(new File(filename.toString()), byteOrder);
    }

    public static void storeChars(char[][] array, long offset, long length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeChars(array[i], s, l - s, channel, byteOrder);
        }
    }

    public static void storeChars(char[][] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (char[] t : array) {
            BinIO.storeChars(t, channel, byteOrder);
        }
    }

    public static void storeChars(char[][] array, long offset, long length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeChars(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeChars(char[][] array, long offset, long length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeChars(char[][] array, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeChars(array, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeChars(char[][] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(array, new File(filename.toString()), byteOrder);
    }

    public static void storeChars(CharIterator i, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        CharBuffer buffer = byteBuffer.asCharBuffer();
        while (i.hasNext()) {
            if (!buffer.hasRemaining()) {
                buffer.flip();
                byteBuffer.clear();
                byteBuffer.limit(buffer.limit() << CharMappedBigList.LOG2_BYTES);
                channel.write(byteBuffer);
                buffer.clear();
            }
            buffer.put(i.nextChar());
        }
        buffer.flip();
        byteBuffer.clear();
        byteBuffer.limit(buffer.limit() << CharMappedBigList.LOG2_BYTES);
        channel.write(byteBuffer);
    }

    public static void storeChars(CharIterator i, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeChars(i, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeChars(CharIterator i, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeChars(i, new File(filename.toString()), byteOrder);
    }

    public static CharIterator asCharIterator(ReadableByteChannel channel, ByteOrder byteOrder) {
        return new CharDataNioInputWrapper(channel, byteOrder);
    }

    public static CharIterator asCharIterator(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        return new CharDataNioInputWrapper((ReadableByteChannel)channel, byteOrder);
    }

    public static CharIterator asCharIterator(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.asCharIterator(new File(filename.toString()), byteOrder);
    }

    public static CharIterable asCharIterable(File file, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asCharIterator(file, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static CharIterable asCharIterable(CharSequence filename, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asCharIterator(filename, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadChars(DataInput dataInput, char[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readChar();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadChars(DataInput dataInput, char[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readChar();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadChars(File file, char[] array, int offset, int length) throws IOException {
        return BinIO.loadChars(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static int loadChars(CharSequence filename, char[] array, int offset, int length) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), array, offset, length);
    }

    public static int loadChars(File file, char[] array) throws IOException {
        return BinIO.loadChars(file, array, 0, array.length);
    }

    public static int loadChars(CharSequence filename, char[] array) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), array);
    }

    public static char[] loadChars(File file) throws IOException {
        return BinIO.loadChars(file, ByteOrder.BIG_ENDIAN);
    }

    public static char[] loadChars(CharSequence filename) throws IOException {
        return BinIO.loadChars(new File(filename.toString()));
    }

    public static void storeChars(char[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeChar(array[offset + i]);
        }
    }

    public static void storeChars(char[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeChar(array[i]);
        }
    }

    public static void storeChars(char[] array, int offset, int length, File file) throws IOException {
        BinIO.storeChars(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeChars(char[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeChars(array, offset, length, new File(filename.toString()));
    }

    public static void storeChars(char[] array, File file) throws IOException {
        BinIO.storeChars(array, 0, array.length, file);
    }

    public static void storeChars(char[] array, CharSequence filename) throws IOException {
        BinIO.storeChars(array, new File(filename.toString()));
    }

    public static long loadChars(DataInput dataInput, char[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                char[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readChar();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadChars(DataInput dataInput, char[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                char[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readChar();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadChars(File file, char[][] array, long offset, long length) throws IOException {
        return BinIO.loadChars(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static long loadChars(CharSequence filename, char[][] array, long offset, long length) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), array, offset, length);
    }

    public static long loadChars(File file, char[][] array) throws IOException {
        return BinIO.loadChars(file, ByteOrder.BIG_ENDIAN, array);
    }

    public static long loadChars(CharSequence filename, char[][] array) throws IOException {
        return BinIO.loadChars(new File(filename.toString()), array);
    }

    public static char[][] loadCharsBig(File file) throws IOException {
        return BinIO.loadCharsBig(file, ByteOrder.BIG_ENDIAN);
    }

    public static char[][] loadCharsBig(CharSequence filename) throws IOException {
        return BinIO.loadCharsBig(new File(filename.toString()));
    }

    public static void storeChars(char[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            char[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeChar(t[d]);
            }
        }
    }

    public static void storeChars(char[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            char[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeChar(t[d]);
            }
        }
    }

    public static void storeChars(char[][] array, long offset, long length, File file) throws IOException {
        BinIO.storeChars(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeChars(char[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeChars(array, offset, length, new File(filename.toString()));
    }

    public static void storeChars(char[][] array, File file) throws IOException {
        BinIO.storeChars(array, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeChars(char[][] array, CharSequence filename) throws IOException {
        BinIO.storeChars(array, new File(filename.toString()));
    }

    public static void storeChars(CharIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeChar(i.nextChar());
        }
    }

    public static void storeChars(CharIterator i, File file) throws IOException {
        BinIO.storeChars(i, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeChars(CharIterator i, CharSequence filename) throws IOException {
        BinIO.storeChars(i, new File(filename.toString()));
    }

    public static CharIterator asCharIterator(DataInput dataInput) {
        return new CharDataInputWrapper(dataInput);
    }

    public static CharIterator asCharIterator(File file) throws IOException {
        return BinIO.asCharIterator(file, ByteOrder.BIG_ENDIAN);
    }

    public static CharIterator asCharIterator(CharSequence filename) throws IOException {
        return BinIO.asCharIterator(new File(filename.toString()));
    }

    public static CharIterable asCharIterable(File file) {
        return () -> {
            try {
                return BinIO.asCharIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static CharIterable asCharIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asCharIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadShorts(ReadableByteChannel channel, ByteOrder byteOrder, short[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        ShortBuffer buffer = byteBuffer.asShortBuffer();
        int read = 0;
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min((long)buffer.capacity(), (long)length << ShortMappedBigList.LOG2_BYTES));
            int r = channel.read(byteBuffer);
            if (r <= 0) {
                return read;
            }
            read += (r >>>= ShortMappedBigList.LOG2_BYTES);
            buffer.clear();
            buffer.limit(r);
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadShorts(ReadableByteChannel channel, ByteOrder byteOrder, short[] array) throws IOException {
        return BinIO.loadShorts(channel, byteOrder, array, 0, array.length);
    }

    public static int loadShorts(File file, ByteOrder byteOrder, short[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int read = BinIO.loadShorts((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static int loadShorts(CharSequence filename, ByteOrder byteOrder, short[] array, int offset, int length) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static int loadShorts(File file, ByteOrder byteOrder, short[] array) throws IOException {
        return BinIO.loadShorts(file, byteOrder, array, 0, array.length);
    }

    public static int loadShorts(CharSequence filename, ByteOrder byteOrder, short[] array) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), byteOrder, array);
    }

    public static short[] loadShorts(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 2L;
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        short[] array = new short[(int)length];
        if ((long)BinIO.loadShorts((ReadableByteChannel)channel, byteOrder, array) < length) {
            throw new EOFException();
        }
        channel.close();
        return array;
    }

    public static short[] loadShorts(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), byteOrder);
    }

    public static void storeShorts(short[] array, int offset, int length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        ShortBuffer buffer = byteBuffer.asShortBuffer();
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            byteBuffer.clear();
            byteBuffer.limit(buffer.limit() << ShortMappedBigList.LOG2_BYTES);
            channel.write(byteBuffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeShorts(short[] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(array, 0, array.length, channel, byteOrder);
    }

    public static void storeShorts(short[] array, int offset, int length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeShorts(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeShorts(short[] array, int offset, int length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeShorts(short[] array, File file, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(array, 0, array.length, file, byteOrder);
    }

    public static void storeShorts(short[] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(array, new File(filename.toString()), byteOrder);
    }

    public static long loadShorts(ReadableByteChannel channel, ByteOrder byteOrder, short[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            short[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadShorts(channel, byteOrder, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadShorts(ReadableByteChannel channel, ByteOrder byteOrder, short[][] array) throws IOException {
        return BinIO.loadShorts(channel, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadShorts(File file, ByteOrder byteOrder, short[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadShorts((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static long loadShorts(CharSequence filename, ByteOrder byteOrder, short[][] array, long offset, long length) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static long loadShorts(File file, ByteOrder byteOrder, short[][] array) throws IOException {
        return BinIO.loadShorts(file, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadShorts(CharSequence filename, ByteOrder byteOrder, short[][] array) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), byteOrder, array);
    }

    public static short[][] loadShortsBig(File file, ByteOrder byteOrder) throws IOException {
        short[][] array;
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 2L;
        for (short[] t : array = ShortBigArrays.newBigArray((long)length)) {
            BinIO.loadShorts((ReadableByteChannel)channel, byteOrder, t);
        }
        channel.close();
        return array;
    }

    public static short[][] loadShortsBig(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadShortsBig(new File(filename.toString()), byteOrder);
    }

    public static void storeShorts(short[][] array, long offset, long length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeShorts(array[i], s, l - s, channel, byteOrder);
        }
    }

    public static void storeShorts(short[][] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (short[] t : array) {
            BinIO.storeShorts(t, channel, byteOrder);
        }
    }

    public static void storeShorts(short[][] array, long offset, long length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeShorts(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeShorts(short[][] array, long offset, long length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeShorts(short[][] array, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeShorts(array, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeShorts(short[][] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(array, new File(filename.toString()), byteOrder);
    }

    public static void storeShorts(ShortIterator i, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        ShortBuffer buffer = byteBuffer.asShortBuffer();
        while (i.hasNext()) {
            if (!buffer.hasRemaining()) {
                buffer.flip();
                byteBuffer.clear();
                byteBuffer.limit(buffer.limit() << ShortMappedBigList.LOG2_BYTES);
                channel.write(byteBuffer);
                buffer.clear();
            }
            buffer.put(i.nextShort());
        }
        buffer.flip();
        byteBuffer.clear();
        byteBuffer.limit(buffer.limit() << ShortMappedBigList.LOG2_BYTES);
        channel.write(byteBuffer);
    }

    public static void storeShorts(ShortIterator i, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeShorts(i, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeShorts(ShortIterator i, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeShorts(i, new File(filename.toString()), byteOrder);
    }

    public static ShortIterator asShortIterator(ReadableByteChannel channel, ByteOrder byteOrder) {
        return new ShortDataNioInputWrapper(channel, byteOrder);
    }

    public static ShortIterator asShortIterator(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        return new ShortDataNioInputWrapper((ReadableByteChannel)channel, byteOrder);
    }

    public static ShortIterator asShortIterator(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.asShortIterator(new File(filename.toString()), byteOrder);
    }

    public static ShortIterable asShortIterable(File file, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asShortIterator(file, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static ShortIterable asShortIterable(CharSequence filename, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asShortIterator(filename, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadShorts(DataInput dataInput, short[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readShort();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadShorts(DataInput dataInput, short[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readShort();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadShorts(File file, short[] array, int offset, int length) throws IOException {
        return BinIO.loadShorts(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static int loadShorts(CharSequence filename, short[] array, int offset, int length) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), array, offset, length);
    }

    public static int loadShorts(File file, short[] array) throws IOException {
        return BinIO.loadShorts(file, array, 0, array.length);
    }

    public static int loadShorts(CharSequence filename, short[] array) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), array);
    }

    public static short[] loadShorts(File file) throws IOException {
        return BinIO.loadShorts(file, ByteOrder.BIG_ENDIAN);
    }

    public static short[] loadShorts(CharSequence filename) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()));
    }

    public static void storeShorts(short[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeShort(array[offset + i]);
        }
    }

    public static void storeShorts(short[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeShort(array[i]);
        }
    }

    public static void storeShorts(short[] array, int offset, int length, File file) throws IOException {
        BinIO.storeShorts(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeShorts(short[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeShorts(array, offset, length, new File(filename.toString()));
    }

    public static void storeShorts(short[] array, File file) throws IOException {
        BinIO.storeShorts(array, 0, array.length, file);
    }

    public static void storeShorts(short[] array, CharSequence filename) throws IOException {
        BinIO.storeShorts(array, new File(filename.toString()));
    }

    public static long loadShorts(DataInput dataInput, short[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                short[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readShort();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadShorts(DataInput dataInput, short[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                short[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readShort();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadShorts(File file, short[][] array, long offset, long length) throws IOException {
        return BinIO.loadShorts(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static long loadShorts(CharSequence filename, short[][] array, long offset, long length) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), array, offset, length);
    }

    public static long loadShorts(File file, short[][] array) throws IOException {
        return BinIO.loadShorts(file, ByteOrder.BIG_ENDIAN, array);
    }

    public static long loadShorts(CharSequence filename, short[][] array) throws IOException {
        return BinIO.loadShorts(new File(filename.toString()), array);
    }

    public static short[][] loadShortsBig(File file) throws IOException {
        return BinIO.loadShortsBig(file, ByteOrder.BIG_ENDIAN);
    }

    public static short[][] loadShortsBig(CharSequence filename) throws IOException {
        return BinIO.loadShortsBig(new File(filename.toString()));
    }

    public static void storeShorts(short[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            short[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeShort(t[d]);
            }
        }
    }

    public static void storeShorts(short[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            short[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeShort(t[d]);
            }
        }
    }

    public static void storeShorts(short[][] array, long offset, long length, File file) throws IOException {
        BinIO.storeShorts(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeShorts(short[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeShorts(array, offset, length, new File(filename.toString()));
    }

    public static void storeShorts(short[][] array, File file) throws IOException {
        BinIO.storeShorts(array, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeShorts(short[][] array, CharSequence filename) throws IOException {
        BinIO.storeShorts(array, new File(filename.toString()));
    }

    public static void storeShorts(ShortIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeShort(i.nextShort());
        }
    }

    public static void storeShorts(ShortIterator i, File file) throws IOException {
        BinIO.storeShorts(i, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeShorts(ShortIterator i, CharSequence filename) throws IOException {
        BinIO.storeShorts(i, new File(filename.toString()));
    }

    public static ShortIterator asShortIterator(DataInput dataInput) {
        return new ShortDataInputWrapper(dataInput);
    }

    public static ShortIterator asShortIterator(File file) throws IOException {
        return BinIO.asShortIterator(file, ByteOrder.BIG_ENDIAN);
    }

    public static ShortIterator asShortIterator(CharSequence filename) throws IOException {
        return BinIO.asShortIterator(new File(filename.toString()));
    }

    public static ShortIterable asShortIterable(File file) {
        return () -> {
            try {
                return BinIO.asShortIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static ShortIterable asShortIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asShortIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadInts(ReadableByteChannel channel, ByteOrder byteOrder, int[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        IntBuffer buffer = byteBuffer.asIntBuffer();
        int read = 0;
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min((long)buffer.capacity(), (long)length << IntMappedBigList.LOG2_BYTES));
            int r = channel.read(byteBuffer);
            if (r <= 0) {
                return read;
            }
            read += (r >>>= IntMappedBigList.LOG2_BYTES);
            buffer.clear();
            buffer.limit(r);
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadInts(ReadableByteChannel channel, ByteOrder byteOrder, int[] array) throws IOException {
        return BinIO.loadInts(channel, byteOrder, array, 0, array.length);
    }

    public static int loadInts(File file, ByteOrder byteOrder, int[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int read = BinIO.loadInts((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static int loadInts(CharSequence filename, ByteOrder byteOrder, int[] array, int offset, int length) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static int loadInts(File file, ByteOrder byteOrder, int[] array) throws IOException {
        return BinIO.loadInts(file, byteOrder, array, 0, array.length);
    }

    public static int loadInts(CharSequence filename, ByteOrder byteOrder, int[] array) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), byteOrder, array);
    }

    public static int[] loadInts(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 4L;
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        int[] array = new int[(int)length];
        if ((long)BinIO.loadInts((ReadableByteChannel)channel, byteOrder, array) < length) {
            throw new EOFException();
        }
        channel.close();
        return array;
    }

    public static int[] loadInts(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), byteOrder);
    }

    public static void storeInts(int[] array, int offset, int length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        IntBuffer buffer = byteBuffer.asIntBuffer();
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            byteBuffer.clear();
            byteBuffer.limit(buffer.limit() << IntMappedBigList.LOG2_BYTES);
            channel.write(byteBuffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeInts(int[] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(array, 0, array.length, channel, byteOrder);
    }

    public static void storeInts(int[] array, int offset, int length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeInts(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeInts(int[] array, int offset, int length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeInts(int[] array, File file, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(array, 0, array.length, file, byteOrder);
    }

    public static void storeInts(int[] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(array, new File(filename.toString()), byteOrder);
    }

    public static long loadInts(ReadableByteChannel channel, ByteOrder byteOrder, int[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadInts(channel, byteOrder, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadInts(ReadableByteChannel channel, ByteOrder byteOrder, int[][] array) throws IOException {
        return BinIO.loadInts(channel, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadInts(File file, ByteOrder byteOrder, int[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadInts((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static long loadInts(CharSequence filename, ByteOrder byteOrder, int[][] array, long offset, long length) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static long loadInts(File file, ByteOrder byteOrder, int[][] array) throws IOException {
        return BinIO.loadInts(file, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadInts(CharSequence filename, ByteOrder byteOrder, int[][] array) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), byteOrder, array);
    }

    public static int[][] loadIntsBig(File file, ByteOrder byteOrder) throws IOException {
        int[][] array;
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 4L;
        for (int[] t : array = IntBigArrays.newBigArray((long)length)) {
            BinIO.loadInts((ReadableByteChannel)channel, byteOrder, t);
        }
        channel.close();
        return array;
    }

    public static int[][] loadIntsBig(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadIntsBig(new File(filename.toString()), byteOrder);
    }

    public static void storeInts(int[][] array, long offset, long length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeInts(array[i], s, l - s, channel, byteOrder);
        }
    }

    public static void storeInts(int[][] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int[] t : array) {
            BinIO.storeInts(t, channel, byteOrder);
        }
    }

    public static void storeInts(int[][] array, long offset, long length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeInts(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeInts(int[][] array, long offset, long length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeInts(int[][] array, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeInts(array, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeInts(int[][] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(array, new File(filename.toString()), byteOrder);
    }

    public static void storeInts(IntIterator i, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        IntBuffer buffer = byteBuffer.asIntBuffer();
        while (i.hasNext()) {
            if (!buffer.hasRemaining()) {
                buffer.flip();
                byteBuffer.clear();
                byteBuffer.limit(buffer.limit() << IntMappedBigList.LOG2_BYTES);
                channel.write(byteBuffer);
                buffer.clear();
            }
            buffer.put(i.nextInt());
        }
        buffer.flip();
        byteBuffer.clear();
        byteBuffer.limit(buffer.limit() << IntMappedBigList.LOG2_BYTES);
        channel.write(byteBuffer);
    }

    public static void storeInts(IntIterator i, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeInts(i, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeInts(IntIterator i, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeInts(i, new File(filename.toString()), byteOrder);
    }

    public static IntIterator asIntIterator(ReadableByteChannel channel, ByteOrder byteOrder) {
        return new IntDataNioInputWrapper(channel, byteOrder);
    }

    public static IntIterator asIntIterator(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        return new IntDataNioInputWrapper(channel, byteOrder);
    }

    public static IntIterator asIntIterator(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.asIntIterator(new File(filename.toString()), byteOrder);
    }

    public static IntIterable asIntIterable(File file, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asIntIterator(file, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static IntIterable asIntIterable(CharSequence filename, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asIntIterator(filename, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadInts(DataInput dataInput, int[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readInt();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadInts(DataInput dataInput, int[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readInt();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadInts(File file, int[] array, int offset, int length) throws IOException {
        return BinIO.loadInts(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static int loadInts(CharSequence filename, int[] array, int offset, int length) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), array, offset, length);
    }

    public static int loadInts(File file, int[] array) throws IOException {
        return BinIO.loadInts(file, array, 0, array.length);
    }

    public static int loadInts(CharSequence filename, int[] array) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), array);
    }

    public static int[] loadInts(File file) throws IOException {
        return BinIO.loadInts(file, ByteOrder.BIG_ENDIAN);
    }

    public static int[] loadInts(CharSequence filename) throws IOException {
        return BinIO.loadInts(new File(filename.toString()));
    }

    public static void storeInts(int[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeInt(array[offset + i]);
        }
    }

    public static void storeInts(int[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeInt(array[i]);
        }
    }

    public static void storeInts(int[] array, int offset, int length, File file) throws IOException {
        BinIO.storeInts(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeInts(int[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeInts(array, offset, length, new File(filename.toString()));
    }

    public static void storeInts(int[] array, File file) throws IOException {
        BinIO.storeInts(array, 0, array.length, file);
    }

    public static void storeInts(int[] array, CharSequence filename) throws IOException {
        BinIO.storeInts(array, new File(filename.toString()));
    }

    public static long loadInts(DataInput dataInput, int[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                int[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readInt();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadInts(DataInput dataInput, int[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                int[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readInt();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadInts(File file, int[][] array, long offset, long length) throws IOException {
        return BinIO.loadInts(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static long loadInts(CharSequence filename, int[][] array, long offset, long length) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), array, offset, length);
    }

    public static long loadInts(File file, int[][] array) throws IOException {
        return BinIO.loadInts(file, ByteOrder.BIG_ENDIAN, array);
    }

    public static long loadInts(CharSequence filename, int[][] array) throws IOException {
        return BinIO.loadInts(new File(filename.toString()), array);
    }

    public static int[][] loadIntsBig(File file) throws IOException {
        return BinIO.loadIntsBig(file, ByteOrder.BIG_ENDIAN);
    }

    public static int[][] loadIntsBig(CharSequence filename) throws IOException {
        return BinIO.loadIntsBig(new File(filename.toString()));
    }

    public static void storeInts(int[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeInt(t[d]);
            }
        }
    }

    public static void storeInts(int[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            int[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeInt(t[d]);
            }
        }
    }

    public static void storeInts(int[][] array, long offset, long length, File file) throws IOException {
        BinIO.storeInts(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeInts(int[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeInts(array, offset, length, new File(filename.toString()));
    }

    public static void storeInts(int[][] array, File file) throws IOException {
        BinIO.storeInts(array, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeInts(int[][] array, CharSequence filename) throws IOException {
        BinIO.storeInts(array, new File(filename.toString()));
    }

    public static void storeInts(IntIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeInt(i.nextInt());
        }
    }

    public static void storeInts(IntIterator i, File file) throws IOException {
        BinIO.storeInts(i, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeInts(IntIterator i, CharSequence filename) throws IOException {
        BinIO.storeInts(i, new File(filename.toString()));
    }

    public static IntIterator asIntIterator(DataInput dataInput) {
        return new IntDataInputWrapper(dataInput);
    }

    public static IntIterator asIntIterator(File file) throws IOException {
        return BinIO.asIntIterator(file, ByteOrder.BIG_ENDIAN);
    }

    public static IntIterator asIntIterator(CharSequence filename) throws IOException {
        return BinIO.asIntIterator(new File(filename.toString()));
    }

    public static IntIterable asIntIterable(File file) {
        return () -> {
            try {
                return BinIO.asIntIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static IntIterable asIntIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asIntIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadFloats(ReadableByteChannel channel, ByteOrder byteOrder, float[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        FloatBuffer buffer = byteBuffer.asFloatBuffer();
        int read = 0;
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min((long)buffer.capacity(), (long)length << FloatMappedBigList.LOG2_BYTES));
            int r = channel.read(byteBuffer);
            if (r <= 0) {
                return read;
            }
            read += (r >>>= FloatMappedBigList.LOG2_BYTES);
            buffer.clear();
            buffer.limit(r);
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadFloats(ReadableByteChannel channel, ByteOrder byteOrder, float[] array) throws IOException {
        return BinIO.loadFloats(channel, byteOrder, array, 0, array.length);
    }

    public static int loadFloats(File file, ByteOrder byteOrder, float[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int read = BinIO.loadFloats((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static int loadFloats(CharSequence filename, ByteOrder byteOrder, float[] array, int offset, int length) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static int loadFloats(File file, ByteOrder byteOrder, float[] array) throws IOException {
        return BinIO.loadFloats(file, byteOrder, array, 0, array.length);
    }

    public static int loadFloats(CharSequence filename, ByteOrder byteOrder, float[] array) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), byteOrder, array);
    }

    public static float[] loadFloats(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 4L;
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        float[] array = new float[(int)length];
        if ((long)BinIO.loadFloats((ReadableByteChannel)channel, byteOrder, array) < length) {
            throw new EOFException();
        }
        channel.close();
        return array;
    }

    public static float[] loadFloats(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), byteOrder);
    }

    public static void storeFloats(float[] array, int offset, int length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        FloatBuffer buffer = byteBuffer.asFloatBuffer();
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            byteBuffer.clear();
            byteBuffer.limit(buffer.limit() << FloatMappedBigList.LOG2_BYTES);
            channel.write(byteBuffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeFloats(float[] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(array, 0, array.length, channel, byteOrder);
    }

    public static void storeFloats(float[] array, int offset, int length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeFloats(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeFloats(float[] array, int offset, int length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeFloats(float[] array, File file, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(array, 0, array.length, file, byteOrder);
    }

    public static void storeFloats(float[] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(array, new File(filename.toString()), byteOrder);
    }

    public static long loadFloats(ReadableByteChannel channel, ByteOrder byteOrder, float[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            float[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadFloats(channel, byteOrder, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadFloats(ReadableByteChannel channel, ByteOrder byteOrder, float[][] array) throws IOException {
        return BinIO.loadFloats(channel, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadFloats(File file, ByteOrder byteOrder, float[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadFloats((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static long loadFloats(CharSequence filename, ByteOrder byteOrder, float[][] array, long offset, long length) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static long loadFloats(File file, ByteOrder byteOrder, float[][] array) throws IOException {
        return BinIO.loadFloats(file, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadFloats(CharSequence filename, ByteOrder byteOrder, float[][] array) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), byteOrder, array);
    }

    public static float[][] loadFloatsBig(File file, ByteOrder byteOrder) throws IOException {
        float[][] array;
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 4L;
        for (float[] t : array = FloatBigArrays.newBigArray((long)length)) {
            BinIO.loadFloats((ReadableByteChannel)channel, byteOrder, t);
        }
        channel.close();
        return array;
    }

    public static float[][] loadFloatsBig(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadFloatsBig(new File(filename.toString()), byteOrder);
    }

    public static void storeFloats(float[][] array, long offset, long length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeFloats(array[i], s, l - s, channel, byteOrder);
        }
    }

    public static void storeFloats(float[][] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (float[] t : array) {
            BinIO.storeFloats(t, channel, byteOrder);
        }
    }

    public static void storeFloats(float[][] array, long offset, long length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeFloats(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeFloats(float[][] array, long offset, long length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeFloats(float[][] array, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeFloats(array, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeFloats(float[][] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(array, new File(filename.toString()), byteOrder);
    }

    public static void storeFloats(FloatIterator i, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        FloatBuffer buffer = byteBuffer.asFloatBuffer();
        while (i.hasNext()) {
            if (!buffer.hasRemaining()) {
                buffer.flip();
                byteBuffer.clear();
                byteBuffer.limit(buffer.limit() << FloatMappedBigList.LOG2_BYTES);
                channel.write(byteBuffer);
                buffer.clear();
            }
            buffer.put(i.nextFloat());
        }
        buffer.flip();
        byteBuffer.clear();
        byteBuffer.limit(buffer.limit() << FloatMappedBigList.LOG2_BYTES);
        channel.write(byteBuffer);
    }

    public static void storeFloats(FloatIterator i, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeFloats(i, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeFloats(FloatIterator i, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeFloats(i, new File(filename.toString()), byteOrder);
    }

    public static FloatIterator asFloatIterator(ReadableByteChannel channel, ByteOrder byteOrder) {
        return new FloatDataNioInputWrapper(channel, byteOrder);
    }

    public static FloatIterator asFloatIterator(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        return new FloatDataNioInputWrapper((ReadableByteChannel)channel, byteOrder);
    }

    public static FloatIterator asFloatIterator(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.asFloatIterator(new File(filename.toString()), byteOrder);
    }

    public static FloatIterable asFloatIterable(File file, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asFloatIterator(file, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static FloatIterable asFloatIterable(CharSequence filename, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asFloatIterator(filename, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadFloats(DataInput dataInput, float[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readFloat();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadFloats(DataInput dataInput, float[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readFloat();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadFloats(File file, float[] array, int offset, int length) throws IOException {
        return BinIO.loadFloats(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static int loadFloats(CharSequence filename, float[] array, int offset, int length) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), array, offset, length);
    }

    public static int loadFloats(File file, float[] array) throws IOException {
        return BinIO.loadFloats(file, array, 0, array.length);
    }

    public static int loadFloats(CharSequence filename, float[] array) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), array);
    }

    public static float[] loadFloats(File file) throws IOException {
        return BinIO.loadFloats(file, ByteOrder.BIG_ENDIAN);
    }

    public static float[] loadFloats(CharSequence filename) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()));
    }

    public static void storeFloats(float[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeFloat(array[offset + i]);
        }
    }

    public static void storeFloats(float[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeFloat(array[i]);
        }
    }

    public static void storeFloats(float[] array, int offset, int length, File file) throws IOException {
        BinIO.storeFloats(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeFloats(float[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeFloats(array, offset, length, new File(filename.toString()));
    }

    public static void storeFloats(float[] array, File file) throws IOException {
        BinIO.storeFloats(array, 0, array.length, file);
    }

    public static void storeFloats(float[] array, CharSequence filename) throws IOException {
        BinIO.storeFloats(array, new File(filename.toString()));
    }

    public static long loadFloats(DataInput dataInput, float[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                float[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readFloat();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadFloats(DataInput dataInput, float[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                float[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readFloat();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadFloats(File file, float[][] array, long offset, long length) throws IOException {
        return BinIO.loadFloats(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static long loadFloats(CharSequence filename, float[][] array, long offset, long length) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), array, offset, length);
    }

    public static long loadFloats(File file, float[][] array) throws IOException {
        return BinIO.loadFloats(file, ByteOrder.BIG_ENDIAN, array);
    }

    public static long loadFloats(CharSequence filename, float[][] array) throws IOException {
        return BinIO.loadFloats(new File(filename.toString()), array);
    }

    public static float[][] loadFloatsBig(File file) throws IOException {
        return BinIO.loadFloatsBig(file, ByteOrder.BIG_ENDIAN);
    }

    public static float[][] loadFloatsBig(CharSequence filename) throws IOException {
        return BinIO.loadFloatsBig(new File(filename.toString()));
    }

    public static void storeFloats(float[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            float[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeFloat(t[d]);
            }
        }
    }

    public static void storeFloats(float[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            float[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeFloat(t[d]);
            }
        }
    }

    public static void storeFloats(float[][] array, long offset, long length, File file) throws IOException {
        BinIO.storeFloats(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeFloats(float[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeFloats(array, offset, length, new File(filename.toString()));
    }

    public static void storeFloats(float[][] array, File file) throws IOException {
        BinIO.storeFloats(array, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeFloats(float[][] array, CharSequence filename) throws IOException {
        BinIO.storeFloats(array, new File(filename.toString()));
    }

    public static void storeFloats(FloatIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeFloat(i.nextFloat());
        }
    }

    public static void storeFloats(FloatIterator i, File file) throws IOException {
        BinIO.storeFloats(i, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeFloats(FloatIterator i, CharSequence filename) throws IOException {
        BinIO.storeFloats(i, new File(filename.toString()));
    }

    public static FloatIterator asFloatIterator(DataInput dataInput) {
        return new FloatDataInputWrapper(dataInput);
    }

    public static FloatIterator asFloatIterator(File file) throws IOException {
        return BinIO.asFloatIterator(file, ByteOrder.BIG_ENDIAN);
    }

    public static FloatIterator asFloatIterator(CharSequence filename) throws IOException {
        return BinIO.asFloatIterator(new File(filename.toString()));
    }

    public static FloatIterable asFloatIterable(File file) {
        return () -> {
            try {
                return BinIO.asFloatIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static FloatIterable asFloatIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asFloatIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadLongs(ReadableByteChannel channel, ByteOrder byteOrder, long[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        LongBuffer buffer = byteBuffer.asLongBuffer();
        int read = 0;
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min((long)buffer.capacity(), (long)length << LongMappedBigList.LOG2_BYTES));
            int r = channel.read(byteBuffer);
            if (r <= 0) {
                return read;
            }
            read += (r >>>= LongMappedBigList.LOG2_BYTES);
            buffer.clear();
            buffer.limit(r);
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadLongs(ReadableByteChannel channel, ByteOrder byteOrder, long[] array) throws IOException {
        return BinIO.loadLongs(channel, byteOrder, array, 0, array.length);
    }

    public static int loadLongs(File file, ByteOrder byteOrder, long[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int read = BinIO.loadLongs((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static int loadLongs(CharSequence filename, ByteOrder byteOrder, long[] array, int offset, int length) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static int loadLongs(File file, ByteOrder byteOrder, long[] array) throws IOException {
        return BinIO.loadLongs(file, byteOrder, array, 0, array.length);
    }

    public static int loadLongs(CharSequence filename, ByteOrder byteOrder, long[] array) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), byteOrder, array);
    }

    public static long[] loadLongs(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 8L;
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        long[] array = new long[(int)length];
        if ((long)BinIO.loadLongs((ReadableByteChannel)channel, byteOrder, array) < length) {
            throw new EOFException();
        }
        channel.close();
        return array;
    }

    public static long[] loadLongs(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), byteOrder);
    }

    public static void storeLongs(long[] array, int offset, int length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        LongBuffer buffer = byteBuffer.asLongBuffer();
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            byteBuffer.clear();
            byteBuffer.limit(buffer.limit() << LongMappedBigList.LOG2_BYTES);
            channel.write(byteBuffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeLongs(long[] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(array, 0, array.length, channel, byteOrder);
    }

    public static void storeLongs(long[] array, int offset, int length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeLongs(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeLongs(long[] array, int offset, int length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeLongs(long[] array, File file, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(array, 0, array.length, file, byteOrder);
    }

    public static void storeLongs(long[] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(array, new File(filename.toString()), byteOrder);
    }

    public static long loadLongs(ReadableByteChannel channel, ByteOrder byteOrder, long[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            long[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadLongs(channel, byteOrder, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadLongs(ReadableByteChannel channel, ByteOrder byteOrder, long[][] array) throws IOException {
        return BinIO.loadLongs(channel, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadLongs(File file, ByteOrder byteOrder, long[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadLongs((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static long loadLongs(CharSequence filename, ByteOrder byteOrder, long[][] array, long offset, long length) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static long loadLongs(File file, ByteOrder byteOrder, long[][] array) throws IOException {
        return BinIO.loadLongs(file, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadLongs(CharSequence filename, ByteOrder byteOrder, long[][] array) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), byteOrder, array);
    }

    public static long[][] loadLongsBig(File file, ByteOrder byteOrder) throws IOException {
        long[][] array;
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 8L;
        for (long[] t : array = LongBigArrays.newBigArray((long)length)) {
            BinIO.loadLongs((ReadableByteChannel)channel, byteOrder, t);
        }
        channel.close();
        return array;
    }

    public static long[][] loadLongsBig(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadLongsBig(new File(filename.toString()), byteOrder);
    }

    public static void storeLongs(long[][] array, long offset, long length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeLongs(array[i], s, l - s, channel, byteOrder);
        }
    }

    public static void storeLongs(long[][] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (long[] t : array) {
            BinIO.storeLongs(t, channel, byteOrder);
        }
    }

    public static void storeLongs(long[][] array, long offset, long length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeLongs(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeLongs(long[][] array, long offset, long length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeLongs(long[][] array, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeLongs(array, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeLongs(long[][] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(array, new File(filename.toString()), byteOrder);
    }

    public static void storeLongs(LongIterator i, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        LongBuffer buffer = byteBuffer.asLongBuffer();
        while (i.hasNext()) {
            if (!buffer.hasRemaining()) {
                buffer.flip();
                byteBuffer.clear();
                byteBuffer.limit(buffer.limit() << LongMappedBigList.LOG2_BYTES);
                channel.write(byteBuffer);
                buffer.clear();
            }
            buffer.put(i.nextLong());
        }
        buffer.flip();
        byteBuffer.clear();
        byteBuffer.limit(buffer.limit() << LongMappedBigList.LOG2_BYTES);
        channel.write(byteBuffer);
    }

    public static void storeLongs(LongIterator i, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeLongs(i, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeLongs(LongIterator i, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeLongs(i, new File(filename.toString()), byteOrder);
    }

    public static LongIterator asLongIterator(ReadableByteChannel channel, ByteOrder byteOrder) {
        return new LongDataNioInputWrapper(channel, byteOrder);
    }

    public static LongIterator asLongIterator(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        return new LongDataNioInputWrapper((ReadableByteChannel)channel, byteOrder);
    }

    public static LongIterator asLongIterator(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.asLongIterator(new File(filename.toString()), byteOrder);
    }

    public static LongIterable asLongIterable(File file, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asLongIterator(file, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static LongIterable asLongIterable(CharSequence filename, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asLongIterator(filename, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadLongs(DataInput dataInput, long[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readLong();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadLongs(DataInput dataInput, long[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readLong();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadLongs(File file, long[] array, int offset, int length) throws IOException {
        return BinIO.loadLongs(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static int loadLongs(CharSequence filename, long[] array, int offset, int length) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), array, offset, length);
    }

    public static int loadLongs(File file, long[] array) throws IOException {
        return BinIO.loadLongs(file, array, 0, array.length);
    }

    public static int loadLongs(CharSequence filename, long[] array) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), array);
    }

    public static long[] loadLongs(File file) throws IOException {
        return BinIO.loadLongs(file, ByteOrder.BIG_ENDIAN);
    }

    public static long[] loadLongs(CharSequence filename) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()));
    }

    public static void storeLongs(long[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeLong(array[offset + i]);
        }
    }

    public static void storeLongs(long[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeLong(array[i]);
        }
    }

    public static void storeLongs(long[] array, int offset, int length, File file) throws IOException {
        BinIO.storeLongs(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeLongs(long[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeLongs(array, offset, length, new File(filename.toString()));
    }

    public static void storeLongs(long[] array, File file) throws IOException {
        BinIO.storeLongs(array, 0, array.length, file);
    }

    public static void storeLongs(long[] array, CharSequence filename) throws IOException {
        BinIO.storeLongs(array, new File(filename.toString()));
    }

    public static long loadLongs(DataInput dataInput, long[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                long[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readLong();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadLongs(DataInput dataInput, long[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                long[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readLong();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadLongs(File file, long[][] array, long offset, long length) throws IOException {
        return BinIO.loadLongs(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static long loadLongs(CharSequence filename, long[][] array, long offset, long length) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), array, offset, length);
    }

    public static long loadLongs(File file, long[][] array) throws IOException {
        return BinIO.loadLongs(file, ByteOrder.BIG_ENDIAN, array);
    }

    public static long loadLongs(CharSequence filename, long[][] array) throws IOException {
        return BinIO.loadLongs(new File(filename.toString()), array);
    }

    public static long[][] loadLongsBig(File file) throws IOException {
        return BinIO.loadLongsBig(file, ByteOrder.BIG_ENDIAN);
    }

    public static long[][] loadLongsBig(CharSequence filename) throws IOException {
        return BinIO.loadLongsBig(new File(filename.toString()));
    }

    public static void storeLongs(long[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            long[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeLong(t[d]);
            }
        }
    }

    public static void storeLongs(long[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            long[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeLong(t[d]);
            }
        }
    }

    public static void storeLongs(long[][] array, long offset, long length, File file) throws IOException {
        BinIO.storeLongs(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeLongs(long[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeLongs(array, offset, length, new File(filename.toString()));
    }

    public static void storeLongs(long[][] array, File file) throws IOException {
        BinIO.storeLongs(array, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeLongs(long[][] array, CharSequence filename) throws IOException {
        BinIO.storeLongs(array, new File(filename.toString()));
    }

    public static void storeLongs(LongIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeLong(i.nextLong());
        }
    }

    public static void storeLongs(LongIterator i, File file) throws IOException {
        BinIO.storeLongs(i, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeLongs(LongIterator i, CharSequence filename) throws IOException {
        BinIO.storeLongs(i, new File(filename.toString()));
    }

    public static LongIterator asLongIterator(DataInput dataInput) {
        return new LongDataInputWrapper(dataInput);
    }

    public static LongIterator asLongIterator(File file) throws IOException {
        return BinIO.asLongIterator(file, ByteOrder.BIG_ENDIAN);
    }

    public static LongIterator asLongIterator(CharSequence filename) throws IOException {
        return BinIO.asLongIterator(new File(filename.toString()));
    }

    public static LongIterable asLongIterable(File file) {
        return () -> {
            try {
                return BinIO.asLongIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static LongIterable asLongIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asLongIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadDoubles(ReadableByteChannel channel, ByteOrder byteOrder, double[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        DoubleBuffer buffer = byteBuffer.asDoubleBuffer();
        int read = 0;
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min((long)buffer.capacity(), (long)length << DoubleMappedBigList.LOG2_BYTES));
            int r = channel.read(byteBuffer);
            if (r <= 0) {
                return read;
            }
            read += (r >>>= DoubleMappedBigList.LOG2_BYTES);
            buffer.clear();
            buffer.limit(r);
            buffer.get(array, offset, r);
            offset += r;
            length -= r;
        }
    }

    public static int loadDoubles(ReadableByteChannel channel, ByteOrder byteOrder, double[] array) throws IOException {
        return BinIO.loadDoubles(channel, byteOrder, array, 0, array.length);
    }

    public static int loadDoubles(File file, ByteOrder byteOrder, double[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        int read = BinIO.loadDoubles((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static int loadDoubles(CharSequence filename, ByteOrder byteOrder, double[] array, int offset, int length) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static int loadDoubles(File file, ByteOrder byteOrder, double[] array) throws IOException {
        return BinIO.loadDoubles(file, byteOrder, array, 0, array.length);
    }

    public static int loadDoubles(CharSequence filename, ByteOrder byteOrder, double[] array) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), byteOrder, array);
    }

    public static double[] loadDoubles(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 8L;
        if (length > Integer.MAX_VALUE) {
            channel.close();
            throw new IllegalArgumentException("File too long: " + channel.size() + " bytes (" + length + " elements)");
        }
        double[] array = new double[(int)length];
        if ((long)BinIO.loadDoubles((ReadableByteChannel)channel, byteOrder, array) < length) {
            throw new EOFException();
        }
        channel.close();
        return array;
    }

    public static double[] loadDoubles(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), byteOrder);
    }

    public static void storeDoubles(double[] array, int offset, int length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        DoubleBuffer buffer = byteBuffer.asDoubleBuffer();
        while (length != 0) {
            int l = Math.min(length, buffer.capacity());
            buffer.clear();
            buffer.put(array, offset, l);
            buffer.flip();
            byteBuffer.clear();
            byteBuffer.limit(buffer.limit() << DoubleMappedBigList.LOG2_BYTES);
            channel.write(byteBuffer);
            offset += l;
            length -= l;
        }
    }

    public static void storeDoubles(double[] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(array, 0, array.length, channel, byteOrder);
    }

    public static void storeDoubles(double[] array, int offset, int length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeDoubles(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeDoubles(double[] array, int offset, int length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeDoubles(double[] array, File file, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(array, 0, array.length, file, byteOrder);
    }

    public static void storeDoubles(double[] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(array, new File(filename.toString()), byteOrder);
    }

    public static long loadDoubles(ReadableByteChannel channel, ByteOrder byteOrder, double[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long read = 0L;
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            double[] t = array[i];
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int e = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            int r = BinIO.loadDoubles(channel, byteOrder, t, s, e - s);
            read += (long)r;
            if (r < e - s) break;
        }
        return read;
    }

    public static long loadDoubles(ReadableByteChannel channel, ByteOrder byteOrder, double[][] array) throws IOException {
        return BinIO.loadDoubles(channel, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadDoubles(File file, ByteOrder byteOrder, double[][] array, long offset, long length) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long read = BinIO.loadDoubles((ReadableByteChannel)channel, byteOrder, array, offset, length);
        channel.close();
        return read;
    }

    public static long loadDoubles(CharSequence filename, ByteOrder byteOrder, double[][] array, long offset, long length) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), byteOrder, array, offset, length);
    }

    public static long loadDoubles(File file, ByteOrder byteOrder, double[][] array) throws IOException {
        return BinIO.loadDoubles(file, byteOrder, array, 0L, BigArrays.length(array));
    }

    public static long loadDoubles(CharSequence filename, ByteOrder byteOrder, double[][] array) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), byteOrder, array);
    }

    public static double[][] loadDoublesBig(File file, ByteOrder byteOrder) throws IOException {
        double[][] array;
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        long length = channel.size() / 8L;
        for (double[] t : array = DoubleBigArrays.newBigArray((long)length)) {
            BinIO.loadDoubles((ReadableByteChannel)channel, byteOrder, t);
        }
        channel.close();
        return array;
    }

    public static double[][] loadDoublesBig(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.loadDoublesBig(new File(filename.toString()), byteOrder);
    }

    public static void storeDoubles(double[][] array, long offset, long length, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            int s = (int)Math.max(0L, offset - BigArrays.start(i));
            int l = (int)Math.min((long)array[i].length, offset + length - BigArrays.start(i));
            BinIO.storeDoubles(array[i], s, l - s, channel, byteOrder);
        }
    }

    public static void storeDoubles(double[][] array, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        for (double[] t : array) {
            BinIO.storeDoubles(t, channel, byteOrder);
        }
    }

    public static void storeDoubles(double[][] array, long offset, long length, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeDoubles(array, offset, length, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeDoubles(double[][] array, long offset, long length, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(array, offset, length, new File(filename.toString()), byteOrder);
    }

    public static void storeDoubles(double[][] array, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeDoubles(array, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeDoubles(double[][] array, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(array, new File(filename.toString()), byteOrder);
    }

    public static void storeDoubles(DoubleIterator i, WritableByteChannel channel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        DoubleBuffer buffer = byteBuffer.asDoubleBuffer();
        while (i.hasNext()) {
            if (!buffer.hasRemaining()) {
                buffer.flip();
                byteBuffer.clear();
                byteBuffer.limit(buffer.limit() << DoubleMappedBigList.LOG2_BYTES);
                channel.write(byteBuffer);
                buffer.clear();
            }
            buffer.put(i.nextDouble());
        }
        buffer.flip();
        byteBuffer.clear();
        byteBuffer.limit(buffer.limit() << DoubleMappedBigList.LOG2_BYTES);
        channel.write(byteBuffer);
    }

    public static void storeDoubles(DoubleIterator i, File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BinIO.storeDoubles(i, (WritableByteChannel)channel, byteOrder);
        channel.close();
    }

    public static void storeDoubles(DoubleIterator i, CharSequence filename, ByteOrder byteOrder) throws IOException {
        BinIO.storeDoubles(i, new File(filename.toString()), byteOrder);
    }

    public static DoubleIterator asDoubleIterator(ReadableByteChannel channel, ByteOrder byteOrder) {
        return new DoubleDataNioInputWrapper(channel, byteOrder);
    }

    public static DoubleIterator asDoubleIterator(File file, ByteOrder byteOrder) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), new OpenOption[0]);
        return new DoubleDataNioInputWrapper((ReadableByteChannel)channel, byteOrder);
    }

    public static DoubleIterator asDoubleIterator(CharSequence filename, ByteOrder byteOrder) throws IOException {
        return BinIO.asDoubleIterator(new File(filename.toString()), byteOrder);
    }

    public static DoubleIterable asDoubleIterable(File file, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asDoubleIterator(file, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static DoubleIterable asDoubleIterable(CharSequence filename, ByteOrder byteOrder) {
        return () -> {
            try {
                return BinIO.asDoubleIterator(filename, byteOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static int loadDoubles(DataInput dataInput, double[] array, int offset, int length) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        int i = 0;
        try {
            for (i = 0; i < length; ++i) {
                array[i + offset] = dataInput.readDouble();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadDoubles(DataInput dataInput, double[] array) throws IOException {
        int i = 0;
        try {
            int length = array.length;
            for (i = 0; i < length; ++i) {
                array[i] = dataInput.readDouble();
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return i;
    }

    public static int loadDoubles(File file, double[] array, int offset, int length) throws IOException {
        return BinIO.loadDoubles(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static int loadDoubles(CharSequence filename, double[] array, int offset, int length) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), array, offset, length);
    }

    public static int loadDoubles(File file, double[] array) throws IOException {
        return BinIO.loadDoubles(file, array, 0, array.length);
    }

    public static int loadDoubles(CharSequence filename, double[] array) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), array);
    }

    public static double[] loadDoubles(File file) throws IOException {
        return BinIO.loadDoubles(file, ByteOrder.BIG_ENDIAN);
    }

    public static double[] loadDoubles(CharSequence filename) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()));
    }

    public static void storeDoubles(double[] array, int offset, int length, DataOutput dataOutput) throws IOException {
        Arrays.ensureOffsetLength(array.length, offset, length);
        for (int i = 0; i < length; ++i) {
            dataOutput.writeDouble(array[offset + i]);
        }
    }

    public static void storeDoubles(double[] array, DataOutput dataOutput) throws IOException {
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            dataOutput.writeDouble(array[i]);
        }
    }

    public static void storeDoubles(double[] array, int offset, int length, File file) throws IOException {
        BinIO.storeDoubles(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeDoubles(double[] array, int offset, int length, CharSequence filename) throws IOException {
        BinIO.storeDoubles(array, offset, length, new File(filename.toString()));
    }

    public static void storeDoubles(double[] array, File file) throws IOException {
        BinIO.storeDoubles(array, 0, array.length, file);
    }

    public static void storeDoubles(double[] array, CharSequence filename) throws IOException {
        BinIO.storeDoubles(array, new File(filename.toString()));
    }

    public static long loadDoubles(DataInput dataInput, double[][] array, long offset, long length) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        long c = 0L;
        try {
            for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
                double[] t = array[i];
                int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
                for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                    t[d] = dataInput.readDouble();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadDoubles(DataInput dataInput, double[][] array) throws IOException {
        long c = 0L;
        try {
            for (int i = 0; i < array.length; ++i) {
                double[] t = array[i];
                int l = t.length;
                for (int d = 0; d < l; ++d) {
                    t[d] = dataInput.readDouble();
                    ++c;
                }
            }
        } catch (EOFException eOFException) {
            // empty catch block
        }
        return c;
    }

    public static long loadDoubles(File file, double[][] array, long offset, long length) throws IOException {
        return BinIO.loadDoubles(file, ByteOrder.BIG_ENDIAN, array, offset, length);
    }

    public static long loadDoubles(CharSequence filename, double[][] array, long offset, long length) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), array, offset, length);
    }

    public static long loadDoubles(File file, double[][] array) throws IOException {
        return BinIO.loadDoubles(file, ByteOrder.BIG_ENDIAN, array);
    }

    public static long loadDoubles(CharSequence filename, double[][] array) throws IOException {
        return BinIO.loadDoubles(new File(filename.toString()), array);
    }

    public static double[][] loadDoublesBig(File file) throws IOException {
        return BinIO.loadDoublesBig(file, ByteOrder.BIG_ENDIAN);
    }

    public static double[][] loadDoublesBig(CharSequence filename) throws IOException {
        return BinIO.loadDoublesBig(new File(filename.toString()));
    }

    public static void storeDoubles(double[][] array, long offset, long length, DataOutput dataOutput) throws IOException {
        BigArrays.ensureOffsetLength(array, offset, length);
        for (int i = BigArrays.segment(offset); i < BigArrays.segment(offset + length + 0x7FFFFFFL); ++i) {
            double[] t = array[i];
            int l = (int)Math.min((long)t.length, offset + length - BigArrays.start(i));
            for (int d = (int)Math.max(0L, offset - BigArrays.start(i)); d < l; ++d) {
                dataOutput.writeDouble(t[d]);
            }
        }
    }

    public static void storeDoubles(double[][] array, DataOutput dataOutput) throws IOException {
        for (int i = 0; i < array.length; ++i) {
            double[] t = array[i];
            int l = t.length;
            for (int d = 0; d < l; ++d) {
                dataOutput.writeDouble(t[d]);
            }
        }
    }

    public static void storeDoubles(double[][] array, long offset, long length, File file) throws IOException {
        BinIO.storeDoubles(array, offset, length, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeDoubles(double[][] array, long offset, long length, CharSequence filename) throws IOException {
        BinIO.storeDoubles(array, offset, length, new File(filename.toString()));
    }

    public static void storeDoubles(double[][] array, File file) throws IOException {
        BinIO.storeDoubles(array, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeDoubles(double[][] array, CharSequence filename) throws IOException {
        BinIO.storeDoubles(array, new File(filename.toString()));
    }

    public static void storeDoubles(DoubleIterator i, DataOutput dataOutput) throws IOException {
        while (i.hasNext()) {
            dataOutput.writeDouble(i.nextDouble());
        }
    }

    public static void storeDoubles(DoubleIterator i, File file) throws IOException {
        BinIO.storeDoubles(i, file, ByteOrder.BIG_ENDIAN);
    }

    public static void storeDoubles(DoubleIterator i, CharSequence filename) throws IOException {
        BinIO.storeDoubles(i, new File(filename.toString()));
    }

    public static DoubleIterator asDoubleIterator(DataInput dataInput) {
        return new DoubleDataInputWrapper(dataInput);
    }

    public static DoubleIterator asDoubleIterator(File file) throws IOException {
        return BinIO.asDoubleIterator(file, ByteOrder.BIG_ENDIAN);
    }

    public static DoubleIterator asDoubleIterator(CharSequence filename) throws IOException {
        return BinIO.asDoubleIterator(new File(filename.toString()));
    }

    public static DoubleIterable asDoubleIterable(File file) {
        return () -> {
            try {
                return BinIO.asDoubleIterator(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static DoubleIterable asDoubleIterable(CharSequence filename) {
        return () -> {
            try {
                return BinIO.asDoubleIterator(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static final class IntDataNioInputWrapper
    implements IntIterator {
        private final ReadableByteChannel channel;
        private final ByteBuffer byteBuffer;
        private final IntBuffer buffer;

        public IntDataNioInputWrapper(ReadableByteChannel channel, ByteOrder byteOrder) {
            this.channel = channel;
            this.byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
            this.buffer = this.byteBuffer.asIntBuffer();
            this.buffer.clear().flip();
        }

        @Override
        public boolean hasNext() {
            if (!this.buffer.hasRemaining()) {
                this.byteBuffer.clear();
                try {
                    this.channel.read(this.byteBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.byteBuffer.flip();
                this.buffer.clear();
                this.buffer.limit(this.byteBuffer.limit() >>> IntMappedBigList.LOG2_BYTES);
            }
            return this.buffer.hasRemaining();
        }

        @Override
        public int nextInt() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            return this.buffer.get();
        }
    }

    private static final class IntDataInputWrapper
    implements IntIterator {
        private final DataInput dataInput;
        private boolean toAdvance = true;
        private boolean endOfProcess = false;
        private int next;

        public IntDataInputWrapper(DataInput dataInput) {
            this.dataInput = dataInput;
        }

        @Override
        public boolean hasNext() {
            if (!this.toAdvance) {
                return !this.endOfProcess;
            }
            this.toAdvance = false;
            try {
                this.next = this.dataInput.readInt();
            } catch (EOFException eof) {
                this.endOfProcess = true;
            } catch (IOException rethrow) {
                throw new RuntimeException(rethrow);
            }
            return !this.endOfProcess;
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

