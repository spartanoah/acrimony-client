/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.appender.rolling.AbstractRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectFileRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.apache.logging.log4j.core.appender.rolling.PatternProcessor;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescriptionImpl;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.CompositeAction;
import org.apache.logging.log4j.core.appender.rolling.action.FileRenameAction;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PosixViewAttributeAction;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="DirectWriteRolloverStrategy", category="Core", printObject=true)
public class DirectWriteRolloverStrategy
extends AbstractRolloverStrategy
implements DirectFileRolloverStrategy {
    private static final int DEFAULT_MAX_FILES = 7;
    private final int maxFiles;
    private final int compressionLevel;
    private final List<Action> customActions;
    private final boolean stopCustomActionsOnError;
    private volatile String currentFileName;
    private int nextIndex = -1;
    private final PatternProcessor tempCompressedFilePattern;
    private volatile boolean usePrevTime = false;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Deprecated
    @PluginFactory
    public static DirectWriteRolloverStrategy createStrategy(@PluginAttribute(value="maxFiles") String maxFiles, @PluginAttribute(value="compressionLevel") String compressionLevelStr, @PluginElement(value="Actions") Action[] customActions, @PluginAttribute(value="stopCustomActionsOnError", defaultBoolean=true) boolean stopCustomActionsOnError, @PluginConfiguration Configuration config) {
        return DirectWriteRolloverStrategy.newBuilder().withMaxFiles(maxFiles).withCompressionLevelStr(compressionLevelStr).withCustomActions(customActions).withStopCustomActionsOnError(stopCustomActionsOnError).withConfig(config).build();
    }

    @Deprecated
    protected DirectWriteRolloverStrategy(int maxFiles, int compressionLevel, StrSubstitutor strSubstitutor, Action[] customActions, boolean stopCustomActionsOnError) {
        this(maxFiles, compressionLevel, strSubstitutor, customActions, stopCustomActionsOnError, null);
    }

    protected DirectWriteRolloverStrategy(int maxFiles, int compressionLevel, StrSubstitutor strSubstitutor, Action[] customActions, boolean stopCustomActionsOnError, String tempCompressedFilePatternString) {
        super(strSubstitutor);
        this.maxFiles = maxFiles;
        this.compressionLevel = compressionLevel;
        this.stopCustomActionsOnError = stopCustomActionsOnError;
        this.customActions = customActions == null ? Collections.emptyList() : Arrays.asList(customActions);
        this.tempCompressedFilePattern = tempCompressedFilePatternString != null ? new PatternProcessor(tempCompressedFilePatternString) : null;
    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public List<Action> getCustomActions() {
        return this.customActions;
    }

    public int getMaxFiles() {
        return this.maxFiles;
    }

    public boolean isStopCustomActionsOnError() {
        return this.stopCustomActionsOnError;
    }

    public PatternProcessor getTempCompressedFilePattern() {
        return this.tempCompressedFilePattern;
    }

    private int purge(RollingFileManager manager) {
        SortedMap<Integer, Path> eligibleFiles = this.getEligibleFiles(manager);
        LOGGER.debug("Found {} eligible files, max is  {}", (Object)eligibleFiles.size(), (Object)this.maxFiles);
        while (eligibleFiles.size() >= this.maxFiles) {
            try {
                Integer key = eligibleFiles.firstKey();
                Files.delete((Path)eligibleFiles.get(key));
                eligibleFiles.remove(key);
            } catch (IOException ioe) {
                LOGGER.error("Unable to delete {}", (Object)eligibleFiles.firstKey(), (Object)ioe);
                break;
            }
        }
        return eligibleFiles.size() > 0 ? eligibleFiles.lastKey() : 1;
    }

    @Override
    public String getCurrentFileName(RollingFileManager manager) {
        if (this.currentFileName == null) {
            String name;
            SortedMap<Integer, Path> eligibleFiles = this.getEligibleFiles(manager);
            int fileIndex = eligibleFiles.size() > 0 ? (this.nextIndex > 0 ? this.nextIndex : eligibleFiles.lastKey()) : 1;
            StringBuilder buf = new StringBuilder(255);
            manager.getPatternProcessor().setCurrentFileTime(System.currentTimeMillis());
            manager.getPatternProcessor().formatFileName(this.strSubstitutor, buf, true, (Object)fileIndex);
            int suffixLength = this.suffixLength(buf.toString());
            this.currentFileName = name = suffixLength > 0 ? buf.substring(0, buf.length() - suffixLength) : buf.toString();
        }
        return this.currentFileName;
    }

    @Override
    public void clearCurrentFileName() {
        this.currentFileName = null;
    }

    @Override
    public RolloverDescription rollover(RollingFileManager manager) throws SecurityException {
        String sourceName;
        LOGGER.debug("Rolling " + this.currentFileName);
        if (this.maxFiles < 0) {
            return null;
        }
        long startNanos = System.nanoTime();
        int fileIndex = this.purge(manager);
        if (LOGGER.isTraceEnabled()) {
            double durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            LOGGER.trace("DirectWriteRolloverStrategy.purge() took {} milliseconds", (Object)durationMillis);
        }
        Action compressAction = null;
        String compressedName = sourceName = this.getCurrentFileName(manager);
        this.currentFileName = null;
        this.nextIndex = fileIndex + 1;
        FileExtension fileExtension = manager.getFileExtension();
        if (fileExtension != null) {
            compressedName = compressedName + fileExtension.getExtension();
            if (this.tempCompressedFilePattern != null) {
                StringBuilder buf = new StringBuilder();
                this.tempCompressedFilePattern.formatFileName(this.strSubstitutor, buf, (Object)fileIndex);
                String tmpCompressedName = buf.toString();
                File tmpCompressedNameFile = new File(tmpCompressedName);
                File parentFile = tmpCompressedNameFile.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                compressAction = new CompositeAction(Arrays.asList(fileExtension.createCompressAction(sourceName, tmpCompressedName, true, this.compressionLevel), new FileRenameAction(tmpCompressedNameFile, new File(compressedName), true)), true);
            } else {
                compressAction = fileExtension.createCompressAction(sourceName, compressedName, true, this.compressionLevel);
            }
        }
        if (compressAction != null && manager.isAttributeViewEnabled()) {
            PosixViewAttributeAction posixAttributeViewAction = PosixViewAttributeAction.newBuilder().withBasePath(compressedName).withFollowLinks(false).withMaxDepth(1).withPathConditions(PathCondition.EMPTY_ARRAY).withSubst(this.getStrSubstitutor()).withFilePermissions(manager.getFilePermissions()).withFileOwner(manager.getFileOwner()).withFileGroup(manager.getFileGroup()).build();
            compressAction = new CompositeAction(Arrays.asList(compressAction, posixAttributeViewAction), false);
        }
        Action asyncAction = this.merge(compressAction, this.customActions, this.stopCustomActionsOnError);
        return new RolloverDescriptionImpl(sourceName, false, null, asyncAction);
    }

    public String toString() {
        return "DirectWriteRolloverStrategy(maxFiles=" + this.maxFiles + ')';
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<DirectWriteRolloverStrategy> {
        @PluginBuilderAttribute(value="maxFiles")
        private String maxFiles;
        @PluginBuilderAttribute(value="compressionLevel")
        private String compressionLevelStr;
        @PluginElement(value="Actions")
        private Action[] customActions;
        @PluginBuilderAttribute(value="stopCustomActionsOnError")
        private boolean stopCustomActionsOnError = true;
        @PluginBuilderAttribute(value="tempCompressedFilePattern")
        private String tempCompressedFilePattern;
        @PluginConfiguration
        private Configuration config;

        @Override
        public DirectWriteRolloverStrategy build() {
            int maxIndex = Integer.MAX_VALUE;
            if (this.maxFiles != null) {
                maxIndex = Integers.parseInt(this.maxFiles);
                if (maxIndex < 0) {
                    maxIndex = Integer.MAX_VALUE;
                } else if (maxIndex < 2) {
                    AbstractRolloverStrategy.LOGGER.error("Maximum files too small. Limited to 7");
                    maxIndex = 7;
                }
            }
            int compressionLevel = Integers.parseInt(this.compressionLevelStr, -1);
            return new DirectWriteRolloverStrategy(maxIndex, compressionLevel, this.config.getStrSubstitutor(), this.customActions, this.stopCustomActionsOnError, this.tempCompressedFilePattern);
        }

        public String getMaxFiles() {
            return this.maxFiles;
        }

        public Builder withMaxFiles(String maxFiles) {
            this.maxFiles = maxFiles;
            return this;
        }

        public String getCompressionLevelStr() {
            return this.compressionLevelStr;
        }

        public Builder withCompressionLevelStr(String compressionLevelStr) {
            this.compressionLevelStr = compressionLevelStr;
            return this;
        }

        public Action[] getCustomActions() {
            return this.customActions;
        }

        public Builder withCustomActions(Action[] customActions) {
            this.customActions = customActions;
            return this;
        }

        public boolean isStopCustomActionsOnError() {
            return this.stopCustomActionsOnError;
        }

        public Builder withStopCustomActionsOnError(boolean stopCustomActionsOnError) {
            this.stopCustomActionsOnError = stopCustomActionsOnError;
            return this;
        }

        public String getTempCompressedFilePattern() {
            return this.tempCompressedFilePattern;
        }

        public Builder withTempCompressedFilePattern(String tempCompressedFilePattern) {
            this.tempCompressedFilePattern = tempCompressedFilePattern;
            return this;
        }

        public Configuration getConfig() {
            return this.config;
        }

        public Builder withConfig(Configuration config) {
            this.config = config;
            return this;
        }
    }
}

