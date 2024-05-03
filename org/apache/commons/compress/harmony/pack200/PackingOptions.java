/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Attribute
 */
package org.apache.commons.compress.harmony.pack200;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.harmony.pack200.NewAttribute;
import org.objectweb.asm.Attribute;

public class PackingOptions {
    public static final String STRIP = "strip";
    public static final String ERROR = "error";
    public static final String PASS = "pass";
    public static final String KEEP = "keep";
    private boolean gzip = true;
    private boolean stripDebug = false;
    private boolean keepFileOrder = true;
    private long segmentLimit = 1000000L;
    private int effort = 5;
    private String deflateHint = "keep";
    private String modificationTime = "keep";
    private List passFiles;
    private String unknownAttributeAction = "pass";
    private Map classAttributeActions;
    private Map fieldAttributeActions;
    private Map methodAttributeActions;
    private Map codeAttributeActions;
    private boolean verbose = false;
    private String logFile;
    private Attribute[] unknownAttributeTypes;

    public boolean isGzip() {
        return this.gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public boolean isStripDebug() {
        return this.stripDebug;
    }

    public void setStripDebug(boolean stripDebug) {
        this.stripDebug = stripDebug;
    }

    public boolean isKeepFileOrder() {
        return this.keepFileOrder;
    }

    public void setKeepFileOrder(boolean keepFileOrder) {
        this.keepFileOrder = keepFileOrder;
    }

    public long getSegmentLimit() {
        return this.segmentLimit;
    }

    public void setSegmentLimit(long segmentLimit) {
        this.segmentLimit = segmentLimit;
    }

    public int getEffort() {
        return this.effort;
    }

    public void setEffort(int effort) {
        this.effort = effort;
    }

    public String getDeflateHint() {
        return this.deflateHint;
    }

    public boolean isKeepDeflateHint() {
        return KEEP.equals(this.deflateHint);
    }

    public void setDeflateHint(String deflateHint) {
        if (!(KEEP.equals(deflateHint) || "true".equals(deflateHint) || "false".equals(deflateHint))) {
            throw new IllegalArgumentException("Bad argument: -H " + deflateHint + " ? deflate hint should be either true, false or keep (default)");
        }
        this.deflateHint = deflateHint;
    }

    public String getModificationTime() {
        return this.modificationTime;
    }

    public void setModificationTime(String modificationTime) {
        if (!KEEP.equals(modificationTime) && !"latest".equals(modificationTime)) {
            throw new IllegalArgumentException("Bad argument: -m " + modificationTime + " ? transmit modtimes should be either latest or keep (default)");
        }
        this.modificationTime = modificationTime;
    }

    public boolean isPassFile(String passFileName) {
        if (this.passFiles != null) {
            for (String pass : this.passFiles) {
                if (passFileName.equals(pass)) {
                    return true;
                }
                if (pass.endsWith(".class")) continue;
                if (!pass.endsWith("/")) {
                    pass = pass + "/";
                }
                return passFileName.startsWith(pass);
            }
        }
        return false;
    }

    public void addPassFile(String passFileName) {
        String fileSeparator;
        if (this.passFiles == null) {
            this.passFiles = new ArrayList();
        }
        if ((fileSeparator = System.getProperty("file.separator")).equals("\\")) {
            fileSeparator = fileSeparator + "\\";
        }
        passFileName = passFileName.replaceAll(fileSeparator, "/");
        this.passFiles.add(passFileName);
    }

    public void removePassFile(String passFileName) {
        this.passFiles.remove(passFileName);
    }

    public String getUnknownAttributeAction() {
        return this.unknownAttributeAction;
    }

    public void setUnknownAttributeAction(String unknownAttributeAction) {
        this.unknownAttributeAction = unknownAttributeAction;
        if (!(PASS.equals(unknownAttributeAction) || ERROR.equals(unknownAttributeAction) || STRIP.equals(unknownAttributeAction))) {
            throw new RuntimeException("Incorrect option for -U, " + unknownAttributeAction);
        }
    }

    public void addClassAttributeAction(String attributeName, String action) {
        if (this.classAttributeActions == null) {
            this.classAttributeActions = new HashMap();
        }
        this.classAttributeActions.put(attributeName, action);
    }

    public void addFieldAttributeAction(String attributeName, String action) {
        if (this.fieldAttributeActions == null) {
            this.fieldAttributeActions = new HashMap();
        }
        this.fieldAttributeActions.put(attributeName, action);
    }

    public void addMethodAttributeAction(String attributeName, String action) {
        if (this.methodAttributeActions == null) {
            this.methodAttributeActions = new HashMap();
        }
        this.methodAttributeActions.put(attributeName, action);
    }

    public void addCodeAttributeAction(String attributeName, String action) {
        if (this.codeAttributeActions == null) {
            this.codeAttributeActions = new HashMap();
        }
        this.codeAttributeActions.put(attributeName, action);
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setQuiet(boolean quiet) {
        this.verbose = !quiet;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    private void addOrUpdateAttributeActions(List prototypes, Map attributeActions, int tag) {
        if (attributeActions != null && attributeActions.size() > 0) {
            for (String name : attributeActions.keySet()) {
                NewAttribute newAttribute2;
                String action = (String)attributeActions.get(name);
                boolean prototypeExists = false;
                for (NewAttribute newAttribute2 : prototypes) {
                    if (!newAttribute2.type.equals(name)) continue;
                    newAttribute2.addContext(tag);
                    prototypeExists = true;
                    break;
                }
                if (prototypeExists) continue;
                newAttribute2 = ERROR.equals(action) ? new NewAttribute.ErrorAttribute(name, tag) : (STRIP.equals(action) ? new NewAttribute.StripAttribute(name, tag) : (PASS.equals(action) ? new NewAttribute.PassAttribute(name, tag) : new NewAttribute(name, action, tag)));
                prototypes.add(newAttribute2);
            }
        }
    }

    public Attribute[] getUnknownAttributePrototypes() {
        if (this.unknownAttributeTypes == null) {
            ArrayList prototypes = new ArrayList();
            this.addOrUpdateAttributeActions(prototypes, this.classAttributeActions, 0);
            this.addOrUpdateAttributeActions(prototypes, this.methodAttributeActions, 2);
            this.addOrUpdateAttributeActions(prototypes, this.fieldAttributeActions, 1);
            this.addOrUpdateAttributeActions(prototypes, this.codeAttributeActions, 3);
            this.unknownAttributeTypes = prototypes.toArray(new Attribute[0]);
        }
        return this.unknownAttributeTypes;
    }

    public String getUnknownClassAttributeAction(String type) {
        if (this.classAttributeActions == null) {
            return this.unknownAttributeAction;
        }
        String action = (String)this.classAttributeActions.get(type);
        if (action == null) {
            action = this.unknownAttributeAction;
        }
        return action;
    }

    public String getUnknownMethodAttributeAction(String type) {
        if (this.methodAttributeActions == null) {
            return this.unknownAttributeAction;
        }
        String action = (String)this.methodAttributeActions.get(type);
        if (action == null) {
            action = this.unknownAttributeAction;
        }
        return action;
    }

    public String getUnknownFieldAttributeAction(String type) {
        if (this.fieldAttributeActions == null) {
            return this.unknownAttributeAction;
        }
        String action = (String)this.fieldAttributeActions.get(type);
        if (action == null) {
            action = this.unknownAttributeAction;
        }
        return action;
    }

    public String getUnknownCodeAttributeAction(String type) {
        if (this.codeAttributeActions == null) {
            return this.unknownAttributeAction;
        }
        String action = (String)this.codeAttributeActions.get(type);
        if (action == null) {
            action = this.unknownAttributeAction;
        }
        return action;
    }
}

