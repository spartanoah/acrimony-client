/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.diffutils;

import me.xdrop.diffutils.structs.EditOp;
import me.xdrop.diffutils.structs.EditType;
import me.xdrop.diffutils.structs.MatchingBlock;
import me.xdrop.diffutils.structs.OpCode;

public class DiffUtils {
    public static EditOp[] getEditOps(String s1, String s2) {
        return DiffUtils.getEditOps(s1.length(), s1, s2.length(), s2);
    }

    private static EditOp[] getEditOps(int len1, String s1, int len2, String s2) {
        int i;
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        int p1 = 0;
        int p2 = 0;
        int len1o = 0;
        while (len1 > 0 && len2 > 0 && c1[p1] == c2[p2]) {
            --len1;
            --len2;
            ++p1;
            ++p2;
            ++len1o;
        }
        int len2o = len1o;
        while (len1 > 0 && len2 > 0 && c1[p1 + len1 - 1] == c2[p2 + len2 - 1]) {
            --len1;
            --len2;
        }
        int[] matrix = new int[++len2 * ++len1];
        for (i = 0; i < len2; ++i) {
            matrix[i] = i;
        }
        for (i = 1; i < len1; ++i) {
            matrix[len2 * i] = i;
        }
        for (i = 1; i < len1; ++i) {
            int ptrPrev = (i - 1) * len2;
            int ptrC = i * len2;
            int ptrEnd = ptrC + len2 - 1;
            char char1 = c1[p1 + i - 1];
            int ptrChar2 = p2;
            int x = i;
            ++ptrC;
            while (ptrC <= ptrEnd) {
                int c3;
                if (++x > (c3 = matrix[ptrPrev++] + (char1 != c2[ptrChar2++] ? 1 : 0))) {
                    x = c3;
                }
                if (x > (c3 = matrix[ptrPrev] + 1)) {
                    x = c3;
                }
                matrix[ptrC++] = x;
            }
        }
        return DiffUtils.editOpsFromCostMatrix(len1, c1, p1, len1o, len2, c2, p2, len2o, matrix);
    }

    private static EditOp[] editOpsFromCostMatrix(int len1, char[] c1, int p1, int o1, int len2, char[] c2, int p2, int o2, int[] matrix) {
        int dir = 0;
        int pos = matrix[len1 * len2 - 1];
        EditOp[] ops = new EditOp[pos];
        int i = len1 - 1;
        int j = len2 - 1;
        int ptr = len1 * len2 - 1;
        while (i > 0 || j > 0) {
            EditOp eop;
            if (i != 0 && j != 0 && matrix[ptr] == matrix[ptr - len2 - 1] && c1[p1 + i - 1] == c2[p2 + j - 1]) {
                --i;
                --j;
                ptr -= len2 + 1;
                dir = 0;
                continue;
            }
            if (dir < 0 && j != 0 && matrix[ptr] == matrix[ptr - 1] + 1) {
                eop = new EditOp();
                ops[--pos] = eop;
                eop.type = EditType.INSERT;
                eop.spos = i + o1;
                eop.dpos = --j + o2;
                --ptr;
                continue;
            }
            if (dir > 0 && i != 0 && matrix[ptr] == matrix[ptr - len2] + 1) {
                eop = new EditOp();
                ops[--pos] = eop;
                eop.type = EditType.DELETE;
                eop.spos = --i + o1;
                eop.dpos = j + o2;
                ptr -= len2;
                continue;
            }
            if (i != 0 && j != 0 && matrix[ptr] == matrix[ptr - len2 - 1] + 1) {
                ops[--pos] = eop = new EditOp();
                eop.type = EditType.REPLACE;
                eop.spos = --i + o1;
                eop.dpos = --j + o2;
                ptr -= len2 + 1;
                dir = 0;
                continue;
            }
            if (dir == 0 && j != 0 && matrix[ptr] == matrix[ptr - 1] + 1) {
                ops[--pos] = eop = new EditOp();
                eop.type = EditType.INSERT;
                eop.spos = i + o1;
                eop.dpos = --j + o2;
                --ptr;
                dir = -1;
                continue;
            }
            if (dir == 0 && i != 0 && matrix[ptr] == matrix[ptr - len2] + 1) {
                ops[--pos] = eop = new EditOp();
                eop.type = EditType.DELETE;
                eop.spos = --i + o1;
                eop.dpos = j + o2;
                ptr -= len2;
                dir = 1;
                continue;
            }
            assert (false);
        }
        return ops;
    }

    public static MatchingBlock[] getMatchingBlocks(String s1, String s2) {
        return DiffUtils.getMatchingBlocks(s1.length(), s2.length(), DiffUtils.getEditOps(s1, s2));
    }

    public static MatchingBlock[] getMatchingBlocks(int len1, int len2, OpCode[] ops) {
        int n = ops.length;
        int o = 0;
        int noOfMB = 0;
        int i = n;
        while (i-- != 0) {
            if (ops[o].type == EditType.KEEP) {
                ++noOfMB;
                while (i != 0 && ops[o].type == EditType.KEEP) {
                    --i;
                    ++o;
                }
                if (i == 0) break;
            }
            ++o;
        }
        MatchingBlock[] matchingBlocks = new MatchingBlock[noOfMB + 1];
        int mb = 0;
        o = 0;
        matchingBlocks[mb] = new MatchingBlock();
        i = n;
        while (i != 0) {
            if (ops[o].type == EditType.KEEP) {
                matchingBlocks[mb].spos = ops[o].sbeg;
                matchingBlocks[mb].dpos = ops[o].dbeg;
                while (i != 0 && ops[o].type == EditType.KEEP) {
                    --i;
                    ++o;
                }
                if (i == 0) {
                    matchingBlocks[mb].length = len1 - matchingBlocks[mb].spos;
                    ++mb;
                    break;
                }
                matchingBlocks[mb].length = ops[o].sbeg - matchingBlocks[mb].spos;
                matchingBlocks[++mb] = new MatchingBlock();
            }
            --i;
            ++o;
        }
        assert (mb == noOfMB);
        MatchingBlock finalBlock = new MatchingBlock();
        finalBlock.spos = len1;
        finalBlock.dpos = len2;
        finalBlock.length = 0;
        matchingBlocks[mb] = finalBlock;
        return matchingBlocks;
    }

    private static MatchingBlock[] getMatchingBlocks(int len1, int len2, EditOp[] ops) {
        MatchingBlock mb;
        EditType type;
        int n = ops.length;
        int numberOfMatchingBlocks = 0;
        int o = 0;
        int dpos = 0;
        int spos = 0;
        int i = n;
        block10: while (i != 0) {
            while (ops[o].type == EditType.KEEP && --i != 0) {
                ++o;
            }
            if (i == 0) break;
            if (spos < ops[o].spos || dpos < ops[o].dpos) {
                ++numberOfMatchingBlocks;
                spos = ops[o].spos;
                dpos = ops[o].dpos;
            }
            type = ops[o].type;
            switch (type) {
                case REPLACE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    continue block10;
                }
                case DELETE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && dpos == ops[o].dpos) {
                    }
                    continue block10;
                }
                case INSERT: {
                    while (--i != 0 && ops[++o].type == type && spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    continue block10;
                }
            }
        }
        if (spos < len1 || dpos < len2) {
            ++numberOfMatchingBlocks;
        }
        MatchingBlock[] matchingBlocks = new MatchingBlock[numberOfMatchingBlocks + 1];
        o = 0;
        dpos = 0;
        spos = 0;
        int mbIndex = 0;
        i = n;
        block15: while (i != 0) {
            while (ops[o].type == EditType.KEEP && --i != 0) {
                ++o;
            }
            if (i == 0) break;
            if (spos < ops[o].spos || dpos < ops[o].dpos) {
                mb = new MatchingBlock();
                mb.spos = spos;
                mb.dpos = dpos;
                mb.length = ops[o].spos - spos;
                spos = ops[o].spos;
                dpos = ops[o].dpos;
                matchingBlocks[mbIndex++] = mb;
            }
            type = ops[o].type;
            switch (type) {
                case REPLACE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    continue block15;
                }
                case DELETE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && dpos == ops[o].dpos) {
                    }
                    continue block15;
                }
                case INSERT: {
                    while (--i != 0 && ops[++o].type == type && spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    continue block15;
                }
            }
        }
        if (spos < len1 || dpos < len2) {
            assert (len1 - spos == len2 - dpos);
            mb = new MatchingBlock();
            mb.spos = spos;
            mb.dpos = dpos;
            mb.length = len1 - spos;
            matchingBlocks[mbIndex++] = mb;
        }
        assert (numberOfMatchingBlocks == mbIndex);
        MatchingBlock finalBlock = new MatchingBlock();
        finalBlock.spos = len1;
        finalBlock.dpos = len2;
        finalBlock.length = 0;
        matchingBlocks[mbIndex] = finalBlock;
        return matchingBlocks;
    }

    private static OpCode[] editOpsToOpCodes(EditOp[] ops, int len1, int len2) {
        EditType type;
        int n = ops.length;
        int o = 0;
        int noOfBlocks = 0;
        int dpos = 0;
        int spos = 0;
        int i = n;
        block10: while (i != 0) {
            while (ops[o].type == EditType.KEEP && --i != 0) {
                ++o;
            }
            if (i == 0) break;
            if (spos < ops[o].spos || dpos < ops[o].dpos) {
                ++noOfBlocks;
                spos = ops[o].spos;
                dpos = ops[o].dpos;
            }
            ++noOfBlocks;
            type = ops[o].type;
            switch (type) {
                case REPLACE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    continue block10;
                }
                case DELETE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && dpos == ops[o].dpos) {
                    }
                    continue block10;
                }
                case INSERT: {
                    while (--i != 0 && ops[++o].type == type && spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    continue block10;
                }
            }
        }
        if (spos < len1 || dpos < len2) {
            ++noOfBlocks;
        }
        OpCode[] opCodes = new OpCode[noOfBlocks];
        o = 0;
        dpos = 0;
        spos = 0;
        int oIndex = 0;
        i = n;
        while (i != 0) {
            OpCode oc;
            while (ops[o].type == EditType.KEEP && --i != 0) {
                ++o;
            }
            if (i == 0) break;
            opCodes[oIndex] = oc = new OpCode();
            oc.sbeg = spos;
            oc.dbeg = dpos;
            if (spos < ops[o].spos || dpos < ops[o].dpos) {
                OpCode oc2;
                oc.type = EditType.KEEP;
                spos = oc.send = ops[o].spos;
                dpos = oc.dend = ops[o].dpos;
                opCodes[++oIndex] = oc2 = new OpCode();
                oc2.sbeg = spos;
                oc2.dbeg = dpos;
            }
            type = ops[o].type;
            switch (type) {
                case REPLACE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    break;
                }
                case DELETE: {
                    while (--i != 0 && ops[++o].type == type && ++spos == ops[o].spos && dpos == ops[o].dpos) {
                    }
                    break;
                }
                case INSERT: {
                    while (--i != 0 && ops[++o].type == type && spos == ops[o].spos && ++dpos == ops[o].dpos) {
                    }
                    break;
                }
            }
            opCodes[oIndex].type = type;
            opCodes[oIndex].send = spos;
            opCodes[oIndex].dend = dpos;
            ++oIndex;
        }
        if (spos < len1 || dpos < len2) {
            assert (len1 - spos == len2 - dpos);
            if (opCodes[oIndex] == null) {
                opCodes[oIndex] = new OpCode();
            }
            opCodes[oIndex].type = EditType.KEEP;
            opCodes[oIndex].sbeg = spos;
            opCodes[oIndex].dbeg = dpos;
            opCodes[oIndex].send = len1;
            opCodes[oIndex].dend = len2;
            ++oIndex;
        }
        assert (oIndex == noOfBlocks);
        return opCodes;
    }

    public static int levEditDistance(String s1, String s2, int xcost) {
        int i;
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        int str1 = 0;
        int str2 = 0;
        int len1 = s1.length();
        int len2 = s2.length();
        while (len1 > 0 && len2 > 0 && c1[str1] == c2[str2]) {
            --len1;
            --len2;
            ++str1;
            ++str2;
        }
        while (len1 > 0 && len2 > 0 && c1[str1 + len1 - 1] == c2[str2 + len2 - 1]) {
            --len1;
            --len2;
        }
        if (len1 == 0) {
            return len2;
        }
        if (len2 == 0) {
            return len1;
        }
        if (len1 > len2) {
            int nx = len1;
            int temp = str1;
            len1 = len2;
            len2 = nx;
            str1 = str2;
            str2 = temp;
            char[] t = c2;
            c2 = c1;
            c1 = t;
        }
        if (len1 == 1) {
            if (xcost != 0) {
                return len2 + 1 - 2 * DiffUtils.memchr(c2, str2, c1[str1], len2);
            }
            return len2 - DiffUtils.memchr(c2, str2, c1[str1], len2);
        }
        int half = ++len1 >> 1;
        int[] row = new int[++len2];
        int end = len2 - 1;
        for (i = 0; i < len2 - (xcost != 0 ? 0 : half); ++i) {
            row[i] = i;
        }
        if (xcost != 0) {
            for (i = 1; i < len1; ++i) {
                int p = 1;
                char ch1 = c1[str1 + i - 1];
                int c2p = str2;
                int D = i;
                int x = i;
                while (p <= end) {
                    x = ch1 == c2[c2p++] ? --D : ++x;
                    D = row[p];
                    if (x > ++D) {
                        x = D;
                    }
                    row[p++] = x;
                }
            }
        } else {
            row[0] = len1 - half - 1;
            for (i = 1; i < len1; ++i) {
                int c3;
                int D;
                int x;
                int p;
                int c2p;
                char ch1 = c1[str1 + i - 1];
                if (i >= len1 - half) {
                    int offset = i - (len1 - half);
                    c2p = str2 + offset;
                    p = offset;
                    int c32 = row[p++] + (ch1 != c2[c2p++] ? 1 : 0);
                    x = row[p];
                    D = ++x;
                    if (x > c32) {
                        x = c32;
                    }
                    row[p++] = x;
                } else {
                    p = 1;
                    c2p = str2;
                    D = x = i;
                }
                if (i <= half + 1) {
                    end = len2 + i - half - 2;
                }
                while (p <= end) {
                    if (++x > (c3 = --D + (ch1 != c2[c2p++] ? 1 : 0))) {
                        x = c3;
                    }
                    D = row[p];
                    if (x > ++D) {
                        x = D;
                    }
                    row[p++] = x;
                }
                if (i > half) continue;
                if (++x > (c3 = --D + (ch1 != c2[c2p] ? 1 : 0))) {
                    x = c3;
                }
                row[p] = x;
            }
        }
        i = row[end];
        return i;
    }

    private static int memchr(char[] haystack, int offset, char needle, int num) {
        if (num != 0) {
            int p = 0;
            do {
                if (haystack[offset + p] == needle) {
                    return 1;
                }
                ++p;
            } while (--num != 0);
        }
        return 0;
    }

    public static double getRatio(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int lensum = len1 + len2;
        int editDistance = DiffUtils.levEditDistance(s1, s2, 1);
        return (double)(lensum - editDistance) / (double)lensum;
    }
}

