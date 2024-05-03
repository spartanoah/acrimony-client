/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class VersionUtil {
    private static final Pattern V_SEP = Pattern.compile("[-_./;:]");

    protected VersionUtil() {
    }

    @Deprecated
    public Version version() {
        return Version.unknownVersion();
    }

    public static Version versionFor(Class<?> cls) {
        Version version = VersionUtil.packageVersionFor(cls);
        return version == null ? Version.unknownVersion() : version;
    }

    public static Version packageVersionFor(Class<?> cls) {
        Version v = null;
        try {
            String versionInfoClassName = cls.getPackage().getName() + ".PackageVersion";
            Class<?> vClass = Class.forName(versionInfoClassName, true, cls.getClassLoader());
            try {
                v = ((Versioned)vClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0])).version();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to get Versioned out of " + vClass);
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return v == null ? Version.unknownVersion() : v;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public static Version mavenVersionFor(ClassLoader cl, String groupId, String artifactId) {
        InputStream pomProperties = cl.getResourceAsStream("META-INF/maven/" + groupId.replaceAll("\\.", "/") + "/" + artifactId + "/pom.properties");
        if (pomProperties != null) {
            try {
                Properties props = new Properties();
                props.load(pomProperties);
                String versionStr = props.getProperty("version");
                String pomPropertiesArtifactId = props.getProperty("artifactId");
                String pomPropertiesGroupId = props.getProperty("groupId");
                Version version = VersionUtil.parseVersion(versionStr, pomPropertiesGroupId, pomPropertiesArtifactId);
                return version;
            } catch (IOException iOException) {
            } finally {
                VersionUtil._close(pomProperties);
            }
        }
        return Version.unknownVersion();
    }

    public static Version parseVersion(String s, String groupId, String artifactId) {
        if (s != null && (s = s.trim()).length() > 0) {
            String[] parts = V_SEP.split(s);
            return new Version(VersionUtil.parseVersionPart(parts[0]), parts.length > 1 ? VersionUtil.parseVersionPart(parts[1]) : 0, parts.length > 2 ? VersionUtil.parseVersionPart(parts[2]) : 0, parts.length > 3 ? parts[3] : null, groupId, artifactId);
        }
        return Version.unknownVersion();
    }

    protected static int parseVersionPart(String s) {
        char c;
        int number = 0;
        int len = s.length();
        for (int i = 0; i < len && (c = s.charAt(i)) <= '9' && c >= '0'; ++i) {
            number = number * 10 + (c - 48);
        }
        return number;
    }

    private static final void _close(Closeable c) {
        try {
            c.close();
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    public static final void throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
}

