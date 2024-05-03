/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.compress.harmony.pack200.BHSDCodec;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.AttributeLayout;
import org.apache.commons.compress.harmony.unpack200.BandSet;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPDouble;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPFieldRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPFloat;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPInteger;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPInterfaceMethodRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPLong;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMethodRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPString;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.NewAttribute;

public class NewAttributeBands
extends BandSet {
    private final AttributeLayout attributeLayout;
    private int backwardsCallCount;
    protected List attributeLayoutElements;

    public NewAttributeBands(Segment segment, AttributeLayout attributeLayout) throws IOException {
        super(segment);
        this.attributeLayout = attributeLayout;
        this.parseLayout();
        attributeLayout.setBackwardsCallCount(this.backwardsCallCount);
    }

    @Override
    public void read(InputStream in) throws IOException, Pack200Exception {
    }

    public List parseAttributes(InputStream in, int occurrenceCount) throws IOException, Pack200Exception {
        for (int i = 0; i < this.attributeLayoutElements.size(); ++i) {
            AttributeLayoutElement element = (AttributeLayoutElement)this.attributeLayoutElements.get(i);
            element.readBands(in, occurrenceCount);
        }
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(occurrenceCount);
        for (int i = 0; i < occurrenceCount; ++i) {
            attributes.add(this.getOneAttribute(i, this.attributeLayoutElements));
        }
        return attributes;
    }

    private Attribute getOneAttribute(int index, List elements) {
        NewAttribute attribute = new NewAttribute(this.segment.getCpBands().cpUTF8Value(this.attributeLayout.getName()), this.attributeLayout.getIndex());
        for (int i = 0; i < elements.size(); ++i) {
            AttributeLayoutElement element = (AttributeLayoutElement)elements.get(i);
            element.addToAttribute(index, attribute);
        }
        return attribute;
    }

    private void parseLayout() throws IOException {
        if (this.attributeLayoutElements == null) {
            AttributeLayoutElement e;
            this.attributeLayoutElements = new ArrayList();
            StringReader stream = new StringReader(this.attributeLayout.getLayout());
            while ((e = this.readNextAttributeElement(stream)) != null) {
                this.attributeLayoutElements.add(e);
            }
            this.resolveCalls();
        }
    }

    private void resolveCalls() {
        int backwardsCalls = 0;
        for (int i = 0; i < this.attributeLayoutElements.size(); ++i) {
            AttributeLayoutElement element = (AttributeLayoutElement)this.attributeLayoutElements.get(i);
            if (!(element instanceof Callable)) continue;
            Callable callable = (Callable)element;
            if (i == 0) {
                callable.setFirstCallable(true);
            }
            List body = callable.body;
            for (int iIndex = 0; iIndex < body.size(); ++iIndex) {
                LayoutElement layoutElement = (LayoutElement)body.get(iIndex);
                backwardsCalls += this.resolveCallsForElement(i, callable, layoutElement);
            }
        }
        this.backwardsCallCount = backwardsCalls;
    }

    private int resolveCallsForElement(int i, Callable currentCallable, LayoutElement layoutElement) {
        int backwardsCalls;
        block7: {
            block8: {
                backwardsCalls = 0;
                if (!(layoutElement instanceof Call)) break block8;
                Call call = (Call)layoutElement;
                int index = call.callableIndex;
                if (index == 0) {
                    ++backwardsCalls;
                    call.setCallable(currentCallable);
                } else if (index > 0) {
                    for (int k = i + 1; k < this.attributeLayoutElements.size(); ++k) {
                        AttributeLayoutElement el = (AttributeLayoutElement)this.attributeLayoutElements.get(k);
                        if (!(el instanceof Callable) || --index != 0) continue;
                        call.setCallable((Callable)el);
                        break block7;
                    }
                } else {
                    ++backwardsCalls;
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
                backwardsCalls += this.resolveCallsForElement(i, currentCallable, object);
            }
        }
        return backwardsCalls;
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
                    return new Integral("P" + (char)stream.read());
                }
                return new Integral("PO" + (char)stream.read());
            }
            case 79: {
                stream.mark(1);
                if (stream.read() != 83) {
                    stream.reset();
                    return new Integral("O" + (char)stream.read());
                }
                return new Integral("OS" + (char)stream.read());
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

    public BHSDCodec getCodec(String layoutElement) {
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

    public int getBackwardsCallCount() {
        return this.backwardsCallCount;
    }

    public void setBackwardsCalls(int[] backwardsCalls) throws IOException {
        int index = 0;
        this.parseLayout();
        for (int i = 0; i < this.attributeLayoutElements.size(); ++i) {
            AttributeLayoutElement element = (AttributeLayoutElement)this.attributeLayoutElements.get(i);
            if (!(element instanceof Callable) || !((Callable)element).isBackwardsCallable()) continue;
            ((Callable)element).addCount(backwardsCalls[index]);
            ++index;
        }
    }

    @Override
    public void unpack() throws IOException, Pack200Exception {
    }

    public class UnionCase
    extends LayoutElement {
        private List body;
        private final List tags;

        public UnionCase(List tags) {
            this.tags = tags;
        }

        public boolean hasTag(long l) {
            return this.tags.contains((int)l);
        }

        public UnionCase(List tags, List body) throws IOException {
            this.tags = tags;
            this.body = body;
        }

        @Override
        public void readBands(InputStream in, int count) throws IOException, Pack200Exception {
            if (this.body != null) {
                for (int i = 0; i < this.body.size(); ++i) {
                    LayoutElement element = (LayoutElement)this.body.get(i);
                    element.readBands(in, count);
                }
            }
        }

        @Override
        public void addToAttribute(int index, NewAttribute attribute) {
            if (this.body != null) {
                for (int i = 0; i < this.body.size(); ++i) {
                    LayoutElement element = (LayoutElement)this.body.get(i);
                    element.addToAttribute(index, attribute);
                }
            }
        }

        public List getBody() {
            return this.body == null ? Collections.EMPTY_LIST : this.body;
        }
    }

    public static class Callable
    implements AttributeLayoutElement {
        private final List body;
        private boolean isBackwardsCallable;
        private boolean isFirstCallable;
        private int count;
        private int index;

        public Callable(List body) throws IOException {
            this.body = body;
        }

        public void addNextToAttribute(NewAttribute attribute) {
            for (int i = 0; i < this.body.size(); ++i) {
                LayoutElement element = (LayoutElement)this.body.get(i);
                element.addToAttribute(this.index, attribute);
            }
            ++this.index;
        }

        public void addCount(int count) {
            this.count += count;
        }

        @Override
        public void readBands(InputStream in, int count) throws IOException, Pack200Exception {
            count = this.isFirstCallable ? (count += this.count) : this.count;
            for (int i = 0; i < this.body.size(); ++i) {
                LayoutElement element = (LayoutElement)this.body.get(i);
                element.readBands(in, count);
            }
        }

        @Override
        public void addToAttribute(int n, NewAttribute attribute) {
            if (this.isFirstCallable) {
                for (int i = 0; i < this.body.size(); ++i) {
                    LayoutElement element = (LayoutElement)this.body.get(i);
                    element.addToAttribute(this.index, attribute);
                }
                ++this.index;
            }
        }

        public boolean isBackwardsCallable() {
            return this.isBackwardsCallable;
        }

        public void setBackwardsCallable() {
            this.isBackwardsCallable = true;
        }

        public void setFirstCallable(boolean isFirstCallable) {
            this.isFirstCallable = isFirstCallable;
        }

        public List getBody() {
            return this.body;
        }
    }

    public class Reference
    extends LayoutElement {
        private final String tag;
        private Object band;
        private final int length;

        public Reference(String tag) {
            this.tag = tag;
            this.length = this.getLength(tag.charAt(tag.length() - 1));
        }

        @Override
        public void readBands(InputStream in, int count) throws IOException, Pack200Exception {
            if (this.tag.startsWith("KI")) {
                this.band = NewAttributeBands.this.parseCPIntReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("KJ")) {
                this.band = NewAttributeBands.this.parseCPLongReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("KF")) {
                this.band = NewAttributeBands.this.parseCPFloatReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("KD")) {
                this.band = NewAttributeBands.this.parseCPDoubleReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("KS")) {
                this.band = NewAttributeBands.this.parseCPStringReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RC")) {
                this.band = NewAttributeBands.this.parseCPClassReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RS")) {
                this.band = NewAttributeBands.this.parseCPSignatureReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RD")) {
                this.band = NewAttributeBands.this.parseCPDescriptorReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RF")) {
                this.band = NewAttributeBands.this.parseCPFieldRefReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RM")) {
                this.band = NewAttributeBands.this.parseCPMethodRefReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RI")) {
                this.band = NewAttributeBands.this.parseCPInterfaceMethodRefReferences(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            } else if (this.tag.startsWith("RU")) {
                this.band = NewAttributeBands.this.parseCPUTF8References(NewAttributeBands.this.attributeLayout.getName(), in, Codec.UNSIGNED5, count);
            }
        }

        @Override
        public void addToAttribute(int n, NewAttribute attribute) {
            if (this.tag.startsWith("KI")) {
                attribute.addToBody(this.length, ((CPInteger[])this.band)[n]);
            } else if (this.tag.startsWith("KJ")) {
                attribute.addToBody(this.length, ((CPLong[])this.band)[n]);
            } else if (this.tag.startsWith("KF")) {
                attribute.addToBody(this.length, ((CPFloat[])this.band)[n]);
            } else if (this.tag.startsWith("KD")) {
                attribute.addToBody(this.length, ((CPDouble[])this.band)[n]);
            } else if (this.tag.startsWith("KS")) {
                attribute.addToBody(this.length, ((CPString[])this.band)[n]);
            } else if (this.tag.startsWith("RC")) {
                attribute.addToBody(this.length, ((CPClass[])this.band)[n]);
            } else if (this.tag.startsWith("RS")) {
                attribute.addToBody(this.length, ((CPUTF8[])this.band)[n]);
            } else if (this.tag.startsWith("RD")) {
                attribute.addToBody(this.length, ((CPNameAndType[])this.band)[n]);
            } else if (this.tag.startsWith("RF")) {
                attribute.addToBody(this.length, ((CPFieldRef[])this.band)[n]);
            } else if (this.tag.startsWith("RM")) {
                attribute.addToBody(this.length, ((CPMethodRef[])this.band)[n]);
            } else if (this.tag.startsWith("RI")) {
                attribute.addToBody(this.length, ((CPInterfaceMethodRef[])this.band)[n]);
            } else if (this.tag.startsWith("RU")) {
                attribute.addToBody(this.length, ((CPUTF8[])this.band)[n]);
            }
        }

        public String getTag() {
            return this.tag;
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
        public void readBands(InputStream in, int count) {
            if (this.callableIndex > 0) {
                this.callable.addCount(count);
            }
        }

        @Override
        public void addToAttribute(int n, NewAttribute attribute) {
            this.callable.addNextToAttribute(attribute);
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
        private int[] caseCounts;
        private int defaultCount;

        public Union(String tag, List unionCases, List body) {
            this.unionTag = new Integral(tag);
            this.unionCases = unionCases;
            this.defaultCaseBody = body;
        }

        @Override
        public void readBands(InputStream in, int count) throws IOException, Pack200Exception {
            int i;
            this.unionTag.readBands(in, count);
            int[] values = this.unionTag.band;
            this.caseCounts = new int[this.unionCases.size()];
            for (i = 0; i < this.caseCounts.length; ++i) {
                UnionCase unionCase = (UnionCase)this.unionCases.get(i);
                for (int j = 0; j < values.length; ++j) {
                    if (!unionCase.hasTag(values[j])) continue;
                    int n = i;
                    this.caseCounts[n] = this.caseCounts[n] + 1;
                }
                unionCase.readBands(in, this.caseCounts[i]);
            }
            for (i = 0; i < values.length; ++i) {
                boolean found = false;
                for (int it = 0; it < this.unionCases.size(); ++it) {
                    UnionCase unionCase = (UnionCase)this.unionCases.get(it);
                    if (!unionCase.hasTag(values[i])) continue;
                    found = true;
                }
                if (found) continue;
                ++this.defaultCount;
            }
            if (this.defaultCaseBody != null) {
                for (i = 0; i < this.defaultCaseBody.size(); ++i) {
                    LayoutElement element = (LayoutElement)this.defaultCaseBody.get(i);
                    element.readBands(in, this.defaultCount);
                }
            }
        }

        @Override
        public void addToAttribute(int n, NewAttribute attribute) {
            this.unionTag.addToAttribute(n, attribute);
            int offset = 0;
            int[] tagBand = this.unionTag.band;
            long tag = this.unionTag.getValue(n);
            boolean defaultCase = true;
            for (int i = 0; i < this.unionCases.size(); ++i) {
                UnionCase element = (UnionCase)this.unionCases.get(i);
                if (!element.hasTag(tag)) continue;
                defaultCase = false;
                for (int j = 0; j < n; ++j) {
                    if (!element.hasTag(tagBand[j])) continue;
                    ++offset;
                }
                element.addToAttribute(offset, attribute);
            }
            if (defaultCase) {
                int defaultOffset = 0;
                for (int j = 0; j < n; ++j) {
                    boolean found = false;
                    for (int i = 0; i < this.unionCases.size(); ++i) {
                        UnionCase element = (UnionCase)this.unionCases.get(i);
                        if (!element.hasTag(tagBand[j])) continue;
                        found = true;
                    }
                    if (found) continue;
                    ++defaultOffset;
                }
                if (this.defaultCaseBody != null) {
                    for (int i = 0; i < this.defaultCaseBody.size(); ++i) {
                        LayoutElement element = (LayoutElement)this.defaultCaseBody.get(i);
                        element.addToAttribute(defaultOffset, attribute);
                    }
                }
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
        public void readBands(InputStream in, int count) throws IOException, Pack200Exception {
            int i;
            this.countElement.readBands(in, count);
            int arrayCount = 0;
            for (i = 0; i < count; ++i) {
                arrayCount = (int)((long)arrayCount + this.countElement.getValue(i));
            }
            for (i = 0; i < this.layoutElements.size(); ++i) {
                LayoutElement element = (LayoutElement)this.layoutElements.get(i);
                element.readBands(in, arrayCount);
            }
        }

        @Override
        public void addToAttribute(int index, NewAttribute attribute) {
            this.countElement.addToAttribute(index, attribute);
            int offset = 0;
            for (int i = 0; i < index; ++i) {
                offset = (int)((long)offset + this.countElement.getValue(i));
            }
            long numElements = this.countElement.getValue(index);
            int i = offset;
            while ((long)i < (long)offset + numElements) {
                for (int it = 0; it < this.layoutElements.size(); ++it) {
                    LayoutElement element = (LayoutElement)this.layoutElements.get(it);
                    element.addToAttribute(i, attribute);
                }
                ++i;
            }
        }

        public Integral getCountElement() {
            return this.countElement;
        }

        public List getLayoutElements() {
            return this.layoutElements;
        }
    }

    public class Integral
    extends LayoutElement {
        private final String tag;
        private int[] band;

        public Integral(String tag) {
            this.tag = tag;
        }

        @Override
        public void readBands(InputStream in, int count) throws IOException, Pack200Exception {
            this.band = NewAttributeBands.this.decodeBandInt(NewAttributeBands.this.attributeLayout.getName() + "_" + this.tag, in, NewAttributeBands.this.getCodec(this.tag), count);
        }

        @Override
        public void addToAttribute(int n, NewAttribute attribute) {
            long value = this.band[n];
            if (this.tag.equals("B") || this.tag.equals("FB")) {
                attribute.addInteger(1, value);
            } else if (this.tag.equals("SB")) {
                attribute.addInteger(1, (byte)value);
            } else if (this.tag.equals("H") || this.tag.equals("FH")) {
                attribute.addInteger(2, value);
            } else if (this.tag.equals("SH")) {
                attribute.addInteger(2, (short)value);
            } else if (this.tag.equals("I") || this.tag.equals("FI")) {
                attribute.addInteger(4, value);
            } else if (this.tag.equals("SI")) {
                attribute.addInteger(4, (int)value);
            } else if (!(this.tag.equals("V") || this.tag.equals("FV") || this.tag.equals("SV"))) {
                if (this.tag.startsWith("PO")) {
                    char uint_type = this.tag.substring(2).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    attribute.addBCOffset(length, (int)value);
                } else if (this.tag.startsWith("P")) {
                    char uint_type = this.tag.substring(1).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    attribute.addBCIndex(length, (int)value);
                } else if (this.tag.startsWith("OS")) {
                    char uint_type = this.tag.substring(2).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    if (length == 1) {
                        value = (byte)value;
                    } else if (length == 2) {
                        value = (short)value;
                    } else if (length == 4) {
                        value = (int)value;
                    }
                    attribute.addBCLength(length, (int)value);
                } else if (this.tag.startsWith("O")) {
                    char uint_type = this.tag.substring(1).toCharArray()[0];
                    int length = this.getLength(uint_type);
                    attribute.addBCLength(length, (int)value);
                }
            }
        }

        long getValue(int index) {
            return this.band[index];
        }

        public String getTag() {
            return this.tag;
        }
    }

    private abstract class LayoutElement
    implements AttributeLayoutElement {
        private LayoutElement() {
        }

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

    private static interface AttributeLayoutElement {
        public void readBands(InputStream var1, int var2) throws IOException, Pack200Exception;

        public void addToAttribute(int var1, NewAttribute var2);
    }
}

