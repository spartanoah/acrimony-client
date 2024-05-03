/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.config;

import org.apache.hc.core5.util.Args;

public class NamedElementChain<E> {
    private final Node master = new Node("master", null);
    private int size;

    public NamedElementChain() {
        this.master.previous = this.master;
        this.master.next = this.master;
        this.size = 0;
    }

    public Node getFirst() {
        return this.master.next != this.master ? this.master.next : null;
    }

    public Node getLast() {
        return this.master.previous != this.master ? this.master.previous : null;
    }

    public Node addFirst(E value, String name) {
        Args.notBlank(name, "Name");
        Args.notNull(value, "Value");
        Node newNode = new Node(name, value);
        Node oldNode = this.master.next;
        this.master.next = newNode;
        newNode.previous = this.master;
        newNode.next = oldNode;
        oldNode.previous = newNode;
        ++this.size;
        return newNode;
    }

    public Node addLast(E value, String name) {
        Args.notBlank(name, "Name");
        Args.notNull(value, "Value");
        Node newNode = new Node(name, value);
        Node oldNode = this.master.previous;
        this.master.previous = newNode;
        newNode.previous = oldNode;
        newNode.next = this.master;
        oldNode.next = newNode;
        ++this.size;
        return newNode;
    }

    public Node find(String name) {
        Args.notBlank(name, "Name");
        return this.doFind(name);
    }

    private Node doFind(String name) {
        Node current = this.master.next;
        while (current != this.master) {
            if (name.equals(current.name)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public Node addBefore(String existing, E value, String name) {
        Args.notBlank(name, "Name");
        Args.notNull(value, "Value");
        Node current = this.doFind(existing);
        if (current == null) {
            return null;
        }
        Node newNode = new Node(name, value);
        Node previousNode = current.previous;
        previousNode.next = newNode;
        newNode.previous = previousNode;
        newNode.next = current;
        current.previous = newNode;
        ++this.size;
        return newNode;
    }

    public Node addAfter(String existing, E value, String name) {
        Args.notBlank(name, "Name");
        Args.notNull(value, "Value");
        Node current = this.doFind(existing);
        if (current == null) {
            return null;
        }
        Node newNode = new Node(name, value);
        Node nextNode = current.next;
        current.next = newNode;
        newNode.previous = current;
        newNode.next = nextNode;
        nextNode.previous = newNode;
        ++this.size;
        return newNode;
    }

    public boolean remove(String name) {
        Node node = this.doFind(name);
        if (node == null) {
            return false;
        }
        node.previous.next = node.next;
        node.next.previous = node.previous;
        node.previous = null;
        node.next = null;
        --this.size;
        return true;
    }

    public boolean replace(String existing, E value) {
        Node node = this.doFind(existing);
        if (node == null) {
            return false;
        }
        node.value = value;
        return true;
    }

    public int getSize() {
        return this.size;
    }

    public class Node {
        private final String name;
        private E value;
        private Node previous;
        private Node next;

        Node(String name, E value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public E getValue() {
            return this.value;
        }

        public Node getPrevious() {
            return this.previous != NamedElementChain.this.master ? this.previous : null;
        }

        public Node getNext() {
            return this.next != NamedElementChain.this.master ? this.next : null;
        }

        public String toString() {
            return this.name + ": " + this.value;
        }
    }
}

