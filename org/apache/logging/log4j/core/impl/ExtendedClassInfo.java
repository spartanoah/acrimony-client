/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.impl;

import java.io.Serializable;
import java.util.Objects;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.core.pattern.TextRenderer;

public final class ExtendedClassInfo
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean exact;
    private final String location;
    private final String version;

    public ExtendedClassInfo(boolean exact, String location, String version) {
        this.exact = exact;
        this.location = location;
        this.version = version;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExtendedClassInfo)) {
            return false;
        }
        ExtendedClassInfo other = (ExtendedClassInfo)obj;
        if (this.exact != other.exact) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return Objects.equals(this.version, other.version);
    }

    public boolean getExact() {
        return this.exact;
    }

    public String getLocation() {
        return this.location;
    }

    public String getVersion() {
        return this.version;
    }

    public int hashCode() {
        return Objects.hash(this.exact, this.location, this.version);
    }

    public void renderOn(StringBuilder output, TextRenderer textRenderer) {
        if (!this.exact) {
            textRenderer.render("~", output, "ExtraClassInfo.Inexact");
        }
        textRenderer.render("[", output, "ExtraClassInfo.Container");
        textRenderer.render(this.location, output, "ExtraClassInfo.Location");
        textRenderer.render(":", output, "ExtraClassInfo.ContainerSeparator");
        textRenderer.render(this.version, output, "ExtraClassInfo.Version");
        textRenderer.render("]", output, "ExtraClassInfo.Container");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.renderOn(sb, PlainTextRenderer.getInstance());
        return sb.toString();
    }
}

