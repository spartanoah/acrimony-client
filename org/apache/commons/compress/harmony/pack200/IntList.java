/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.util.Arrays;

public class IntList {
    private int[] array;
    private int firstIndex;
    private int lastIndex;
    private int modCount;

    public IntList() {
        this(10);
    }

    public IntList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        this.lastIndex = 0;
        this.firstIndex = 0;
        this.array = new int[capacity];
    }

    public boolean add(int object) {
        if (this.lastIndex == this.array.length) {
            this.growAtEnd(1);
        }
        this.array[this.lastIndex++] = object;
        ++this.modCount;
        return true;
    }

    public void add(int location, int object) {
        int size = this.lastIndex - this.firstIndex;
        if (0 < location && location < size) {
            if (this.firstIndex == 0 && this.lastIndex == this.array.length) {
                this.growForInsert(location, 1);
            } else if (location < size / 2 && this.firstIndex > 0 || this.lastIndex == this.array.length) {
                System.arraycopy(this.array, this.firstIndex, this.array, --this.firstIndex, location);
            } else {
                int index = location + this.firstIndex;
                System.arraycopy(this.array, index, this.array, index + 1, size - location);
                ++this.lastIndex;
            }
            this.array[location + this.firstIndex] = object;
        } else if (location == 0) {
            if (this.firstIndex == 0) {
                this.growAtFront(1);
            }
            this.array[--this.firstIndex] = object;
        } else if (location == size) {
            if (this.lastIndex == this.array.length) {
                this.growAtEnd(1);
            }
            this.array[this.lastIndex++] = object;
        } else {
            throw new IndexOutOfBoundsException();
        }
        ++this.modCount;
    }

    public void clear() {
        if (this.firstIndex != this.lastIndex) {
            Arrays.fill(this.array, this.firstIndex, this.lastIndex, -1);
            this.lastIndex = 0;
            this.firstIndex = 0;
            ++this.modCount;
        }
    }

    public int get(int location) {
        if (0 <= location && location < this.lastIndex - this.firstIndex) {
            return this.array[this.firstIndex + location];
        }
        throw new IndexOutOfBoundsException("" + location);
    }

    private void growAtEnd(int required) {
        int size = this.lastIndex - this.firstIndex;
        if (this.firstIndex >= required - (this.array.length - this.lastIndex)) {
            int newLast = this.lastIndex - this.firstIndex;
            if (size > 0) {
                System.arraycopy(this.array, this.firstIndex, this.array, 0, size);
            }
            this.firstIndex = 0;
            this.lastIndex = newLast;
        } else {
            int increment = size / 2;
            if (required > increment) {
                increment = required;
            }
            if (increment < 12) {
                increment = 12;
            }
            int[] newArray = new int[size + increment];
            if (size > 0) {
                System.arraycopy(this.array, this.firstIndex, newArray, 0, size);
                this.firstIndex = 0;
                this.lastIndex = size;
            }
            this.array = newArray;
        }
    }

    private void growAtFront(int required) {
        int size = this.lastIndex - this.firstIndex;
        if (this.array.length - this.lastIndex + this.firstIndex >= required) {
            int newFirst = this.array.length - size;
            if (size > 0) {
                System.arraycopy(this.array, this.firstIndex, this.array, newFirst, size);
            }
            this.firstIndex = newFirst;
            this.lastIndex = this.array.length;
        } else {
            int increment = size / 2;
            if (required > increment) {
                increment = required;
            }
            if (increment < 12) {
                increment = 12;
            }
            int[] newArray = new int[size + increment];
            if (size > 0) {
                System.arraycopy(this.array, this.firstIndex, newArray, newArray.length - size, size);
            }
            this.firstIndex = newArray.length - size;
            this.lastIndex = newArray.length;
            this.array = newArray;
        }
    }

    private void growForInsert(int location, int required) {
        int size = this.lastIndex - this.firstIndex;
        int increment = size / 2;
        if (required > increment) {
            increment = required;
        }
        if (increment < 12) {
            increment = 12;
        }
        int[] newArray = new int[size + increment];
        int newFirst = increment - required;
        System.arraycopy(this.array, location + this.firstIndex, newArray, newFirst + location + required, size - location);
        System.arraycopy(this.array, this.firstIndex, newArray, newFirst, location);
        this.firstIndex = newFirst;
        this.lastIndex = size + increment;
        this.array = newArray;
    }

    public void increment(int location) {
        if (0 > location || location >= this.lastIndex - this.firstIndex) {
            throw new IndexOutOfBoundsException("" + location);
        }
        int n = this.firstIndex + location;
        this.array[n] = this.array[n] + 1;
    }

    public boolean isEmpty() {
        return this.lastIndex == this.firstIndex;
    }

    public int remove(int location) {
        int result;
        int size = this.lastIndex - this.firstIndex;
        if (0 > location || location >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (location == size - 1) {
            result = this.array[--this.lastIndex];
            this.array[this.lastIndex] = 0;
        } else if (location == 0) {
            result = this.array[this.firstIndex];
            this.array[this.firstIndex++] = 0;
        } else {
            int elementIndex = this.firstIndex + location;
            result = this.array[elementIndex];
            if (location < size / 2) {
                System.arraycopy(this.array, this.firstIndex, this.array, this.firstIndex + 1, location);
                this.array[this.firstIndex++] = 0;
            } else {
                System.arraycopy(this.array, elementIndex + 1, this.array, elementIndex, size - location - 1);
                this.array[--this.lastIndex] = 0;
            }
        }
        if (this.firstIndex == this.lastIndex) {
            this.lastIndex = 0;
            this.firstIndex = 0;
        }
        ++this.modCount;
        return result;
    }

    public int size() {
        return this.lastIndex - this.firstIndex;
    }

    public int[] toArray() {
        int size = this.lastIndex - this.firstIndex;
        int[] result = new int[size];
        System.arraycopy(this.array, this.firstIndex, result, 0, size);
        return result;
    }

    public void addAll(IntList list) {
        this.growAtEnd(list.size());
        for (int i = 0; i < list.size(); ++i) {
            this.add(list.get(i));
        }
    }
}

