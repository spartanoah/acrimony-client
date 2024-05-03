/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import java.io.Serializable;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.Strings;

public class StructuredDataId
implements Serializable,
StringBuilderFormattable {
    public static final StructuredDataId TIME_QUALITY = new StructuredDataId("timeQuality", null, new String[]{"tzKnown", "isSynced", "syncAccuracy"});
    public static final StructuredDataId ORIGIN = new StructuredDataId("origin", null, new String[]{"ip", "enterpriseId", "software", "swVersion"});
    public static final StructuredDataId META = new StructuredDataId("meta", null, new String[]{"sequenceId", "sysUpTime", "language"});
    public static final String RESERVED = "-1";
    private static final long serialVersionUID = -8252896346202183738L;
    private static final int MAX_LENGTH = 32;
    private static final String AT_SIGN = "@";
    private final String name;
    private final String enterpriseNumber;
    private final String[] required;
    private final String[] optional;

    public StructuredDataId(String name) {
        this(name, null, null, 32);
    }

    public StructuredDataId(String name, int maxLength) {
        this(name, null, null, maxLength);
    }

    public StructuredDataId(String name, String[] required, String[] optional) {
        this(name, required, optional, 32);
    }

    public StructuredDataId(String name, String[] required, String[] optional, int maxLength) {
        int index = -1;
        if (name != null) {
            if (maxLength <= 0) {
                maxLength = 32;
            }
            if (name.length() > maxLength) {
                throw new IllegalArgumentException(String.format("Length of id %s exceeds maximum of %d characters", name, maxLength));
            }
            index = name.indexOf(AT_SIGN);
        }
        if (index > 0) {
            this.name = name.substring(0, index);
            this.enterpriseNumber = name.substring(index + 1).trim();
        } else {
            this.name = name;
            this.enterpriseNumber = RESERVED;
        }
        this.required = required;
        this.optional = optional;
    }

    public StructuredDataId(String name, String enterpriseNumber, String[] required, String[] optional) {
        this(name, enterpriseNumber, required, optional, 32);
    }

    @Deprecated
    public StructuredDataId(String name, int enterpriseNumber, String[] required, String[] optional) {
        this(name, String.valueOf(enterpriseNumber), required, optional, 32);
    }

    public StructuredDataId(String name, String enterpriseNumber, String[] required, String[] optional, int maxLength) {
        if (name == null) {
            throw new IllegalArgumentException("No structured id name was supplied");
        }
        if (name.contains(AT_SIGN)) {
            throw new IllegalArgumentException("Structured id name cannot contain an " + Strings.quote(AT_SIGN));
        }
        if (RESERVED.equals(enterpriseNumber)) {
            throw new IllegalArgumentException("No enterprise number was supplied");
        }
        this.name = name;
        this.enterpriseNumber = enterpriseNumber;
        String id = name + AT_SIGN + enterpriseNumber;
        if (maxLength > 0 && id.length() > maxLength) {
            throw new IllegalArgumentException("Length of id exceeds maximum of " + maxLength + " characters: " + id);
        }
        this.required = required;
        this.optional = optional;
    }

    @Deprecated
    public StructuredDataId(String name, int enterpriseNumber, String[] required, String[] optional, int maxLength) {
        this(name, String.valueOf(enterpriseNumber), required, optional, maxLength);
    }

    public StructuredDataId makeId(StructuredDataId id) {
        if (id == null) {
            return this;
        }
        return this.makeId(id.getName(), id.getEnterpriseNumber());
    }

    public StructuredDataId makeId(String defaultId, String anEnterpriseNumber) {
        String[] opt;
        String[] req;
        String id;
        if (RESERVED.equals(anEnterpriseNumber)) {
            return this;
        }
        if (this.name != null) {
            id = this.name;
            req = this.required;
            opt = this.optional;
        } else {
            id = defaultId;
            req = null;
            opt = null;
        }
        return new StructuredDataId(id, anEnterpriseNumber, req, opt);
    }

    @Deprecated
    public StructuredDataId makeId(String defaultId, int anEnterpriseNumber) {
        return this.makeId(defaultId, String.valueOf(anEnterpriseNumber));
    }

    public String[] getRequired() {
        return this.required;
    }

    public String[] getOptional() {
        return this.optional;
    }

    public String getName() {
        return this.name;
    }

    public String getEnterpriseNumber() {
        return this.enterpriseNumber;
    }

    public boolean isReserved() {
        return RESERVED.equals(this.enterpriseNumber);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.name.length() + 10);
        this.formatTo(sb);
        return sb.toString();
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        if (this.isReserved()) {
            buffer.append(this.name);
        } else {
            buffer.append(this.name).append(AT_SIGN).append(this.enterpriseNumber);
        }
    }
}

