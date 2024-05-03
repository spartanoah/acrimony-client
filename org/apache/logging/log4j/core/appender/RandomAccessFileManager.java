/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.NullOutputStream;

public class RandomAccessFileManager
extends OutputStreamManager {
    static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final RandomAccessFileManagerFactory FACTORY = new RandomAccessFileManagerFactory();
    private final String advertiseURI;
    private final RandomAccessFile randomAccessFile;

    protected RandomAccessFileManager(LoggerContext loggerContext, RandomAccessFile file, String fileName, OutputStream os, int bufferSize, String advertiseURI, Layout<? extends Serializable> layout, boolean writeHeader) {
        super(loggerContext, os, fileName, false, layout, writeHeader, ByteBuffer.wrap(new byte[bufferSize]));
        this.randomAccessFile = file;
        this.advertiseURI = advertiseURI;
    }

    public static RandomAccessFileManager getFileManager(String fileName, boolean append, boolean immediateFlush, int bufferSize, String advertiseURI, Layout<? extends Serializable> layout, Configuration configuration) {
        return RandomAccessFileManager.narrow(RandomAccessFileManager.class, RandomAccessFileManager.getManager(fileName, new FactoryData(append, immediateFlush, bufferSize, advertiseURI, layout, configuration), FACTORY));
    }

    @Deprecated
    public Boolean isEndOfBatch() {
        return Boolean.FALSE;
    }

    @Deprecated
    public void setEndOfBatch(boolean endOfBatch) {
    }

    @Override
    protected void writeToDestination(byte[] bytes, int offset, int length) {
        try {
            this.randomAccessFile.write(bytes, offset, length);
        } catch (IOException ex) {
            String msg = "Error writing to RandomAccessFile " + this.getName();
            throw new AppenderLoggingException(msg, ex);
        }
    }

    @Override
    public synchronized void flush() {
        this.flushBuffer(this.byteBuffer);
    }

    @Override
    public synchronized boolean closeOutputStream() {
        this.flush();
        try {
            this.randomAccessFile.close();
            return true;
        } catch (IOException ex) {
            this.logError("Unable to close RandomAccessFile", ex);
            return false;
        }
    }

    public String getFileName() {
        return this.getName();
    }

    public int getBufferSize() {
        return this.byteBuffer.capacity();
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> result = new HashMap<String, String>(super.getContentFormat());
        result.put("fileURI", this.advertiseURI);
        return result;
    }

    private static class RandomAccessFileManagerFactory
    implements ManagerFactory<RandomAccessFileManager, FactoryData> {
        private RandomAccessFileManagerFactory() {
        }

        @Override
        public RandomAccessFileManager createManager(String name, FactoryData data) {
            File file = new File(name);
            if (!data.append) {
                file.delete();
            }
            boolean writeHeader = !data.append || !file.exists();
            NullOutputStream os = NullOutputStream.getInstance();
            try {
                FileUtils.makeParentDirs(file);
                RandomAccessFile raf = new RandomAccessFile(name, "rw");
                if (data.append) {
                    raf.seek(raf.length());
                } else {
                    raf.setLength(0L);
                }
                return new RandomAccessFileManager(data.getLoggerContext(), raf, name, os, data.bufferSize, data.advertiseURI, data.layout, writeHeader);
            } catch (Exception ex) {
                AbstractManager.LOGGER.error("RandomAccessFileManager (" + name + ") " + ex, (Throwable)ex);
                return null;
            }
        }
    }

    private static class FactoryData
    extends ConfigurationFactoryData {
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;

        public FactoryData(boolean append, boolean immediateFlush, int bufferSize, String advertiseURI, Layout<? extends Serializable> layout, Configuration configuration) {
            super(configuration);
            this.append = append;
            this.immediateFlush = immediateFlush;
            this.bufferSize = bufferSize;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
        }
    }
}

