/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@JsonDeserialize(as=MarkerManager.Log4jMarker.class)
abstract class MarkerMixIn
implements Marker {
    private static final long serialVersionUID = 1L;

    @JsonCreator
    MarkerMixIn(@JsonProperty(value="name") String name) {
    }

    @Override
    @JsonProperty(value="name")
    @JacksonXmlProperty(isAttribute=true)
    public abstract String getName();

    @Override
    @JsonProperty(value="parents")
    @JacksonXmlElementWrapper(namespace="http://logging.apache.org/log4j/2.0/events", localName="Parents")
    @JacksonXmlProperty(namespace="http://logging.apache.org/log4j/2.0/events", localName="Marker")
    public abstract Marker[] getParents();
}

