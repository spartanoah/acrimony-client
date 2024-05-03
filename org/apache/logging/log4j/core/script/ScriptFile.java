/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.util.ExtensionLanguageMapping;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.IOUtils;
import org.apache.logging.log4j.core.util.NetUtils;

@Plugin(name="ScriptFile", category="Core", printObject=true)
public class ScriptFile
extends AbstractScript {
    private final Path filePath;
    private final boolean isWatched;

    public ScriptFile(String name, Path filePath, String language, boolean isWatched, String scriptText) {
        super(name, language, scriptText);
        this.filePath = filePath;
        this.isWatched = isWatched;
    }

    public Path getPath() {
        return this.filePath;
    }

    public boolean isWatched() {
        return this.isWatched;
    }

    @PluginFactory
    public static ScriptFile createScript(@PluginAttribute(value="name") String name, @PluginAttribute(value="language") String language, @PluginAttribute(value="path") String filePathOrUri, @PluginAttribute(value="isWatched") Boolean isWatched, @PluginAttribute(value="charset") Charset charset) {
        Path path;
        String scriptText;
        ExtensionLanguageMapping mapping;
        String fileExtension;
        if (filePathOrUri == null) {
            LOGGER.error("No script path provided for ScriptFile");
            return null;
        }
        if (name == null) {
            name = filePathOrUri;
        }
        URI uri = NetUtils.toURI(filePathOrUri);
        File file = FileUtils.fileFromUri(uri);
        if (language == null && file != null && (fileExtension = FileUtils.getFileExtension(file)) != null && (mapping = ExtensionLanguageMapping.getByExtension(fileExtension)) != null) {
            language = mapping.getLanguage();
        }
        if (language == null) {
            LOGGER.info("No script language supplied, defaulting to {}", (Object)"JavaScript");
            language = "JavaScript";
        }
        Charset actualCharset = charset == null ? Charset.defaultCharset() : charset;
        try (InputStreamReader reader = new InputStreamReader(file != null ? new FileInputStream(file) : uri.toURL().openStream(), actualCharset);){
            scriptText = IOUtils.toString(reader);
        } catch (IOException e) {
            LOGGER.error("{}: language={}, path={}, actualCharset={}", (Object)e.getClass().getSimpleName(), (Object)language, (Object)filePathOrUri, (Object)actualCharset);
            return null;
        }
        Path path2 = path = file != null ? Paths.get(file.toURI()) : Paths.get(uri);
        if (path == null) {
            LOGGER.error("Unable to convert {} to a Path", (Object)uri.toString());
            return null;
        }
        return new ScriptFile(name, path, language, isWatched == null ? Boolean.FALSE : isWatched, scriptText);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!this.getName().equals(this.filePath.toString())) {
            sb.append("name=").append(this.getName()).append(", ");
        }
        sb.append("path=").append(this.filePath);
        if (this.getLanguage() != null) {
            sb.append(", language=").append(this.getLanguage());
        }
        sb.append(", isWatched=").append(this.isWatched);
        return sb.toString();
    }
}

