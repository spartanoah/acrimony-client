/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class StringTrieBuilder {
    private State state = State.ADDING;
    protected StringBuilder strings = new StringBuilder();
    private Node root;
    private HashMap<Node, Node> nodes = new HashMap();
    private ValueNode lookupFinalValueNode = new ValueNode();

    protected StringTrieBuilder() {
    }

    protected void addImpl(CharSequence s, int value) {
        if (this.state != State.ADDING) {
            throw new IllegalStateException("Cannot add (string, value) pairs after build().");
        }
        if (s.length() > 65535) {
            throw new IndexOutOfBoundsException("The maximum string length is 0xffff.");
        }
        this.root = this.root == null ? this.createSuffixNode(s, 0, value) : this.root.add(this, s, 0, value);
    }

    protected final void buildImpl(Option buildOption) {
        switch (this.state) {
            case ADDING: {
                if (this.root == null) {
                    throw new IndexOutOfBoundsException("No (string, value) pairs were added.");
                }
                if (buildOption == Option.FAST) {
                    this.state = State.BUILDING_FAST;
                    break;
                }
                this.state = State.BUILDING_SMALL;
                break;
            }
            case BUILDING_FAST: 
            case BUILDING_SMALL: {
                throw new IllegalStateException("Builder failed and must be clear()ed.");
            }
            case BUILT: {
                return;
            }
        }
        this.root = this.root.register(this);
        this.root.markRightEdgesFirst(-1);
        this.root.write(this);
        this.state = State.BUILT;
    }

    protected void clearImpl() {
        this.strings.setLength(0);
        this.nodes.clear();
        this.root = null;
        this.state = State.ADDING;
    }

    private final Node registerNode(Node newNode) {
        if (this.state == State.BUILDING_FAST) {
            return newNode;
        }
        Node oldNode = this.nodes.get(newNode);
        if (oldNode != null) {
            return oldNode;
        }
        oldNode = this.nodes.put(newNode, newNode);
        assert (oldNode == null);
        return newNode;
    }

    private final ValueNode registerFinalValue(int value) {
        this.lookupFinalValueNode.setFinalValue(value);
        Node oldNode = this.nodes.get(this.lookupFinalValueNode);
        if (oldNode != null) {
            return (ValueNode)oldNode;
        }
        ValueNode newNode = new ValueNode(value);
        oldNode = this.nodes.put(newNode, newNode);
        assert (oldNode == null);
        return newNode;
    }

    private ValueNode createSuffixNode(CharSequence s, int start, int sValue) {
        ValueNode node = this.registerFinalValue(sValue);
        if (start < s.length()) {
            int offset = this.strings.length();
            this.strings.append(s, start, s.length());
            node = new LinearMatchNode(this.strings, offset, s.length() - start, node);
        }
        return node;
    }

    protected abstract boolean matchNodesCanHaveValues();

    protected abstract int getMaxBranchLinearSubNodeLength();

    protected abstract int getMinLinearMatch();

    protected abstract int getMaxLinearMatchLength();

    protected abstract int write(int var1);

    protected abstract int write(int var1, int var2);

    protected abstract int writeValueAndFinal(int var1, boolean var2);

    protected abstract int writeValueAndType(boolean var1, int var2, int var3);

    protected abstract int writeDeltaTo(int var1);

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static enum State {
        ADDING,
        BUILDING_FAST,
        BUILDING_SMALL,
        BUILT;

    }

    private static final class BranchHeadNode
    extends ValueNode {
        private int length;
        private Node next;

        public BranchHeadNode(int len, Node subNode) {
            this.length = len;
            this.next = subNode;
        }

        public int hashCode() {
            return (0xECCCCBE + this.length) * 37 + this.next.hashCode();
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            BranchHeadNode o = (BranchHeadNode)other;
            return this.length == o.length && this.next == o.next;
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.offset = edgeNumber = this.next.markRightEdgesFirst(edgeNumber);
            }
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.next.write(builder);
            if (this.length <= builder.getMinLinearMatch()) {
                this.offset = builder.writeValueAndType(this.hasValue, this.value, this.length - 1);
            } else {
                builder.write(this.length - 1);
                this.offset = builder.writeValueAndType(this.hasValue, this.value, 0);
            }
        }
    }

    private static final class SplitBranchNode
    extends BranchNode {
        private char unit;
        private Node lessThan;
        private Node greaterOrEqual;

        public SplitBranchNode(char middleUnit, Node lessThanNode, Node greaterOrEqualNode) {
            this.hash = ((206918985 + middleUnit) * 37 + lessThanNode.hashCode()) * 37 + greaterOrEqualNode.hashCode();
            this.unit = middleUnit;
            this.lessThan = lessThanNode;
            this.greaterOrEqual = greaterOrEqualNode;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            SplitBranchNode o = (SplitBranchNode)other;
            return this.unit == o.unit && this.lessThan == o.lessThan && this.greaterOrEqual == o.greaterOrEqual;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.firstEdgeNumber = edgeNumber;
                edgeNumber = this.greaterOrEqual.markRightEdgesFirst(edgeNumber);
                this.offset = edgeNumber = this.lessThan.markRightEdgesFirst(edgeNumber - 1);
            }
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.lessThan.writeUnlessInsideRightEdge(this.firstEdgeNumber, this.greaterOrEqual.getOffset(), builder);
            this.greaterOrEqual.write(builder);
            assert (this.lessThan.getOffset() > 0);
            builder.writeDeltaTo(this.lessThan.getOffset());
            this.offset = builder.write(this.unit);
        }
    }

    private static final class ListBranchNode
    extends BranchNode {
        private Node[] equal;
        private int length;
        private int[] values;
        private char[] units;

        public ListBranchNode(int capacity) {
            this.hash = 0x9DDDDD4 + capacity;
            this.equal = new Node[capacity];
            this.values = new int[capacity];
            this.units = new char[capacity];
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            ListBranchNode o = (ListBranchNode)other;
            for (int i = 0; i < this.length; ++i) {
                if (this.units[i] == o.units[i] && this.values[i] == o.values[i] && this.equal[i] == o.equal[i]) continue;
                return false;
            }
            return true;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.firstEdgeNumber = edgeNumber;
                int step = 0;
                int i = this.length;
                do {
                    Node edge;
                    if ((edge = this.equal[--i]) != null) {
                        edgeNumber = edge.markRightEdgesFirst(edgeNumber - step);
                    }
                    step = 1;
                } while (i > 0);
                this.offset = edgeNumber;
            }
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            int rightEdgeNumber;
            int unitNumber = this.length - 1;
            Node rightEdge = this.equal[unitNumber];
            int n = rightEdgeNumber = rightEdge == null ? this.firstEdgeNumber : rightEdge.getOffset();
            do {
                if (this.equal[--unitNumber] == null) continue;
                this.equal[unitNumber].writeUnlessInsideRightEdge(this.firstEdgeNumber, rightEdgeNumber, builder);
            } while (unitNumber > 0);
            unitNumber = this.length - 1;
            if (rightEdge == null) {
                builder.writeValueAndFinal(this.values[unitNumber], true);
            } else {
                rightEdge.write(builder);
            }
            this.offset = builder.write(this.units[unitNumber]);
            while (--unitNumber >= 0) {
                boolean isFinal;
                int value;
                if (this.equal[unitNumber] == null) {
                    value = this.values[unitNumber];
                    isFinal = true;
                } else {
                    assert (this.equal[unitNumber].getOffset() > 0);
                    value = this.offset - this.equal[unitNumber].getOffset();
                    isFinal = false;
                }
                builder.writeValueAndFinal(value, isFinal);
                this.offset = builder.write(this.units[unitNumber]);
            }
        }

        public void add(int c, int value) {
            this.units[this.length] = (char)c;
            this.equal[this.length] = null;
            this.values[this.length] = value;
            ++this.length;
            this.hash = (this.hash * 37 + c) * 37 + value;
        }

        public void add(int c, Node node) {
            this.units[this.length] = (char)c;
            this.equal[this.length] = node;
            this.values[this.length] = 0;
            ++this.length;
            this.hash = (this.hash * 37 + c) * 37 + node.hashCode();
        }
    }

    private static abstract class BranchNode
    extends Node {
        protected int hash;
        protected int firstEdgeNumber;

        public int hashCode() {
            return this.hash;
        }
    }

    private static final class DynamicBranchNode
    extends ValueNode {
        private StringBuilder chars = new StringBuilder();
        private ArrayList<Node> equal = new ArrayList();

        public void add(char c, Node node) {
            int i = this.find(c);
            this.chars.insert(i, c);
            this.equal.add(i, node);
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            char c;
            int i;
            if (start == s.length()) {
                if (this.hasValue) {
                    throw new IllegalArgumentException("Duplicate string.");
                }
                this.setValue(sValue);
                return this;
            }
            if ((i = this.find(c = s.charAt(start++))) < this.chars.length() && c == this.chars.charAt(i)) {
                this.equal.set(i, this.equal.get(i).add(builder, s, start, sValue));
            } else {
                this.chars.insert(i, c);
                this.equal.add(i, builder.createSuffixNode(s, start, sValue));
            }
            return this;
        }

        public Node register(StringTrieBuilder builder) {
            BranchHeadNode head;
            Node subNode = this.register(builder, 0, this.chars.length());
            ValueNode result = head = new BranchHeadNode(this.chars.length(), subNode);
            if (this.hasValue) {
                if (builder.matchNodesCanHaveValues()) {
                    head.setValue(this.value);
                } else {
                    result = new IntermediateValueNode(this.value, builder.registerNode(head));
                }
            }
            return builder.registerNode(result);
        }

        private Node register(StringTrieBuilder builder, int start, int limit) {
            int length = limit - start;
            if (length > builder.getMaxBranchLinearSubNodeLength()) {
                int middle = start + length / 2;
                return builder.registerNode(new SplitBranchNode(this.chars.charAt(middle), this.register(builder, start, middle), this.register(builder, middle, limit)));
            }
            ListBranchNode listNode = new ListBranchNode(length);
            do {
                char c = this.chars.charAt(start);
                Node node = this.equal.get(start);
                if (node.getClass() == ValueNode.class) {
                    listNode.add((int)c, ((ValueNode)node).value);
                    continue;
                }
                listNode.add((int)c, node.register(builder));
            } while (++start < limit);
            return builder.registerNode(listNode);
        }

        private int find(char c) {
            int start = 0;
            int limit = this.chars.length();
            while (start < limit) {
                int i = (start + limit) / 2;
                char middleChar = this.chars.charAt(i);
                if (c < middleChar) {
                    limit = i;
                    continue;
                }
                if (c == middleChar) {
                    return i;
                }
                start = i + 1;
            }
            return start;
        }
    }

    private static final class LinearMatchNode
    extends ValueNode {
        private CharSequence strings;
        private int stringOffset;
        private int length;
        private Node next;
        private int hash;

        public LinearMatchNode(CharSequence builderStrings, int sOffset, int len, Node nextNode) {
            this.strings = builderStrings;
            this.stringOffset = sOffset;
            this.length = len;
            this.next = nextNode;
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            LinearMatchNode o = (LinearMatchNode)other;
            if (this.length != o.length || this.next != o.next) {
                return false;
            }
            int i = this.stringOffset;
            int j = o.stringOffset;
            int limit = this.stringOffset + this.length;
            while (i < limit) {
                if (this.strings.charAt(i) != this.strings.charAt(j)) {
                    return false;
                }
                ++i;
                ++j;
            }
            return true;
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if (start == s.length()) {
                if (this.hasValue) {
                    throw new IllegalArgumentException("Duplicate string.");
                }
                this.setValue(sValue);
                return this;
            }
            int limit = this.stringOffset + this.length;
            int i = this.stringOffset;
            while (i < limit) {
                char newChar;
                if (start == s.length()) {
                    int prefixLength = i - this.stringOffset;
                    LinearMatchNode suffixNode = new LinearMatchNode(this.strings, i, this.length - prefixLength, this.next);
                    suffixNode.setValue(sValue);
                    this.length = prefixLength;
                    this.next = suffixNode;
                    return this;
                }
                char thisChar = this.strings.charAt(i);
                if (thisChar != (newChar = s.charAt(start))) {
                    ValueNode result;
                    Node thisSuffixNode;
                    DynamicBranchNode branchNode = new DynamicBranchNode();
                    if (i == this.stringOffset) {
                        if (this.hasValue) {
                            branchNode.setValue(this.value);
                            this.value = 0;
                            this.hasValue = false;
                        }
                        ++this.stringOffset;
                        --this.length;
                        thisSuffixNode = this.length > 0 ? this : this.next;
                        result = branchNode;
                    } else if (i == limit - 1) {
                        --this.length;
                        thisSuffixNode = this.next;
                        this.next = branchNode;
                        result = this;
                    } else {
                        int prefixLength = i - this.stringOffset;
                        thisSuffixNode = new LinearMatchNode(this.strings, ++i, this.length - (prefixLength + 1), this.next);
                        this.length = prefixLength;
                        this.next = branchNode;
                        result = this;
                    }
                    ValueNode newSuffixNode = builder.createSuffixNode(s, start + 1, sValue);
                    branchNode.add(thisChar, thisSuffixNode);
                    branchNode.add(newChar, newSuffixNode);
                    return result;
                }
                ++i;
                ++start;
            }
            this.next = this.next.add(builder, s, start, sValue);
            return this;
        }

        public Node register(StringTrieBuilder builder) {
            ValueNode result;
            this.next = this.next.register(builder);
            int maxLinearMatchLength = builder.getMaxLinearMatchLength();
            while (this.length > maxLinearMatchLength) {
                int nextOffset = this.stringOffset + this.length - maxLinearMatchLength;
                this.length -= maxLinearMatchLength;
                LinearMatchNode suffixNode = new LinearMatchNode(this.strings, nextOffset, maxLinearMatchLength, this.next);
                suffixNode.setHashCode();
                this.next = builder.registerNode(suffixNode);
            }
            if (this.hasValue && !builder.matchNodesCanHaveValues()) {
                int intermediateValue = this.value;
                this.value = 0;
                this.hasValue = false;
                this.setHashCode();
                result = new IntermediateValueNode(intermediateValue, builder.registerNode(this));
            } else {
                this.setHashCode();
                result = this;
            }
            return builder.registerNode(result);
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.offset = edgeNumber = this.next.markRightEdgesFirst(edgeNumber);
            }
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.next.write(builder);
            builder.write(this.stringOffset, this.length);
            this.offset = builder.writeValueAndType(this.hasValue, this.value, builder.getMinLinearMatch() + this.length - 1);
        }

        private void setHashCode() {
            this.hash = (124151391 + this.length) * 37 + this.next.hashCode();
            if (this.hasValue) {
                this.hash = this.hash * 37 + this.value;
            }
            int limit = this.stringOffset + this.length;
            for (int i = this.stringOffset; i < limit; ++i) {
                this.hash = this.hash * 37 + this.strings.charAt(i);
            }
        }
    }

    private static final class IntermediateValueNode
    extends ValueNode {
        private Node next;

        public IntermediateValueNode(int v, Node nextNode) {
            this.next = nextNode;
            this.setValue(v);
        }

        public int hashCode() {
            return (0x4EEEEEA + this.value) * 37 + this.next.hashCode();
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            IntermediateValueNode o = (IntermediateValueNode)other;
            return this.next == o.next;
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.offset = edgeNumber = this.next.markRightEdgesFirst(edgeNumber);
            }
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.next.write(builder);
            this.offset = builder.writeValueAndFinal(this.value, false);
        }
    }

    private static class ValueNode
    extends Node {
        protected boolean hasValue;
        protected int value;

        public ValueNode() {
        }

        public ValueNode(int v) {
            this.hasValue = true;
            this.value = v;
        }

        public final void setValue(int v) {
            assert (!this.hasValue);
            this.hasValue = true;
            this.value = v;
        }

        private void setFinalValue(int v) {
            this.hasValue = true;
            this.value = v;
        }

        public int hashCode() {
            int hash = 0x111111;
            if (this.hasValue) {
                hash = hash * 37 + this.value;
            }
            return hash;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            ValueNode o = (ValueNode)other;
            return this.hasValue == o.hasValue && (!this.hasValue || this.value == o.value);
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if (start == s.length()) {
                throw new IllegalArgumentException("Duplicate string.");
            }
            ValueNode node = builder.createSuffixNode(s, start, sValue);
            node.setValue(this.value);
            return node;
        }

        public void write(StringTrieBuilder builder) {
            this.offset = builder.writeValueAndFinal(this.value, true);
        }
    }

    private static abstract class Node {
        protected int offset = 0;

        public abstract int hashCode();

        public boolean equals(Object other) {
            return this == other || this.getClass() == other.getClass();
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            return this;
        }

        public Node register(StringTrieBuilder builder) {
            return this;
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.offset = edgeNumber;
            }
            return edgeNumber;
        }

        public abstract void write(StringTrieBuilder var1);

        public final void writeUnlessInsideRightEdge(int firstRight, int lastRight, StringTrieBuilder builder) {
            if (this.offset < 0 && (this.offset < lastRight || firstRight < this.offset)) {
                this.write(builder);
            }
        }

        public final int getOffset() {
            return this.offset;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Option {
        FAST,
        SMALL;

    }
}

