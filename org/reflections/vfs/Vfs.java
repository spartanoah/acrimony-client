/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.JarInputDir;
import org.reflections.vfs.JbossDir;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.UrlTypeVFS;
import org.reflections.vfs.ZipDir;

public abstract class Vfs {
    private static List<UrlType> defaultUrlTypes = new ArrayList<DefaultUrlTypes>(Arrays.asList(DefaultUrlTypes.values()));

    public static List<UrlType> getDefaultUrlTypes() {
        return defaultUrlTypes;
    }

    public static void setDefaultURLTypes(List<UrlType> urlTypes) {
        defaultUrlTypes = urlTypes;
    }

    public static void addDefaultURLTypes(UrlType urlType) {
        defaultUrlTypes.add(0, urlType);
    }

    public static Dir fromURL(URL url) {
        return Vfs.fromURL(url, defaultUrlTypes);
    }

    public static Dir fromURL(URL url, List<UrlType> urlTypes) {
        for (UrlType type : urlTypes) {
            try {
                Dir dir;
                if (!type.matches(url) || (dir = type.createDir(url)) == null) continue;
                return dir;
            } catch (Throwable e) {
                if (Reflections.log == null) continue;
                Reflections.log.warn("could not create Dir using " + type + " from url " + url.toExternalForm() + ". skipping.", e);
            }
        }
        throw new ReflectionsException("could not create Vfs.Dir from url, no matching UrlType was found [" + url.toExternalForm() + "]\neither use fromURL(final URL url, final List<UrlType> urlTypes) or use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) with your specialized UrlType.");
    }

    public static Dir fromURL(URL url, UrlType ... urlTypes) {
        return Vfs.fromURL(url, Arrays.asList(urlTypes));
    }

    public static Iterable<File> findFiles(Collection<URL> inUrls, String packagePrefix, Predicate<String> nameFilter) {
        Predicate<File> fileNamePredicate = file -> {
            String path = file.getRelativePath();
            if (path.startsWith(packagePrefix)) {
                String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());
                return !filename.isEmpty() && nameFilter.test(filename.substring(1));
            }
            return false;
        };
        return Vfs.findFiles(inUrls, fileNamePredicate);
    }

    public static Iterable<File> findFiles(Collection<URL> urls, Predicate<File> filePredicate) {
        return () -> urls.stream().flatMap(url -> {
            try {
                return StreamSupport.stream(Vfs.fromURL(url).getFiles().spliterator(), false);
            } catch (Throwable e) {
                if (Reflections.log != null) {
                    Reflections.log.error("could not findFiles for url. continuing. [" + url + "]", e);
                }
                return Stream.of(new File[0]);
            }
        }).filter(filePredicate).iterator();
    }

    public static java.io.File getFile(URL url) {
        java.io.File file;
        String path;
        try {
            path = url.toURI().getSchemeSpecificPart();
            file = new java.io.File(path);
            if (file.exists()) {
                return file;
            }
        } catch (URISyntaxException uRISyntaxException) {
            // empty catch block
        }
        try {
            path = URLDecoder.decode(url.getPath(), "UTF-8");
            if (path.contains(".jar!")) {
                path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
            }
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        try {
            path = url.toExternalForm();
            if (path.startsWith("jar:")) {
                path = path.substring("jar:".length());
            }
            if (path.startsWith("wsjar:")) {
                path = path.substring("wsjar:".length());
            }
            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
            }
            if (path.contains(".jar!")) {
                path = path.substring(0, path.indexOf(".jar!") + ".jar".length());
            }
            if (path.contains(".war!")) {
                path = path.substring(0, path.indexOf(".war!") + ".war".length());
            }
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
            file = new java.io.File(path = path.replace("%20", " "));
            if (file.exists()) {
                return file;
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static boolean hasJarFileInPath(URL url) {
        return url.toExternalForm().matches(".*\\.jar(!.*|$)");
    }

    private static boolean hasInnerJarFileInPath(URL url) {
        return url.toExternalForm().matches(".+\\.jar!/.+");
    }

    public static enum DefaultUrlTypes implements UrlType
    {
        jarFile{

            @Override
            public boolean matches(URL url) {
                return url.getProtocol().equals("file") && Vfs.hasJarFileInPath(url);
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return new ZipDir(new JarFile(Vfs.getFile(url)));
            }
        }
        ,
        jarUrl{

            @Override
            public boolean matches(URL url) {
                return ("jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol())) && !Vfs.hasInnerJarFileInPath(url);
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                try {
                    URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection) {
                        urlConnection.setUseCaches(false);
                        return new ZipDir(((JarURLConnection)urlConnection).getJarFile());
                    }
                } catch (Throwable urlConnection) {
                    // empty catch block
                }
                java.io.File file = Vfs.getFile(url);
                if (file != null) {
                    return new ZipDir(new JarFile(file));
                }
                return null;
            }
        }
        ,
        directory{

            @Override
            public boolean matches(URL url) {
                if (url.getProtocol().equals("file") && !Vfs.hasJarFileInPath(url)) {
                    java.io.File file = Vfs.getFile(url);
                    return file != null && file.isDirectory();
                }
                return false;
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return new SystemDir(Vfs.getFile(url));
            }
        }
        ,
        jboss_vfs{

            @Override
            public boolean matches(URL url) {
                return url.getProtocol().equals("vfs");
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return JbossDir.createDir(url);
            }
        }
        ,
        jboss_vfsfile{

            @Override
            public boolean matches(URL url) throws Exception {
                return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return new UrlTypeVFS().createDir(url);
            }
        }
        ,
        bundle{

            @Override
            public boolean matches(URL url) throws Exception {
                return url.getProtocol().startsWith("bundle");
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return Vfs.fromURL((URL)ClasspathHelper.contextClassLoader().loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", URL.class).invoke(null, url));
            }
        }
        ,
        jarInputStream{

            @Override
            public boolean matches(URL url) throws Exception {
                return url.toExternalForm().contains(".jar");
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return new JarInputDir(url);
            }
        };

    }

    public static interface UrlType {
        public boolean matches(URL var1) throws Exception;

        public Dir createDir(URL var1) throws Exception;
    }

    public static interface File {
        public String getName();

        public String getRelativePath();

        public InputStream openInputStream() throws IOException;
    }

    public static interface Dir {
        public String getPath();

        public Iterable<File> getFiles();

        default public void close() {
        }
    }
}

