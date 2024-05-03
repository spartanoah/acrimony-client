/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.AttributeLayout;
import org.apache.commons.compress.harmony.unpack200.AttributeLayoutMap;
import org.apache.commons.compress.harmony.unpack200.BandSet;
import org.apache.commons.compress.harmony.unpack200.MetadataBandGroup;
import org.apache.commons.compress.harmony.unpack200.NewAttributeBands;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.bytecode.AnnotationDefaultAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CodeAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantValueAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.DeprecatedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.EnclosingMethodAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.ExceptionsAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.InnerClassesAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LineNumberTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTypeTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.SignatureAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.SourceFileAttribute;

public class AttrDefinitionBands
extends BandSet {
    private int[] attributeDefinitionHeader;
    private String[] attributeDefinitionLayout;
    private String[] attributeDefinitionName;
    private AttributeLayoutMap attributeDefinitionMap;
    private final String[] cpUTF8;

    public AttrDefinitionBands(Segment segment) {
        super(segment);
        this.cpUTF8 = segment.getCpBands().getCpUTF8();
    }

    @Override
    public void read(InputStream in) throws IOException, Pack200Exception {
        int attributeDefinitionCount = this.header.getAttributeDefinitionCount();
        this.attributeDefinitionHeader = this.decodeBandInt("attr_definition_headers", in, Codec.BYTE1, attributeDefinitionCount);
        this.attributeDefinitionName = this.parseReferences("attr_definition_name", in, Codec.UNSIGNED5, attributeDefinitionCount, this.cpUTF8);
        this.attributeDefinitionLayout = this.parseReferences("attr_definition_layout", in, Codec.UNSIGNED5, attributeDefinitionCount, this.cpUTF8);
        this.attributeDefinitionMap = new AttributeLayoutMap();
        int overflowIndex = 32;
        if (this.segment.getSegmentHeader().getOptions().hasClassFlagsHi()) {
            overflowIndex = 63;
        }
        for (int i = 0; i < attributeDefinitionCount; ++i) {
            int context = this.attributeDefinitionHeader[i] & 3;
            int index = (this.attributeDefinitionHeader[i] >> 2) - 1;
            if (index == -1) {
                index = overflowIndex++;
            }
            AttributeLayout layout = new AttributeLayout(this.attributeDefinitionName[i], context, this.attributeDefinitionLayout[i], index, false);
            NewAttributeBands newBands = new NewAttributeBands(this.segment, layout);
            this.attributeDefinitionMap.add(layout, newBands);
        }
        this.attributeDefinitionMap.checkMap();
        this.setupDefaultAttributeNames();
    }

    @Override
    public void unpack() throws Pack200Exception, IOException {
    }

    private void setupDefaultAttributeNames() {
        AnnotationDefaultAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("AnnotationDefault"));
        CodeAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("Code"));
        ConstantValueAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("ConstantValue"));
        DeprecatedAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("Deprecated"));
        EnclosingMethodAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("EnclosingMethod"));
        ExceptionsAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("Exceptions"));
        InnerClassesAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("InnerClasses"));
        LineNumberTableAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("LineNumberTable"));
        LocalVariableTableAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("LocalVariableTable"));
        LocalVariableTypeTableAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("LocalVariableTypeTable"));
        SignatureAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("Signature"));
        SourceFileAttribute.setAttributeName(this.segment.getCpBands().cpUTF8Value("SourceFile"));
        MetadataBandGroup.setRvaAttributeName(this.segment.getCpBands().cpUTF8Value("RuntimeVisibleAnnotations"));
        MetadataBandGroup.setRiaAttributeName(this.segment.getCpBands().cpUTF8Value("RuntimeInvisibleAnnotations"));
        MetadataBandGroup.setRvpaAttributeName(this.segment.getCpBands().cpUTF8Value("RuntimeVisibleParameterAnnotations"));
        MetadataBandGroup.setRipaAttributeName(this.segment.getCpBands().cpUTF8Value("RuntimeInvisibleParameterAnnotations"));
    }

    public AttributeLayoutMap getAttributeDefinitionMap() {
        return this.attributeDefinitionMap;
    }
}

