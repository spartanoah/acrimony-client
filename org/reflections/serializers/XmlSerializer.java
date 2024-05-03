/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.dom4j.Document
 *  org.dom4j.DocumentFactory
 *  org.dom4j.Element
 *  org.dom4j.Node
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.SAXReader
 *  org.dom4j.io.XMLWriter
 */
package org.reflections.serializers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.Store;
import org.reflections.serializers.Serializer;

public class XmlSerializer
implements Serializer {
    @Override
    public Reflections read(InputStream inputStream) {
        try {
            Document document = new SAXReader().read(inputStream);
            Map<String, Map<String, Set<String>>> storeMap = document.getRootElement().elements().stream().collect(Collectors.toMap(Node::getName, index -> index.elements().stream().collect(Collectors.toMap(entry -> entry.element("key").getText(), entry -> entry.element("values").elements().stream().map(Element::getText).collect(Collectors.toSet())))));
            return new Reflections(new Store(storeMap));
        } catch (Exception e) {
            throw new ReflectionsException("could not read.", e);
        }
    }

    @Override
    public File save(Reflections reflections, String filename) {
        File file = Serializer.prepareFile(filename);
        try (FileOutputStream out = new FileOutputStream(file);){
            new XMLWriter((OutputStream)out, OutputFormat.createPrettyPrint()).write(this.createDocument(reflections.getStore()));
        } catch (Exception e) {
            throw new ReflectionsException("could not save to file " + filename, e);
        }
        return file;
    }

    private Document createDocument(Store store) {
        Document document = DocumentFactory.getInstance().createDocument();
        Element root = document.addElement("Reflections");
        store.forEach((index, map) -> {
            Element indexElement = root.addElement(index);
            map.forEach((key, values) -> {
                Element entryElement = indexElement.addElement("entry");
                entryElement.addElement("key").setText(key);
                Element valuesElement = entryElement.addElement("values");
                values.forEach(value -> valuesElement.addElement("value").setText(value));
            });
        });
        return document;
    }
}

