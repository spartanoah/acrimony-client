/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Label
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.harmony.pack200.AttributeDefinitionBands;
import org.apache.commons.compress.harmony.pack200.BHSDCodec;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.IntList;
import org.apache.commons.compress.harmony.pack200.NewAttribute;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;
import org.objectweb.asm.Label;

public class NewAttributeBands
extends BandSet {
    protected List attributeLayoutElements;
    private int[] backwardsCallCounts;
    private final CpBands cpBands;
    private final AttributeDefinitionBands.AttributeDefinition def;
    private boolean usedAtLeastOnce;
    private Integral lastPIntegral;

    public NewAttributeBands(int effort, CpBands cpBands, SegmentHeader header, AttributeDefinitionBands.AttributeDefinition def) throws IOException {
        super(effort, header);
        this.def = def;
        this.cpBands = cpBands;
        this.parseLayout();
    }

    public void addAttribute(NewAttribute attribute) {
        this.usedAtLeastOnce = true;
        ByteArrayInputStream stream = new ByteArrayInputStream(attribute.getBytes());
        for (AttributeLayoutElement layoutElement : this.attributeLayoutElements) {
            layoutElement.addAttributeToBand(attribute, stream);
        }
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        for (AttributeLayoutElement layoutElement : this.attributeLayoutElements) {
            layoutElement.pack(out);
        }
    }

    public String getAttributeName() {
        return this.def.name.getUnderlyingString();
    }

    public int getFlagIndex() {
        return this.def.index;
    }

    public int[] numBackwardsCalls() {
        return this.backwardsCallCounts;
    }

    public boolean isUsedAtLeastOnce() {
        return this.usedAtLeastOnce;
    }

    private void parseLayout() throws IOException {
        String layout = this.def.layout.getUnderlyingString();
        if (this.attributeLayoutElements == null) {
            AttributeLayoutElement e;
            this.attributeLayoutElements = new ArrayList();
            StringReader stream = new StringReader(layout);
            while ((e = this.readNextAttributeElement(stream)) != null) {
                this.attributeLayoutElements.add(e);
            }
            this.resolveCalls();
        }
    }

    private void resolveCalls() {
        for (int i = 0; i < this.attributeLayoutElements.size(); ++i) {
            AttributeLayoutElement element = (AttributeLayoutElement)this.attributeLayoutElements.get(i);
            if (!(element instanceof Callable)) continue;
            Callable callable = (Callable)element;
            List body = callable.body;
            for (int iIndex = 0; iIndex < body.size(); ++iIndex) {
                LayoutElement layoutElement = (LayoutElement)body.get(iIndex);
                this.resolveCallsForElement(i, callable, layoutElement);
            }
        }
        int backwardsCallableIndex = 0;
        for (int i = 0; i < this.attributeLayoutElements.size(); ++i) {
            Callable callable;
            AttributeLayoutElement element = (AttributeLayoutElement)this.attributeLayoutElements.get(i);
            if (!(element instanceof Callable) || !(callable = (Callable)element).isBackwardsCallable) continue;
            callable.setBackwardsCallableIndex(backwardsCallableIndex);
            ++backwardsCallableIndex;
        }
        this.backwardsCallCounts = new int[backwardsCallableIndex];
    }

    private void resolveCallsForElement(int i, Callable currentCallable, LayoutElement layoutElement) {
        block7: {
            block8: {
                if (!(layoutElement instanceof Call)) break block8;
                Call call = (Call)layoutElement;
                int index = call.callableIndex;
                if (index == 0) {
                    call.setCallable(currentCallable);
                } else if (index > 0) {
                    for (int k = i + 1; k < this.attributeLayoutElements.size(); ++k) {
                        AttributeLayoutElement el = (AttributeLayoutElement)this.attributeLayoutElements.get(k);
                        if (!(el instanceof Callable) || --index != 0) continue;
                        call.setCallable((Callable)el);
                        break block7;
                    }
                } else {
                    for (int k = i - 1; k >= 0; --k) {
                        AttributeLayoutElement el = (AttributeLayoutElement)this.attributeLayoutElements.get(k);
                        if (!(el instanceof Callable) || ++index != 0) continue;
                        call.setCallable((Callable)el);
                        break block7;
                    }
                }
                break block7;
            }
            if (!(layoutElement instanceof Replication)) break block7;
            List children = ((Replication)layoutElement).layoutElements;
            for (LayoutElement object : children) {
                this.resolveCallsForElement(i, currentCallable, object);
            }
        }
    }

    private AttributeLayoutElement readNextAttributeElement(StringReader stream) throws IOException {
        stream.mark(1);
        int nextChar = stream.read();
        if (nextChar == -1) {
            return null;
        }
        if (nextChar == 91) {
            List body = this.readBody(this.getStreamUpToMatchingBracket(stream));
            return new Callable(body);
        }
        stream.reset();
        return this.readNextLayoutElement(stream);
    }

    private LayoutElement readNextLayoutElement(StringReader stream) throws IOException {
        int nextChar = stream.read();
        if (nextChar == -1) {
            return null;
        }
        switch (nextChar) {
            case 66: 
            case 72: 
            case 73: 
            case 86: {
                return new Integral(new String(new char[]{(char)nextChar}));
            }
            case 70: 
            case 83: {
                return new Integral(new String(new char[]{(char)nextChar, (char)stream.read()}));
            }
            case 80: {
                stream.mark(1);
                if (stream.read() != 79) {
                    stream.reset();
                    this.lastPIntegral = new Integral("P" + (char)stream.read());
                    return this.lastPIntegral;
                }
                this.lastPIntegral = new Integral("PO" + (char)stream.read(), this.lastPIntegral);
                return this.lastPIntegral;
            }
            case 79: {
                stream.mark(1);
                if (stream.read() != 83) {
                    stream.reset();
                    return new Integral("O" + (char)stream.read(), this.lastPIntegral);
                }
                return new Integral("OS" + (char)stream.read(), this.lastPIntegral);
            }
            case 78: {
                char uint_type = (char)stream.read();
                stream.read();
                String str = this.readUpToMatchingBracket(stream);
                return new Replication("" + uint_type, str);
            }
            case 84: {
                UnionCase c;
                String int_type = "" + (char)stream.read();
                if (int_type.equals("S")) {
                    int_type = int_type + (char)stream.read();
                }
                ArrayList<UnionCase> unionCases = new ArrayList<UnionCase>();
                while ((c = this.readNextUnionCase(stream)) != null) {
                    unionCases.add(c);
                }
                stream.read();
                stream.read();
                stream.read();
                List body = null;
                stream.mark(1);
                char next = (char)stream.read();
                if (next != ']') {
                    stream.reset();
                    body = this.readBody(this.getStreamUpToMatchingBracket(stream));
                }
                return new Union(int_type, unionCases, body);
            }
            case 40: {
                int number = this.readNumber(stream);
                stream.read();
                return new Call(number);
            }
            case 75: 
            case 82: {
                StringBuilder string = new StringBuilder("").append((char)nextChar).append((char)stream.read());
                char nxt = (char)stream.read();
                string.append(nxt);
                if (nxt == 'N') {
                    string.append((char)stream.read());
                }
                return new Reference(string.toString());
            }
        }
        return null;
    }

    private UnionCase readNextUnionCase(StringReader stream) throws IOException {
        Integer nextTag;
        stream.mark(2);
        stream.read();
        char next = (char)stream.read();
        if (next == ')') {
            stream.reset();
            return null;
        }
        stream.reset();
        stream.read();
        ArrayList<Integer> tags = new ArrayList<Integer>();
        do {
            if ((nextTag = this.readNumber(stream)) == null) continue;
            tags.add(nextTag);
            stream.read();
        } while (nextTag != null);
        stream.read();
        stream.mark(1);
        next = (char)stream.read();
        if (next == ']') {
            return new UnionCase(tags);
        }
        stream.reset();
        return new UnionCase(tags, this.readBody(this.getStreamUpToMatchingBracket(stream)));
    }

    private StringReader getStreamUpToMatchingBracket(StringReader stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        int foundBracket = -1;
        while (foundBracket != 0) {
            char c = (char)stream.read();
            if (c == ']') {
                ++foundBracket;
            }
            if (c == '[') {
                --foundBracket;
            }
            if (foundBracket == 0) continue;
            sb.append(c);
        }
        return new StringReader(sb.toString());
    }

    private int readInteger(int i, InputStream stream) {
        int result = 0;
        for (int j = 0; j < i; ++j) {
            try {
                result = result << 8 | stream.read();
                continue;
            } catch (IOException e) {
                throw new RuntimeException("Error reading unknown attribute");
            }
        }
        if (i == 1) {
            result = (byte)result;
        }
        if (i == 2) {
            result = (short)result;
        }
        return result;
    }

    private BHSDCodec getCodec(String layoutElement) {
        if (layoutElement.indexOf(79) >= 0) {
            return Codec.BRANCH5;
        }
        if (layoutElement.indexOf(80) >= 0) {
            return Codec.BCI5;
        }
        if (layoutElement.indexOf(83) >= 0 && layoutElement.indexOf("KS") < 0 && layoutElement.indexOf("RS") < 0) {
            return Codec.SIGNED5;
        }
        if (layoutElement.indexOf(66) >= 0) {
            return Codec.BYTE1;
        }
        return Codec.UNSIGNED5;
    }

    private String readUpToMatchingBracket(StringReader stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        int foundBracket = -1;
        while (foundBracket != 0) {
            char c = (char)stream.read();
            if (c == ']') {
                ++foundBracket;
            }
            if (c == '[') {
                --foundBracket;
            }
            if (foundBracket == 0) continue;
            sb.append(c);
        }
        return sb.toString();
    }

    private Integer readNumber(StringReader stream) throws IOException {
        int i;
        boolean negative;
        stream.mark(1);
        char first = (char)stream.read();
        boolean bl = negative = first == '-';
        if (!negative) {
            stream.reset();
        }
        stream.mark(100);
        int length = 0;
        while ((i = stream.read()) != -1 && Character.isDigit((char)i)) {
            ++length;
        }
        stream.reset();
        if (length == 0) {
            return null;
        }
        char[] digits = new char[length];
        int read = stream.read(digits);
        if (read != digits.length) {
            throw new IOException("Error reading from the input stream");
        }
        return Integer.parseInt((negative ? "-" : "") + new String(digits));
    }

    private List readBody(StringReader stream) throws IOException {
        LayoutElement e;
        ArrayList<LayoutElement> layoutElements = new ArrayList<LayoutElement>();
        while ((e = this.readNextLayoutElement(stream)) != null) {
            layoutElements.add(e);
        }
        return layoutElements;
    }

    public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
        for (AttributeLayoutElement element : this.attributeLayoutElements) {
            element.renumberBci(bciRenumbering, labelsToOffsets);
        }
    }

    public class UnionCase
    extends LayoutElement {
        private final List body;
        private final List tags;

        public UnionCase(List tags) {
            this.tags = tags;
            this.body = Collections.EMPTY_LIST;
        }

        public boolean hasTag(long l) {
            return this.tags.contains((int)l);
        }

        public UnionCase(List tags, List body) throws IOException {
            this.tags = tags;
            this.body = body;
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            for (int i = 0; i < this.body.size(); ++i) {
                LayoutElement element = (LayoutElement)this.body.get(i);
                element.addAttributeToBand(attribute, stream);
            }
        }

        @Override
        public void pack(OutputStream out) throws IOException, Pack200Exception {
            for (int i = 0; i < this.body.size(); ++i) {
                LayoutElement element = (LayoutElement)this.body.get(i);
                element.pack(out);
            }
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
            for (int i = 0; i < this.body.size(); ++i) {
                LayoutElement element = (LayoutElement)this.body.get(i);
                element.renumberBci(bciRenumbering, labelsToOffsets);
            }
        }

        public List getBody() {
            return this.body;
        }
    }

    public class Callable
    implements AttributeLayoutElement {
        private final List body;
        private boolean isBackwardsCallable;
        private int backwardsCallableIndex;

        public Callable(List body) throws IOException {
            this.body = body;
        }

        public void setBackwardsCallableIndex(int backwardsCallableIndex) {
            this.backwardsCallableIndex = backwardsCallableIndex;
        }

        public void addBackwardsCall() {
            int[] nArray = NewAttributeBands.this.backwardsCallCounts;
            int n = this.backwardsCallableIndex;
            nArray[n] = nArray[n] + 1;
        }

        public boolean isBackwardsCallable() {
            return this.isBackwardsCallable;
        }

        public void setBackwardsCallable() {
            this.isBackwardsCallable = true;
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            for (AttributeLayoutElement layoutElement : this.body) {
                layoutElement.addAttributeToBand(attribute, stream);
            }
        }

        @Override
        public void pack(OutputStream out) throws IOException, Pack200Exception {
            for (AttributeLayoutElement layoutElement : this.body) {
                layoutElement.pack(out);
            }
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
            for (AttributeLayoutElement layoutElement : this.body) {
                layoutElement.renumberBci(bciRenumbering, labelsToOffsets);
            }
        }

        public List getBody() {
            return this.body;
        }
    }

    public class Reference
    extends LayoutElement {
        private final String tag;
        private List band;
        private boolean nullsAllowed;

        public Reference(String tag) {
            this.nullsAllowed = false;
            this.tag = tag;
            this.nullsAllowed = tag.indexOf(78) != -1;
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            int index = NewAttributeBands.this.readInteger(4, stream);
            if (this.tag.startsWith("RC")) {
                this.band.add(NewAttributeBands.this.cpBands.getCPClass(attribute.readClass(index)));
            } else if (this.tag.startsWith("RU")) {
                this.band.add(NewAttributeBands.this.cpBands.getCPUtf8(attribute.readUTF8(index)));
            } else if (this.tag.startsWith("RS")) {
                this.band.add(NewAttributeBands.this.cpBands.getCPSignature(attribute.readUTF8(index)));
            } else {
                this.band.add(NewAttributeBands.this.cpBands.getConstant(attribute.readConst(index)));
            }
        }

        public String getTag() {
            return this.tag;
        }

        @Override
        public void pack(OutputStream out) throws IOException, Pack200Exception {
            int[] ints = this.nullsAllowed ? NewAttributeBands.this.cpEntryOrNullListToArray(this.band) : NewAttributeBands.this.cpEntryListToArray(this.band);
            byte[] encodedBand = NewAttributeBands.this.encodeBandInt(this.tag, ints, Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + this.tag + "[" + ints.length + "]");
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
        }
    }

    public class Call
    extends LayoutElement {
        private final int callableIndex;
        private Callable callable;

        public Call(int callableIndex) {
            this.callableIndex = callableIndex;
        }

        public void setCallable(Callable callable) {
            this.callable = callable;
            if (this.callableIndex < 1) {
                callable.setBackwardsCallable();
            }
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            this.callable.addAttributeToBand(attribute, stream);
            if (this.callableIndex < 1) {
                this.callable.addBackwardsCall();
            }
        }

        @Override
        public void pack(OutputStream out) {
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
        }

        public int getCallableIndex() {
            return this.callableIndex;
        }

        public Callable getCallable() {
            return this.callable;
        }
    }

    public class Union
    extends LayoutElement {
        private final Integral unionTag;
        private final List unionCases;
        private final List defaultCaseBody;

        public Union(String tag, List unionCases, List body) {
            this.unionTag = new Integral(tag);
            this.unionCases = unionCases;
            this.defaultCaseBody = body;
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            LayoutElement element;
            int i;
            this.unionTag.addAttributeToBand(attribute, stream);
            long tag = this.unionTag.latestValue();
            boolean defaultCase = true;
            for (i = 0; i < this.unionCases.size(); ++i) {
                element = (UnionCase)this.unionCases.get(i);
                if (!((UnionCase)element).hasTag(tag)) continue;
                defaultCase = false;
                ((UnionCase)element).addAttributeToBand(attribute, stream);
            }
            if (defaultCase) {
                for (i = 0; i < this.defaultCaseBody.size(); ++i) {
                    element = (LayoutElement)this.defaultCaseBody.get(i);
                    element.addAttributeToBand(attribute, stream);
                }
            }
        }

        @Override
        public void pack(OutputStream out) throws IOException, Pack200Exception {
            this.unionTag.pack(out);
            for (UnionCase unionCase : this.unionCases) {
                unionCase.pack(out);
            }
            for (AttributeLayoutElement layoutElement : this.defaultCaseBody) {
                layoutElement.pack(out);
            }
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
            for (UnionCase unionCase : this.unionCases) {
                unionCase.renumberBci(bciRenumbering, labelsToOffsets);
            }
            for (AttributeLayoutElement layoutElement : this.defaultCaseBody) {
                layoutElement.renumberBci(bciRenumbering, labelsToOffsets);
            }
        }

        public Integral getUnionTag() {
            return this.unionTag;
        }

        public List getUnionCases() {
            return this.unionCases;
        }

        public List getDefaultCaseBody() {
            return this.defaultCaseBody;
        }
    }

    public class Replication
    extends LayoutElement {
        private final Integral countElement;
        private final List layoutElements;

        public Integral getCountElement() {
            return this.countElement;
        }

        public List getLayoutElements() {
            return this.layoutElements;
        }

        public Replication(String tag, String contents) throws IOException {
            LayoutElement e;
            this.layoutElements = new ArrayList();
            this.countElement = new Integral(tag);
            StringReader stream = new StringReader(contents);
            while ((e = NewAttributeBands.this.readNextLayoutElement(stream)) != null) {
                this.layoutElements.add(e);
            }
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            this.countElement.addAttributeToBand(attribute, stream);
            int count = this.countElement.latestValue();
            for (int i = 0; i < count; ++i) {
                for (AttributeLayoutElement layoutElement : this.layoutElements) {
                    layoutElement.addAttributeToBand(attribute, stream);
                }
            }
        }

        @Override
        public void pack(OutputStream out) throws IOException, Pack200Exception {
            this.countElement.pack(out);
            for (AttributeLayoutElement layoutElement : this.layoutElements) {
                layoutElement.pack(out);
            }
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
            for (AttributeLayoutElement layoutElement : this.layoutElements) {
                layoutElement.renumberBci(bciRenumbering, labelsToOffsets);
            }
        }
    }

    public class Integral
    extends LayoutElement {
        private final String tag;
        private final List band;
        private final BHSDCodec defaultCodec;
        private Integral previousIntegral;
        private int previousPValue;

        public Integral(String tag) {
            this.band = new ArrayList();
            this.tag = tag;
            this.defaultCodec = NewAttributeBands.this.getCodec(tag);
        }

        public Integral(String tag, Integral previousIntegral) {
            this.band = new ArrayList();
            this.tag = tag;
            this.defaultCodec = NewAttributeBands.this.getCodec(tag);
            this.previousIntegral = previousIntegral;
        }

        public String getTag() {
            return this.tag;
        }

        @Override
        public void addAttributeToBand(NewAttribute attribute, InputStream stream) {
            Integer val2 = null;
            int value = 0;
            if (this.tag.equals("B") || this.tag.equals("FB")) {
                value = NewAttributeBands.this.readInteger(1, stream) & 0xFF;
            } else if (this.tag.equals("SB")) {
                value = NewAttributeBands.this.readInteger(1, stream);
            } else if (this.tag.equals("H") || this.tag.equals("FH")) {
                value = NewAttributeBands.this.readInteger(2, stream) & 0xFFFF;
            } else if (this.tag.equals("SH")) {
                value = NewAttributeBands.this.readInteger(2, stream);
            } else if (this.tag.equals("I") || this.tag.equals("FI")) {
                value = NewAttributeBands.this.readInteger(4, stream);
            } else if (this.tag.equals("SI")) {
                value = NewAttributeBands.this.readInteger(4, stream);
            } else if (!(this.tag.equals("V") || this.tag.equals("FV") || this.tag.equals("SV"))) {
                if (this.tag.startsWith("PO") || this.tag.startsWith("OS")) {
                    char uint_type = this.tag.substring(2).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    value = NewAttributeBands.this.readInteger(length, stream);
                    val2 = attribute.getLabel(value += this.previousIntegral.previousPValue);
                    this.previousPValue = value;
                } else if (this.tag.startsWith("P")) {
                    char uint_type = this.tag.substring(1).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    value = NewAttributeBands.this.readInteger(length, stream);
                    val2 = attribute.getLabel(value);
                    this.previousPValue = value;
                } else if (this.tag.startsWith("O")) {
                    char uint_type = this.tag.substring(1).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    value = NewAttributeBands.this.readInteger(length, stream);
                    val2 = attribute.getLabel(value += this.previousIntegral.previousPValue);
                    this.previousPValue = value;
                }
            }
            if (val2 == null) {
                val2 = value;
            }
            this.band.add(val2);
        }

        @Override
        public void pack(OutputStream out) throws IOException, Pack200Exception {
            PackingUtils.log("Writing new attribute bands...");
            byte[] encodedBand = NewAttributeBands.this.encodeBandInt(this.tag, NewAttributeBands.this.integerListToArray(this.band), this.defaultCodec);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + this.tag + "[" + this.band.size() + "]");
        }

        public int latestValue() {
            return (Integer)this.band.get(this.band.size() - 1);
        }

        @Override
        public void renumberBci(IntList bciRenumbering, Map labelsToOffsets) {
            if (this.tag.startsWith("O") || this.tag.startsWith("PO")) {
                this.renumberOffsetBci(this.previousIntegral.band, bciRenumbering, labelsToOffsets);
            } else if (this.tag.startsWith("P")) {
                Object label;
                for (int i = this.band.size() - 1; i >= 0 && !((label = this.band.get(i)) instanceof Integer); --i) {
                    if (!(label instanceof Label)) continue;
                    this.band.remove(i);
                    Integer bytecodeIndex = (Integer)labelsToOffsets.get(label);
                    this.band.add(i, bciRenumbering.get(bytecodeIndex));
                }
            }
        }

        private void renumberOffsetBci(List relative, IntList bciRenumbering, Map labelsToOffsets) {
            Object label;
            for (int i = this.band.size() - 1; i >= 0 && !((label = this.band.get(i)) instanceof Integer); --i) {
                if (!(label instanceof Label)) continue;
                this.band.remove(i);
                Integer bytecodeIndex = (Integer)labelsToOffsets.get(label);
                Integer renumberedOffset = bciRenumbering.get(bytecodeIndex) - (Integer)relative.get(i);
                this.band.add(i, renumberedOffset);
            }
        }
    }

    public abstract class LayoutElement
    implements AttributeLayoutElement {
        protected int getLength(char uint_type) {
            int length = 0;
            switch (uint_type) {
                case 'B': {
                    length = 1;
                    break;
                }
                case 'H': {
                    length = 2;
                    break;
                }
                case 'I': {
                    length = 4;
                    break;
                }
                case 'V': {
                    length = 0;
                }
            }
            return length;
        }
    }

    public static interface AttributeLayoutElement {
        public void addAttributeToBand(NewAttribute var1, InputStream var2);

        public void pack(OutputStream var1) throws IOException, Pack200Exception;

        public void renumberBci(IntList var1, Map var2);
    }
}

