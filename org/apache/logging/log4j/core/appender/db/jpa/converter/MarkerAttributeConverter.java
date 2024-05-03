/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javax.persistence.AttributeConverter
 *  javax.persistence.Converter
 */
package org.apache.logging.log4j.core.appender.db.jpa.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.helpers.Strings;

@Converter(autoApply=false)
public class MarkerAttributeConverter
implements AttributeConverter<Marker, String> {
    public String convertToDatabaseColumn(Marker marker) {
        if (marker == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(marker.getName());
        int levels = 0;
        boolean hasParent = false;
        for (Marker parent = marker.getParent(); parent != null; parent = parent.getParent()) {
            ++levels;
            hasParent = true;
            builder.append("[ ").append(parent.getName());
        }
        for (int i = 0; i < levels; ++i) {
            builder.append(" ]");
        }
        if (hasParent) {
            builder.append(" ]");
        }
        return builder.toString();
    }

    public Marker convertToEntityAttribute(String s) {
        if (Strings.isEmpty(s)) {
            return null;
        }
        int bracket = s.indexOf("[");
        return bracket < 1 ? MarkerManager.getMarker(s) : MarkerManager.getMarker(s.substring(0, bracket));
    }
}

