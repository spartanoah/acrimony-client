/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.extensions;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExtensionRequestData {
    public static final String EMPTY_VALUE = "";
    private Map<String, String> extensionParameters = new LinkedHashMap<String, String>();
    private String extensionName;

    private ExtensionRequestData() {
    }

    public static ExtensionRequestData parseExtensionRequest(String extensionRequest) {
        ExtensionRequestData extensionData = new ExtensionRequestData();
        String[] parts = extensionRequest.split(";");
        extensionData.extensionName = parts[0].trim();
        for (int i = 1; i < parts.length; ++i) {
            String[] keyValue = parts[i].split("=");
            String value = EMPTY_VALUE;
            if (keyValue.length > 1) {
                String tempValue = keyValue[1].trim();
                if (tempValue.startsWith("\"") && tempValue.endsWith("\"") || tempValue.startsWith("'") && tempValue.endsWith("'") && tempValue.length() > 2) {
                    tempValue = tempValue.substring(1, tempValue.length() - 1);
                }
                value = tempValue;
            }
            extensionData.extensionParameters.put(keyValue[0].trim(), value);
        }
        return extensionData;
    }

    public String getExtensionName() {
        return this.extensionName;
    }

    public Map<String, String> getExtensionParameters() {
        return this.extensionParameters;
    }
}

