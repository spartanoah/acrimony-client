/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core;

public class Version {
    public static void main(String[] args) {
        System.out.println(Version.getProductString());
    }

    public static String getProductString() {
        Package pkg = Version.class.getPackage();
        if (pkg == null) {
            return "Apache Log4j";
        }
        return String.format("%s %s", pkg.getSpecificationTitle(), pkg.getSpecificationVersion());
    }
}

