/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.sevenz;

import java.io.File;
import java.io.IOException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;

public class CLI {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            CLI.usage();
            return;
        }
        Mode mode = CLI.grabMode(args);
        System.out.println(mode.getMessage() + " " + args[0]);
        File f = new File(args[0]);
        if (!f.isFile()) {
            System.err.println(f + " doesn't exist or is a directory");
        }
        try (SevenZFile archive = new SevenZFile(f);){
            SevenZArchiveEntry ae;
            while ((ae = archive.getNextEntry()) != null) {
                mode.takeAction(archive, ae);
            }
        }
    }

    private static void usage() {
        System.out.println("Parameters: archive-name [list]");
    }

    private static Mode grabMode(String[] args) {
        if (args.length < 2) {
            return Mode.LIST;
        }
        return Enum.valueOf(Mode.class, args[1].toUpperCase());
    }

    private static enum Mode {
        LIST("Analysing"){

            @Override
            public void takeAction(SevenZFile archive, SevenZArchiveEntry entry) {
                System.out.print(entry.getName());
                if (entry.isDirectory()) {
                    System.out.print(" dir");
                } else {
                    System.out.print(" " + entry.getCompressedSize() + "/" + entry.getSize());
                }
                if (entry.getHasLastModifiedDate()) {
                    System.out.print(" " + entry.getLastModifiedDate());
                } else {
                    System.out.print(" no last modified date");
                }
                if (!entry.isDirectory()) {
                    System.out.println(" " + this.getContentMethods(entry));
                } else {
                    System.out.println();
                }
            }

            private String getContentMethods(SevenZArchiveEntry entry) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (SevenZMethodConfiguration sevenZMethodConfiguration : entry.getContentMethods()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    sb.append((Object)sevenZMethodConfiguration.getMethod());
                    if (sevenZMethodConfiguration.getOptions() == null) continue;
                    sb.append("(").append(sevenZMethodConfiguration.getOptions()).append(")");
                }
                return sb.toString();
            }
        };

        private final String message;

        private Mode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }

        public abstract void takeAction(SevenZFile var1, SevenZArchiveEntry var2) throws IOException;
    }
}

