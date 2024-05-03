/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(value={"epochMillisecond", "nanoOfMillisecond"})
abstract class InstantMixIn {
    @JsonCreator
    InstantMixIn(@JsonProperty(value="epochSecond") long epochSecond, @JsonProperty(value="nanoOfSecond") int nanoOfSecond) {
    }

    @JsonProperty(value="epochSecond")
    @JacksonXmlProperty(localName="epochSecond", isAttribute=true)
    abstract long getEpochSecond();

    @JsonProperty(value="nanoOfSecond")
    @JacksonXmlProperty(localName="nanoOfSecond", isAttribute=true)
    abstract int getNanoOfSecond();
}

