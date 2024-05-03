/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.PatternProcessor;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.NullOutputStream;

public class RollingRandomAccessFileManager
extends RollingFileManager {
    public static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final RollingRandomAccessFileManagerFactory FACTORY = new RollingRandomAccessFileManagerFactory();
    private RandomAccessFile randomAccessFile;

    @Deprecated
    public RollingRandomAccessFileManager(LoggerContext loggerContext, RandomAccessFile raf, String fileName, String pattern, OutputStream os, boolean append, boolean immediateFlush, int bufferSize, long size, long time, TriggeringPolicy policy, RolloverStrategy strategy, String advertiseURI, Layout<? extends Serializable> layout, boolean writeHeader) {
        this(loggerContext, raf, fileName, pattern, os, append, immediateFlush, bufferSize, size, time, policy, strategy, advertiseURI, layout, null, null, null, writeHeader);
    }

    public RollingRandomAccessFileManager(LoggerContext loggerContext, RandomAccessFile raf, String fileName, String pattern, OutputStream os, boolean append, boolean immediateFlush, int bufferSize, long size, long initialTime, TriggeringPolicy policy, RolloverStrategy strategy, String advertiseURI, Layout<? extends Serializable> layout, String filePermissions, String fileOwner, String fileGroup, boolean writeHeader) {
        super(loggerContext, fileName, pattern, os, append, false, size, initialTime, policy, strategy, advertiseURI, layout, filePermissions, fileOwner, fileGroup, writeHeader, ByteBuffer.wrap(new byte[bufferSize]));
        this.randomAccessFile = raf;
        this.writeHeader();
    }

    private void writeHeader() {
        if (this.layout == null) {
            return;
        }
        byte[] header = this.layout.getHeader();
        if (header == null) {
            return;
        }
        try {
            if (this.randomAccessFile != null && this.randomAccessFile.length() == 0L) {
                this.randomAccessFile.write(header, 0, header.length);
            }
        } catch (IOException e) {
            this.logError("Unable to write header", e);
        }
    }

    public static RollingRandomAccessFileManager getRollingRandomAccessFileManager(String fileName, String filePattern, boolean isAppend, boolean immediateFlush, int bufferSize, TriggeringPolicy policy, RolloverStrategy strategy, String advertiseURI, Layout<? extends Serializable> layout, String filePermissions, String fileOwner, String fileGroup, Configuration configuration) {
        if (strategy instanceof DirectWriteRolloverStrategy && fileName != null) {
            LOGGER.error("The fileName attribute must not be specified with the DirectWriteRolloverStrategy");
            return null;
        }
        String name = fileName == null ? filePattern : fileName;
        return RollingRandomAccessFileManager.narrow(RollingRandomAccessFileManager.class, RollingRandomAccessFileManager.getManager(name, new FactoryData(fileName, filePattern, isAppend, immediateFlush, bufferSize, policy, strategy, advertiseURI, layout, filePermissions, fileOwner, fileGroup, configuration), FACTORY));
    }

    @Deprecated
    public Boolean isEndOfBatch() {
        return Boolean.FALSE;
    }

    @Deprecated
    public void setEndOfBatch(boolean endOfBatch) {
    }

    @Override
    protected synchronized void write(byte[] bytes, int offset, int length, boolean immediateFlush) {
        super.write(bytes, offset, length, immediateFlush);
    }

    @Override
    protected synchronized void writeToDestination(byte[] bytes, int offset, int length) {
        try {
            if (this.randomAccessFile == null) {
                String fileName = this.getFileName();
                File file = new File(fileName);
                FileUtils.makeParentDirs(file);
                this.createFileAfterRollover(fileName);
            }
            this.randomAccessFile.write(bytes, offset, length);
            this.size += (long)length;
        } catch (IOException ex) {
            String msg = "Error writing to RandomAccessFile " + this.getName();
            throw new AppenderLoggingException(msg, ex);
        }
    }

    @Override
    protected void createFileAfterRollover() throws IOException {
        this.createFileAfterRollover(this.getFileName());
    }

    private void createFileAfterRollover(String fileName) throws IOException {
        this.randomAccessFile = new RandomAccessFile(fileName, "rw");
        if (this.isAttributeViewEnabled()) {
            this.defineAttributeView(Paths.get(fileName, new String[0]));
        }
        if (this.isAppend()) {
            this.randomAccessFile.seek(this.randomAccessFile.length());
        }
        this.writeHeader();
    }

    @Override
    public synchronized void flush() {
        this.flushBuffer(this.byteBuffer);
    }

    @Override
    public synchronized boolean closeOutputStream() {
        this.flush();
        if (this.randomAccessFile != null) {
            try {
                this.randomAccessFile.close();
                return true;
            } catch (IOException e) {
                this.logError("Unable to close RandomAccessFile", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public int getBufferSize() {
        return this.byteBuffer.capacity();
    }

    @Override
    public void updateData(Object data) {
        FactoryData factoryData = (FactoryData)data;
        this.setRolloverStrategy(factoryData.getRolloverStrategy());
        this.setPatternProcessor(new PatternProcessor(factoryData.getPattern(), this.getPatternProcessor()));
        this.setTriggeringPolicy(factoryData.getTriggeringPolicy());
    }

    private static class FactoryData
    extends ConfigurationFactoryData {
        private final String fileName;
        private final String pattern;
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        private final String filePermissions;
        private final String fileOwner;
        private final String fileGroup;

        public FactoryData(String fileName, String pattern, boolean append, boolean immediateFlush, int bufferSize, TriggeringPolicy policy, RolloverStrategy strategy, String advertiseURI, Layout<? extends Serializable> layout, String filePermissions, String fileOwner, String fileGroup, Configuration configuration) {
            super(configuration);
            this.fileName = fileName;
            this.pattern = pattern;
            this.append = append;
            this.immediateFlush = immediateFlush;
            this.bufferSize = bufferSize;
            this.policy = policy;
            this.strategy = strategy;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
            this.filePermissions = filePermissions;
            this.fileOwner = fileOwner;
            this.fileGroup = fileGroup;
        }

        public String getPattern() {
            return this.pattern;
        }

        public TriggeringPolicy getTriggeringPolicy() {
            return this.policy;
        }

        public RolloverStrategy getRolloverStrategy() {
            return this.strategy;
        }
    }

    private static class RollingRandomAccessFileManagerFactory
    implements ManagerFactory<RollingRandomAccessFileManager, FactoryData> {
        private RollingRandomAccessFileManagerFactory() {
        }

        @Override
        public RollingRandomAccessFileManager createManager(String name, FactoryData data) {
            File file = null;
            long size = 0L;
            long time = System.currentTimeMillis();
            RandomAccessFile raf = null;
            if (data.fileName != null) {
                file = new File(name);
                if (!data.append) {
                    file.delete();
                }
                long l = size = data.append ? file.length() : 0L;
                if (file.exists()) {
                    time = file.lastModified();
                }
                try {
                    FileUtils.makeParentDirs(file);
                    raf = new RandomAccessFile(name, "rw");
                    if (data.append) {
                        long length = raf.length();
                        LOGGER.trace("RandomAccessFile {} seek to {}", (Object)name, (Object)length);
                        raf.seek(length);
                    } else {
                        LOGGER.trace("RandomAccessFile {} set length to 0", (Object)name);
                        raf.setLength(0L);
                    }
                } catch (IOException ex) {
                    LOGGER.error("Cannot access RandomAccessFile " + ex, (Throwable)ex);
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            LOGGER.error("Cannot close RandomAccessFile {}", (Object)name, (Object)e);
                        }
                    }
                    return null;
                }
            }
            boolean writeHeader = !data.append || file == null || !file.exists();
            RollingRandomAccessFileManager rrm = new RollingRandomAccessFileManager(data.getLoggerContext(), raf, name, data.pattern, NullOutputStream.getInstance(), data.append, data.immediateFlush, data.bufferSize, size, time, data.policy, data.strategy, data.advertiseURI, data.layout, data.filePermissions, data.fileOwner, data.fileGroup, writeHeader);
            if (rrm.isAttributeViewEnabled()) {
                rrm.defineAttributeView(file.toPath());
            }
            return rrm;
        }
    }
}

