/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SizeFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class FileFilterUtils {
    private static final IOFileFilter cvsFilter = FileFilterUtils.notFileFilter(FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), FileFilterUtils.nameFileFilter("CVS")));
    private static final IOFileFilter svnFilter = FileFilterUtils.notFileFilter(FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), FileFilterUtils.nameFileFilter(".svn")));

    public static File[] filter(IOFileFilter filter, File ... files) {
        if (filter == null) {
            throw new IllegalArgumentException("file filter is null");
        }
        if (files == null) {
            return new File[0];
        }
        ArrayList<File> acceptedFiles = new ArrayList<File>();
        for (File file : files) {
            if (file == null) {
                throw new IllegalArgumentException("file array contains null");
            }
            if (!filter.accept(file)) continue;
            acceptedFiles.add(file);
        }
        return acceptedFiles.toArray(new File[acceptedFiles.size()]);
    }

    public static File[] filter(IOFileFilter filter, Iterable<File> files) {
        List<File> acceptedFiles = FileFilterUtils.filterList(filter, files);
        return acceptedFiles.toArray(new File[acceptedFiles.size()]);
    }

    public static List<File> filterList(IOFileFilter filter, Iterable<File> files) {
        return FileFilterUtils.filter(filter, files, new ArrayList());
    }

    public static List<File> filterList(IOFileFilter filter, File ... files) {
        File[] acceptedFiles = FileFilterUtils.filter(filter, files);
        return Arrays.asList(acceptedFiles);
    }

    public static Set<File> filterSet(IOFileFilter filter, File ... files) {
        File[] acceptedFiles = FileFilterUtils.filter(filter, files);
        return new HashSet<File>(Arrays.asList(acceptedFiles));
    }

    public static Set<File> filterSet(IOFileFilter filter, Iterable<File> files) {
        return FileFilterUtils.filter(filter, files, new HashSet());
    }

    private static <T extends Collection<File>> T filter(IOFileFilter filter, Iterable<File> files, T acceptedFiles) {
        if (filter == null) {
            throw new IllegalArgumentException("file filter is null");
        }
        if (files != null) {
            for (File file : files) {
                if (file == null) {
                    throw new IllegalArgumentException("file collection contains null");
                }
                if (!filter.accept(file)) continue;
                acceptedFiles.add((File)file);
            }
        }
        return acceptedFiles;
    }

    public static IOFileFilter prefixFileFilter(String prefix) {
        return new PrefixFileFilter(prefix);
    }

    public static IOFileFilter prefixFileFilter(String prefix, IOCase caseSensitivity) {
        return new PrefixFileFilter(prefix, caseSensitivity);
    }

    public static IOFileFilter suffixFileFilter(String suffix) {
        return new SuffixFileFilter(suffix);
    }

    public static IOFileFilter suffixFileFilter(String suffix, IOCase caseSensitivity) {
        return new SuffixFileFilter(suffix, caseSensitivity);
    }

    public static IOFileFilter nameFileFilter(String name) {
        return new NameFileFilter(name);
    }

    public static IOFileFilter nameFileFilter(String name, IOCase caseSensitivity) {
        return new NameFileFilter(name, caseSensitivity);
    }

    public static IOFileFilter directoryFileFilter() {
        return DirectoryFileFilter.DIRECTORY;
    }

    public static IOFileFilter fileFileFilter() {
        return FileFileFilter.FILE;
    }

    @Deprecated
    public static IOFileFilter andFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        return new AndFileFilter(filter1, filter2);
    }

    @Deprecated
    public static IOFileFilter orFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        return new OrFileFilter(filter1, filter2);
    }

    public static IOFileFilter and(IOFileFilter ... filters) {
        return new AndFileFilter(FileFilterUtils.toList(filters));
    }

    public static IOFileFilter or(IOFileFilter ... filters) {
        return new OrFileFilter(FileFilterUtils.toList(filters));
    }

    public static List<IOFileFilter> toList(IOFileFilter ... filters) {
        if (filters == null) {
            throw new IllegalArgumentException("The filters must not be null");
        }
        ArrayList<IOFileFilter> list = new ArrayList<IOFileFilter>(filters.length);
        for (int i = 0; i < filters.length; ++i) {
            if (filters[i] == null) {
                throw new IllegalArgumentException("The filter[" + i + "] is null");
            }
            list.add(filters[i]);
        }
        return list;
    }

    public static IOFileFilter notFileFilter(IOFileFilter filter) {
        return new NotFileFilter(filter);
    }

    public static IOFileFilter trueFileFilter() {
        return TrueFileFilter.TRUE;
    }

    public static IOFileFilter falseFileFilter() {
        return FalseFileFilter.FALSE;
    }

    public static IOFileFilter asFileFilter(FileFilter filter) {
        return new DelegateFileFilter(filter);
    }

    public static IOFileFilter asFileFilter(FilenameFilter filter) {
        return new DelegateFileFilter(filter);
    }

    public static IOFileFilter ageFileFilter(long cutoff) {
        return new AgeFileFilter(cutoff);
    }

    public static IOFileFilter ageFileFilter(long cutoff, boolean acceptOlder) {
        return new AgeFileFilter(cutoff, acceptOlder);
    }

    public static IOFileFilter ageFileFilter(Date cutoffDate) {
        return new AgeFileFilter(cutoffDate);
    }

    public static IOFileFilter ageFileFilter(Date cutoffDate, boolean acceptOlder) {
        return new AgeFileFilter(cutoffDate, acceptOlder);
    }

    public static IOFileFilter ageFileFilter(File cutoffReference) {
        return new AgeFileFilter(cutoffReference);
    }

    public static IOFileFilter ageFileFilter(File cutoffReference, boolean acceptOlder) {
        return new AgeFileFilter(cutoffReference, acceptOlder);
    }

    public static IOFileFilter sizeFileFilter(long threshold) {
        return new SizeFileFilter(threshold);
    }

    public static IOFileFilter sizeFileFilter(long threshold, boolean acceptLarger) {
        return new SizeFileFilter(threshold, acceptLarger);
    }

    public static IOFileFilter sizeRangeFileFilter(long minSizeInclusive, long maxSizeInclusive) {
        SizeFileFilter minimumFilter = new SizeFileFilter(minSizeInclusive, true);
        SizeFileFilter maximumFilter = new SizeFileFilter(maxSizeInclusive + 1L, false);
        return new AndFileFilter(minimumFilter, maximumFilter);
    }

    public static IOFileFilter magicNumberFileFilter(String magicNumber) {
        return new MagicNumberFileFilter(magicNumber);
    }

    public static IOFileFilter magicNumberFileFilter(String magicNumber, long offset) {
        return new MagicNumberFileFilter(magicNumber, offset);
    }

    public static IOFileFilter magicNumberFileFilter(byte[] magicNumber) {
        return new MagicNumberFileFilter(magicNumber);
    }

    public static IOFileFilter magicNumberFileFilter(byte[] magicNumber, long offset) {
        return new MagicNumberFileFilter(magicNumber, offset);
    }

    public static IOFileFilter makeCVSAware(IOFileFilter filter) {
        if (filter == null) {
            return cvsFilter;
        }
        return FileFilterUtils.and(filter, cvsFilter);
    }

    public static IOFileFilter makeSVNAware(IOFileFilter filter) {
        if (filter == null) {
            return svnFilter;
        }
        return FileFilterUtils.and(filter, svnFilter);
    }

    public static IOFileFilter makeDirectoryOnly(IOFileFilter filter) {
        if (filter == null) {
            return DirectoryFileFilter.DIRECTORY;
        }
        return new AndFileFilter(DirectoryFileFilter.DIRECTORY, filter);
    }

    public static IOFileFilter makeFileOnly(IOFileFilter filter) {
        if (filter == null) {
            return FileFileFilter.FILE;
        }
        return new AndFileFilter(FileFileFilter.FILE, filter);
    }
}

