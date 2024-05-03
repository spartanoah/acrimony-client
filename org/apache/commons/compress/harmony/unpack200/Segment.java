/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.AttrDefinitionBands;
import org.apache.commons.compress.harmony.unpack200.AttributeLayout;
import org.apache.commons.compress.harmony.unpack200.BcBands;
import org.apache.commons.compress.harmony.unpack200.ClassBands;
import org.apache.commons.compress.harmony.unpack200.CpBands;
import org.apache.commons.compress.harmony.unpack200.FileBands;
import org.apache.commons.compress.harmony.unpack200.IcBands;
import org.apache.commons.compress.harmony.unpack200.IcTuple;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;
import org.apache.commons.compress.harmony.unpack200.SegmentHeader;
import org.apache.commons.compress.harmony.unpack200.SegmentOptions;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPField;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMethod;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFile;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.InnerClassesAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.SourceFileAttribute;

public class Segment {
    public static final int LOG_LEVEL_VERBOSE = 2;
    public static final int LOG_LEVEL_STANDARD = 1;
    public static final int LOG_LEVEL_QUIET = 0;
    private SegmentHeader header;
    private CpBands cpBands;
    private AttrDefinitionBands attrDefinitionBands;
    private IcBands icBands;
    private ClassBands classBands;
    private BcBands bcBands;
    private FileBands fileBands;
    private boolean overrideDeflateHint;
    private boolean deflateHint;
    private boolean doPreRead;
    private int logLevel;
    private PrintWriter logStream;
    private byte[][] classFilesContents;
    private boolean[] fileDeflate;
    private boolean[] fileIsClass;
    private InputStream internalBuffer;

    private ClassFile buildClassFile(int classNum) throws Pack200Exception {
        ClassFile classFile = new ClassFile();
        int[] major = this.classBands.getClassVersionMajor();
        int[] minor = this.classBands.getClassVersionMinor();
        if (major != null) {
            classFile.major = major[classNum];
            classFile.minor = minor[classNum];
        } else {
            classFile.major = this.header.getDefaultClassMajorVersion();
            classFile.minor = this.header.getDefaultClassMinorVersion();
        }
        ClassConstantPool cp = classFile.pool;
        int fullNameIndexInCpClass = this.classBands.getClassThisInts()[classNum];
        String fullName = this.cpBands.getCpClass()[fullNameIndexInCpClass];
        int i = fullName.lastIndexOf("/") + 1;
        ArrayList classAttributes = this.classBands.getClassAttributes()[classNum];
        SourceFileAttribute sourceFileAttribute = null;
        for (int index = 0; index < classAttributes.size(); ++index) {
            if (!((Attribute)classAttributes.get(index)).isSourceFileAttribute()) continue;
            sourceFileAttribute = (SourceFileAttribute)classAttributes.get(index);
        }
        if (sourceFileAttribute == null) {
            AttributeLayout SOURCE_FILE = this.attrDefinitionBands.getAttributeDefinitionMap().getAttributeLayout("SourceFile", 0);
            if (SOURCE_FILE.matches(this.classBands.getRawClassFlags()[classNum])) {
                int firstDollar = -1;
                for (int index = 0; index < fullName.length(); ++index) {
                    if (fullName.charAt(index) > '$') continue;
                    firstDollar = index;
                }
                String fileName = null;
                fileName = firstDollar > -1 && i <= firstDollar ? fullName.substring(i, firstDollar) + ".java" : fullName.substring(i) + ".java";
                sourceFileAttribute = new SourceFileAttribute(this.cpBands.cpUTF8Value(fileName, false));
                classFile.attributes = new Attribute[]{(Attribute)cp.add(sourceFileAttribute)};
            } else {
                classFile.attributes = new Attribute[0];
            }
        } else {
            classFile.attributes = new Attribute[]{(Attribute)cp.add(sourceFileAttribute)};
        }
        ArrayList<Attribute> classAttributesWithoutSourceFileAttribute = new ArrayList<Attribute>(classAttributes.size());
        for (int index = 0; index < classAttributes.size(); ++index) {
            Attribute attrib = (Attribute)classAttributes.get(index);
            if (attrib.isSourceFileAttribute()) continue;
            classAttributesWithoutSourceFileAttribute.add(attrib);
        }
        Attribute[] originalAttributes = classFile.attributes;
        classFile.attributes = new Attribute[originalAttributes.length + classAttributesWithoutSourceFileAttribute.size()];
        System.arraycopy(originalAttributes, 0, classFile.attributes, 0, originalAttributes.length);
        for (int index = 0; index < classAttributesWithoutSourceFileAttribute.size(); ++index) {
            Attribute attrib = (Attribute)classAttributesWithoutSourceFileAttribute.get(index);
            cp.add(attrib);
            classFile.attributes[originalAttributes.length + index] = attrib;
        }
        ClassFileEntry cfThis = cp.add(this.cpBands.cpClassValue(fullNameIndexInCpClass));
        ClassFileEntry cfSuper = cp.add(this.cpBands.cpClassValue(this.classBands.getClassSuperInts()[classNum]));
        ClassFileEntry[] cfInterfaces = new ClassFileEntry[this.classBands.getClassInterfacesInts()[classNum].length];
        for (i = 0; i < cfInterfaces.length; ++i) {
            cfInterfaces[i] = cp.add(this.cpBands.cpClassValue(this.classBands.getClassInterfacesInts()[classNum][i]));
        }
        ClassFileEntry[] cfFields = new ClassFileEntry[this.classBands.getClassFieldCount()[classNum]];
        for (i = 0; i < cfFields.length; ++i) {
            int descriptorIndex = this.classBands.getFieldDescrInts()[classNum][i];
            int nameIndex = this.cpBands.getCpDescriptorNameInts()[descriptorIndex];
            int typeIndex = this.cpBands.getCpDescriptorTypeInts()[descriptorIndex];
            CPUTF8 name = this.cpBands.cpUTF8Value(nameIndex);
            CPUTF8 descriptor = this.cpBands.cpSignatureValue(typeIndex);
            cfFields[i] = cp.add(new CPField(name, descriptor, this.classBands.getFieldFlags()[classNum][i], this.classBands.getFieldAttributes()[classNum][i]));
        }
        ClassFileEntry[] cfMethods = new ClassFileEntry[this.classBands.getClassMethodCount()[classNum]];
        for (i = 0; i < cfMethods.length; ++i) {
            int descriptorIndex = this.classBands.getMethodDescrInts()[classNum][i];
            int nameIndex = this.cpBands.getCpDescriptorNameInts()[descriptorIndex];
            int typeIndex = this.cpBands.getCpDescriptorTypeInts()[descriptorIndex];
            CPUTF8 name = this.cpBands.cpUTF8Value(nameIndex);
            CPUTF8 descriptor = this.cpBands.cpSignatureValue(typeIndex);
            cfMethods[i] = cp.add(new CPMethod(name, descriptor, this.classBands.getMethodFlags()[classNum][i], this.classBands.getMethodAttributes()[classNum][i]));
        }
        cp.addNestedEntries();
        boolean addInnerClassesAttr = false;
        IcTuple[] ic_local = this.getClassBands().getIcLocal()[classNum];
        boolean ic_local_sent = ic_local != null;
        InnerClassesAttribute innerClassesAttribute = new InnerClassesAttribute("InnerClasses");
        IcTuple[] ic_relevant = this.getIcBands().getRelevantIcTuples(fullName, cp);
        List ic_stored = this.computeIcStored(ic_local, ic_relevant);
        for (int index = 0; index < ic_stored.size(); ++index) {
            IcTuple icStored = (IcTuple)ic_stored.get(index);
            int innerClassIndex = icStored.thisClassIndex();
            int outerClassIndex = icStored.outerClassIndex();
            int simpleClassNameIndex = icStored.simpleClassNameIndex();
            String innerClassString = icStored.thisClassString();
            String outerClassString = icStored.outerClassString();
            String simpleClassName = icStored.simpleClassName();
            CPClass innerClass = null;
            CPUTF8 innerName = null;
            CPClass outerClass = null;
            CPClass cPClass = innerClass = innerClassIndex != -1 ? this.cpBands.cpClassValue(innerClassIndex) : this.cpBands.cpClassValue(innerClassString);
            if (!icStored.isAnonymous()) {
                CPUTF8 cPUTF8 = innerName = simpleClassNameIndex != -1 ? this.cpBands.cpUTF8Value(simpleClassNameIndex) : this.cpBands.cpUTF8Value(simpleClassName);
            }
            if (icStored.isMember()) {
                outerClass = outerClassIndex != -1 ? this.cpBands.cpClassValue(outerClassIndex) : this.cpBands.cpClassValue(outerClassString);
            }
            int flags = icStored.F;
            innerClassesAttribute.addInnerClassesEntry(innerClass, outerClass, innerName, flags);
            addInnerClassesAttr = true;
        }
        if (ic_local_sent && ic_local.length == 0) {
            addInnerClassesAttr = false;
        }
        if (!ic_local_sent && ic_relevant.length == 0) {
            addInnerClassesAttr = false;
        }
        if (addInnerClassesAttr) {
            Attribute[] originalAttrs = classFile.attributes;
            Attribute[] newAttrs = new Attribute[originalAttrs.length + 1];
            for (int index = 0; index < originalAttrs.length; ++index) {
                newAttrs[index] = originalAttrs[index];
            }
            newAttrs[newAttrs.length - 1] = innerClassesAttribute;
            classFile.attributes = newAttrs;
            cp.addWithNestedEntries(innerClassesAttribute);
        }
        cp.resolve(this);
        classFile.accessFlags = (int)this.classBands.getClassFlags()[classNum];
        classFile.thisClass = cp.indexOf(cfThis);
        classFile.superClass = cp.indexOf(cfSuper);
        classFile.interfaces = new int[cfInterfaces.length];
        for (i = 0; i < cfInterfaces.length; ++i) {
            classFile.interfaces[i] = cp.indexOf(cfInterfaces[i]);
        }
        classFile.fields = cfFields;
        classFile.methods = cfMethods;
        return classFile;
    }

    private List computeIcStored(IcTuple[] ic_local, IcTuple[] ic_relevant) {
        int index;
        ArrayList<IcTuple> result = new ArrayList<IcTuple>(ic_relevant.length);
        ArrayList<IcTuple> duplicates = new ArrayList<IcTuple>(ic_relevant.length);
        HashSet<IcTuple> isInResult = new HashSet<IcTuple>(ic_relevant.length);
        if (ic_local != null) {
            for (index = 0; index < ic_local.length; ++index) {
                if (!isInResult.add(ic_local[index])) continue;
                result.add(ic_local[index]);
            }
        }
        for (index = 0; index < ic_relevant.length; ++index) {
            if (isInResult.add(ic_relevant[index])) {
                result.add(ic_relevant[index]);
                continue;
            }
            duplicates.add(ic_relevant[index]);
        }
        for (index = 0; index < duplicates.size(); ++index) {
            IcTuple tuple = (IcTuple)duplicates.get(index);
            result.remove(tuple);
        }
        return result;
    }

    private void readSegment(InputStream in) throws IOException, Pack200Exception {
        this.log(2, "-------");
        this.cpBands = new CpBands(this);
        this.cpBands.read(in);
        this.attrDefinitionBands = new AttrDefinitionBands(this);
        this.attrDefinitionBands.read(in);
        this.icBands = new IcBands(this);
        this.icBands.read(in);
        this.classBands = new ClassBands(this);
        this.classBands.read(in);
        this.bcBands = new BcBands(this);
        this.bcBands.read(in);
        this.fileBands = new FileBands(this);
        this.fileBands.read(in);
        this.fileBands.processFileBits();
    }

    private void parseSegment() throws IOException, Pack200Exception {
        this.header.unpack();
        this.cpBands.unpack();
        this.attrDefinitionBands.unpack();
        this.icBands.unpack();
        this.classBands.unpack();
        this.bcBands.unpack();
        this.fileBands.unpack();
        int classNum = 0;
        int numberOfFiles = this.header.getNumberOfFiles();
        String[] fileName = this.fileBands.getFileName();
        int[] fileOptions = this.fileBands.getFileOptions();
        SegmentOptions options = this.header.getOptions();
        this.classFilesContents = new byte[numberOfFiles][];
        this.fileDeflate = new boolean[numberOfFiles];
        this.fileIsClass = new boolean[numberOfFiles];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (int i = 0; i < numberOfFiles; ++i) {
            boolean isClass;
            String name = fileName[i];
            boolean nameIsEmpty = name == null || name.equals("");
            boolean bl = isClass = (fileOptions[i] & 2) == 2 || nameIsEmpty;
            if (isClass && nameIsEmpty) {
                fileName[i] = name = this.cpBands.getCpClass()[this.classBands.getClassThisInts()[classNum]] + ".class";
            }
            this.fileDeflate[i] = !this.overrideDeflateHint ? (fileOptions[i] & 1) == 1 || options.shouldDeflate() : this.deflateHint;
            this.fileIsClass[i] = isClass;
            if (!isClass) continue;
            ClassFile classFile = this.buildClassFile(classNum);
            classFile.write(dos);
            dos.flush();
            this.classFilesContents[classNum] = bos.toByteArray();
            bos.reset();
            ++classNum;
        }
    }

    public void unpack(InputStream in, JarOutputStream out) throws IOException, Pack200Exception {
        this.unpackRead(in);
        this.unpackProcess();
        this.unpackWrite(out);
    }

    void unpackRead(InputStream in) throws IOException, Pack200Exception {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        this.header = new SegmentHeader(this);
        this.header.read(in);
        int size = (int)this.header.getArchiveSize() - this.header.getArchiveSizeOffset();
        if (this.doPreRead && this.header.getArchiveSize() != 0L) {
            byte[] data = new byte[size];
            in.read(data);
            this.internalBuffer = new BufferedInputStream(new ByteArrayInputStream(data));
        } else {
            this.readSegment(in);
        }
    }

    void unpackProcess() throws IOException, Pack200Exception {
        if (this.internalBuffer != null) {
            this.readSegment(this.internalBuffer);
        }
        this.parseSegment();
    }

    void unpackWrite(JarOutputStream out) throws IOException, Pack200Exception {
        this.writeJar(out);
        if (this.logStream != null) {
            this.logStream.close();
        }
    }

    public void writeJar(JarOutputStream out) throws IOException, Pack200Exception {
        String[] fileName = this.fileBands.getFileName();
        int[] fileModtime = this.fileBands.getFileModtime();
        long[] fileSize = this.fileBands.getFileSize();
        byte[][] fileBits = this.fileBands.getFileBits();
        int classNum = 0;
        int numberOfFiles = this.header.getNumberOfFiles();
        long archiveModtime = this.header.getArchiveModtime();
        for (int i = 0; i < numberOfFiles; ++i) {
            String name = fileName[i];
            long modtime = 1000L * (archiveModtime + (long)fileModtime[i]);
            boolean deflate = this.fileDeflate[i];
            JarEntry entry = new JarEntry(name);
            if (deflate) {
                entry.setMethod(8);
            } else {
                entry.setMethod(0);
                CRC32 crc = new CRC32();
                if (this.fileIsClass[i]) {
                    crc.update(this.classFilesContents[classNum]);
                    entry.setSize(this.classFilesContents[classNum].length);
                } else {
                    crc.update(fileBits[i]);
                    entry.setSize(fileSize[i]);
                }
                entry.setCrc(crc.getValue());
            }
            entry.setTime(modtime - (long)TimeZone.getDefault().getRawOffset());
            out.putNextEntry(entry);
            if (this.fileIsClass[i]) {
                entry.setSize(this.classFilesContents[classNum].length);
                out.write(this.classFilesContents[classNum]);
                ++classNum;
                continue;
            }
            entry.setSize(fileSize[i]);
            out.write(fileBits[i]);
        }
    }

    public SegmentConstantPool getConstantPool() {
        return this.cpBands.getConstantPool();
    }

    public SegmentHeader getSegmentHeader() {
        return this.header;
    }

    public void setPreRead(boolean value) {
        this.doPreRead = value;
    }

    protected AttrDefinitionBands getAttrDefinitionBands() {
        return this.attrDefinitionBands;
    }

    protected ClassBands getClassBands() {
        return this.classBands;
    }

    protected CpBands getCpBands() {
        return this.cpBands;
    }

    protected IcBands getIcBands() {
        return this.icBands;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public void setLogStream(OutputStream logStream) {
        this.logStream = new PrintWriter(logStream);
    }

    public void log(int logLevel, String message) {
        if (this.logLevel >= logLevel) {
            this.logStream.println(message);
        }
    }

    public void overrideDeflateHint(boolean deflateHint) {
        this.overrideDeflateHint = true;
        this.deflateHint = deflateHint;
    }
}

