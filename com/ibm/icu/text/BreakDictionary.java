/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.util.CompactByteArray;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

class BreakDictionary {
    private char[] reverseColumnMap = null;
    private CompactByteArray columnMap = null;
    private int numCols;
    private short[] table = null;
    private short[] rowIndex = null;
    private int[] rowIndexFlags = null;
    private short[] rowIndexFlagsIndex = null;
    private byte[] rowIndexShifts = null;

    static void writeToFile(String inFile, String outFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        BreakDictionary dictionary = new BreakDictionary(new FileInputStream(inFile));
        PrintWriter out = null;
        if (outFile != null) {
            out = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(outFile), "UnicodeLittle"));
        }
        dictionary.printWordList("", 0, out);
        if (out != null) {
            out.close();
        }
    }

    void printWordList(String partialWord, int state, PrintWriter out) throws IOException {
        if (state == 65535) {
            System.out.println(partialWord);
            if (out != null) {
                out.println(partialWord);
            }
        } else {
            for (int i = 0; i < this.numCols; ++i) {
                int newState = this.at(state, i) & 0xFFFF;
                if (newState == 0) continue;
                char newChar = this.reverseColumnMap[i];
                String newPartialWord = partialWord;
                if (newChar != '\u0000') {
                    newPartialWord = newPartialWord + newChar;
                }
                this.printWordList(newPartialWord, newState, out);
            }
        }
    }

    BreakDictionary(InputStream dictionaryStream) throws IOException {
        this.readDictionaryFile(new DataInputStream(dictionaryStream));
    }

    void readDictionaryFile(DataInputStream in) throws IOException {
        int i;
        in.readInt();
        int l = in.readInt();
        char[] temp = new char[l];
        for (int i2 = 0; i2 < temp.length; ++i2) {
            temp[i2] = (char)in.readShort();
        }
        l = in.readInt();
        byte[] temp2 = new byte[l];
        for (i = 0; i < temp2.length; ++i) {
            temp2[i] = in.readByte();
        }
        this.columnMap = new CompactByteArray(temp, temp2);
        this.numCols = in.readInt();
        in.readInt();
        l = in.readInt();
        this.rowIndex = new short[l];
        for (i = 0; i < this.rowIndex.length; ++i) {
            this.rowIndex[i] = in.readShort();
        }
        l = in.readInt();
        this.rowIndexFlagsIndex = new short[l];
        for (i = 0; i < this.rowIndexFlagsIndex.length; ++i) {
            this.rowIndexFlagsIndex[i] = in.readShort();
        }
        l = in.readInt();
        this.rowIndexFlags = new int[l];
        for (i = 0; i < this.rowIndexFlags.length; ++i) {
            this.rowIndexFlags[i] = in.readInt();
        }
        l = in.readInt();
        this.rowIndexShifts = new byte[l];
        for (i = 0; i < this.rowIndexShifts.length; ++i) {
            this.rowIndexShifts[i] = in.readByte();
        }
        l = in.readInt();
        this.table = new short[l];
        for (i = 0; i < this.table.length; ++i) {
            this.table[i] = in.readShort();
        }
        this.reverseColumnMap = new char[this.numCols];
        for (char c = '\u0000'; c < '\uffff'; c = (char)(c + '\u0001')) {
            byte col = this.columnMap.elementAt(c);
            if (col == 0) continue;
            this.reverseColumnMap[col] = c;
        }
        in.close();
    }

    final short at(int row, char ch) {
        byte col = this.columnMap.elementAt(ch);
        return this.at(row, col);
    }

    final short at(int row, int col) {
        if (this.cellIsPopulated(row, col)) {
            return this.internalAt(this.rowIndex[row], col + this.rowIndexShifts[row]);
        }
        return 0;
    }

    private final boolean cellIsPopulated(int row, int col) {
        if (this.rowIndexFlagsIndex[row] < 0) {
            return col == -this.rowIndexFlagsIndex[row];
        }
        int flags = this.rowIndexFlags[this.rowIndexFlagsIndex[row] + (col >> 5)];
        return (flags & 1 << (col & 0x1F)) != 0;
    }

    private final short internalAt(int row, int col) {
        return this.table[row * this.numCols + col];
    }
}

