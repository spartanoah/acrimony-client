/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CodeAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ByteForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ClassRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.DoubleForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.FieldRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.FloatRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.IMethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.IincForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.IntRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.LabelForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.LocalForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.LongForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.LookupSwitchForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.MethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.MultiANewArrayForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.NarrowClassRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.NewClassRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.NewInitMethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.NoArgumentForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ShortForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.StringRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.SuperFieldRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.SuperInitMethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.SuperMethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.TableSwitchForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ThisFieldRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ThisInitMethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ThisMethodRefForm;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.WideForm;

public abstract class ByteCodeForm {
    protected static final boolean WIDENED = true;
    protected static final ByteCodeForm[] byteCodeArray = new ByteCodeForm[256];
    protected static final Map byteCodesByName = new HashMap(256);
    private final int opcode;
    private final String name;
    private final int[] rewrite;
    private int firstOperandIndex;
    private int operandLength;

    public ByteCodeForm(int opcode, String name) {
        this(opcode, name, new int[]{opcode});
    }

    public ByteCodeForm(int opcode, String name, int[] rewrite) {
        this.opcode = opcode;
        this.name = name;
        this.rewrite = rewrite;
        this.calculateOperandPosition();
    }

    protected void calculateOperandPosition() {
        this.firstOperandIndex = -1;
        this.operandLength = -1;
        int iterationIndex = 0;
        while (iterationIndex < this.rewrite.length) {
            if (this.rewrite[iterationIndex] < 0) {
                this.firstOperandIndex = iterationIndex;
                iterationIndex = this.rewrite.length;
                continue;
            }
            ++iterationIndex;
        }
        if (this.firstOperandIndex == -1) {
            return;
        }
        int lastOperandIndex = -1;
        for (iterationIndex = this.firstOperandIndex; iterationIndex < this.rewrite.length; ++iterationIndex) {
            if (this.rewrite[iterationIndex] >= 0) continue;
            lastOperandIndex = iterationIndex;
        }
        int difference = lastOperandIndex - this.firstOperandIndex;
        if (difference < 0) {
            throw new Error("Logic error: not finding rewrite operands correctly");
        }
        this.operandLength = difference + 1;
    }

    public static ByteCodeForm get(int opcode) {
        return byteCodeArray[opcode];
    }

    public String toString() {
        return this.getClass().getName() + "(" + this.getName() + ")";
    }

    public int getOpcode() {
        return this.opcode;
    }

    public String getName() {
        return this.name;
    }

    public int[] getRewrite() {
        return this.rewrite;
    }

    public int[] getRewriteCopy() {
        int[] result = new int[this.rewrite.length];
        System.arraycopy(this.rewrite, 0, result, 0, this.rewrite.length);
        return result;
    }

    public int firstOperandIndex() {
        return this.firstOperandIndex;
    }

    public int operandLength() {
        return this.operandLength;
    }

    public boolean hasNoOperand() {
        return false;
    }

    public boolean hasMultipleByteCodes() {
        if (this.rewrite.length > 1 && this.rewrite[0] == 42) {
            return this.rewrite[1] > 0;
        }
        return false;
    }

    public abstract void setByteCodeOperands(ByteCode var1, OperandManager var2, int var3);

    public void fixUpByteCodeTargets(ByteCode byteCode, CodeAttribute codeAttribute) {
    }

    public boolean nestedMustStartClassPool() {
        return false;
    }

    static {
        ByteCodeForm.byteCodeArray[0] = new NoArgumentForm(0, "nop");
        ByteCodeForm.byteCodeArray[1] = new NoArgumentForm(1, "aconst_null");
        ByteCodeForm.byteCodeArray[2] = new NoArgumentForm(2, "iconst_m1");
        ByteCodeForm.byteCodeArray[3] = new NoArgumentForm(3, "iconst_0");
        ByteCodeForm.byteCodeArray[4] = new NoArgumentForm(4, "iconst_1");
        ByteCodeForm.byteCodeArray[5] = new NoArgumentForm(5, "iconst_2");
        ByteCodeForm.byteCodeArray[6] = new NoArgumentForm(6, "iconst_3");
        ByteCodeForm.byteCodeArray[7] = new NoArgumentForm(7, "iconst_4");
        ByteCodeForm.byteCodeArray[8] = new NoArgumentForm(8, "iconst_5");
        ByteCodeForm.byteCodeArray[9] = new NoArgumentForm(9, "lconst_0");
        ByteCodeForm.byteCodeArray[10] = new NoArgumentForm(10, "lconst_1");
        ByteCodeForm.byteCodeArray[11] = new NoArgumentForm(11, "fconst_0");
        ByteCodeForm.byteCodeArray[12] = new NoArgumentForm(12, "fconst_1");
        ByteCodeForm.byteCodeArray[13] = new NoArgumentForm(13, "fconst_2");
        ByteCodeForm.byteCodeArray[14] = new NoArgumentForm(14, "dconst_0");
        ByteCodeForm.byteCodeArray[15] = new NoArgumentForm(15, "dconst_1");
        ByteCodeForm.byteCodeArray[16] = new ByteForm(16, "bipush", new int[]{16, -1});
        ByteCodeForm.byteCodeArray[17] = new ShortForm(17, "sipush", new int[]{17, -1, -1});
        ByteCodeForm.byteCodeArray[18] = new StringRefForm(18, "ldc", new int[]{18, -1});
        ByteCodeForm.byteCodeArray[19] = new StringRefForm(19, "ldc_w", new int[]{19, -1, -1}, true);
        ByteCodeForm.byteCodeArray[20] = new LongForm(20, "ldc2_w", new int[]{20, -1, -1});
        ByteCodeForm.byteCodeArray[21] = new LocalForm(21, "iload", new int[]{21, -1});
        ByteCodeForm.byteCodeArray[22] = new LocalForm(22, "lload", new int[]{22, -1});
        ByteCodeForm.byteCodeArray[23] = new LocalForm(23, "fload", new int[]{23, -1});
        ByteCodeForm.byteCodeArray[24] = new LocalForm(24, "dload", new int[]{24, -1});
        ByteCodeForm.byteCodeArray[25] = new LocalForm(25, "aload", new int[]{25, -1});
        ByteCodeForm.byteCodeArray[26] = new NoArgumentForm(26, "iload_0");
        ByteCodeForm.byteCodeArray[27] = new NoArgumentForm(27, "iload_1");
        ByteCodeForm.byteCodeArray[28] = new NoArgumentForm(28, "iload_2");
        ByteCodeForm.byteCodeArray[29] = new NoArgumentForm(29, "iload_3");
        ByteCodeForm.byteCodeArray[30] = new NoArgumentForm(30, "lload_0");
        ByteCodeForm.byteCodeArray[31] = new NoArgumentForm(31, "lload_1");
        ByteCodeForm.byteCodeArray[32] = new NoArgumentForm(32, "lload_2");
        ByteCodeForm.byteCodeArray[33] = new NoArgumentForm(33, "lload_3");
        ByteCodeForm.byteCodeArray[34] = new NoArgumentForm(34, "fload_0");
        ByteCodeForm.byteCodeArray[35] = new NoArgumentForm(35, "fload_1");
        ByteCodeForm.byteCodeArray[36] = new NoArgumentForm(36, "fload_2");
        ByteCodeForm.byteCodeArray[37] = new NoArgumentForm(37, "fload_3");
        ByteCodeForm.byteCodeArray[38] = new NoArgumentForm(38, "dload_0");
        ByteCodeForm.byteCodeArray[39] = new NoArgumentForm(39, "dload_1");
        ByteCodeForm.byteCodeArray[40] = new NoArgumentForm(40, "dload_2");
        ByteCodeForm.byteCodeArray[41] = new NoArgumentForm(41, "dload_3");
        ByteCodeForm.byteCodeArray[42] = new NoArgumentForm(42, "aload_0");
        ByteCodeForm.byteCodeArray[43] = new NoArgumentForm(43, "aload_1");
        ByteCodeForm.byteCodeArray[44] = new NoArgumentForm(44, "aload_2");
        ByteCodeForm.byteCodeArray[45] = new NoArgumentForm(45, "aload_3");
        ByteCodeForm.byteCodeArray[46] = new NoArgumentForm(46, "iaload");
        ByteCodeForm.byteCodeArray[47] = new NoArgumentForm(47, "laload");
        ByteCodeForm.byteCodeArray[48] = new NoArgumentForm(48, "faload");
        ByteCodeForm.byteCodeArray[49] = new NoArgumentForm(49, "daload");
        ByteCodeForm.byteCodeArray[50] = new NoArgumentForm(50, "aaload");
        ByteCodeForm.byteCodeArray[51] = new NoArgumentForm(51, "baload");
        ByteCodeForm.byteCodeArray[52] = new NoArgumentForm(52, "caload");
        ByteCodeForm.byteCodeArray[53] = new NoArgumentForm(53, "saload");
        ByteCodeForm.byteCodeArray[54] = new LocalForm(54, "istore", new int[]{54, -1});
        ByteCodeForm.byteCodeArray[55] = new LocalForm(55, "lstore", new int[]{55, -1});
        ByteCodeForm.byteCodeArray[56] = new LocalForm(56, "fstore", new int[]{56, -1});
        ByteCodeForm.byteCodeArray[57] = new LocalForm(57, "dstore", new int[]{57, -1});
        ByteCodeForm.byteCodeArray[58] = new LocalForm(58, "astore", new int[]{58, -1});
        ByteCodeForm.byteCodeArray[59] = new NoArgumentForm(59, "istore_0");
        ByteCodeForm.byteCodeArray[60] = new NoArgumentForm(60, "istore_1");
        ByteCodeForm.byteCodeArray[61] = new NoArgumentForm(61, "istore_2");
        ByteCodeForm.byteCodeArray[62] = new NoArgumentForm(62, "istore_3");
        ByteCodeForm.byteCodeArray[63] = new NoArgumentForm(63, "lstore_0");
        ByteCodeForm.byteCodeArray[64] = new NoArgumentForm(64, "lstore_1");
        ByteCodeForm.byteCodeArray[65] = new NoArgumentForm(65, "lstore_2");
        ByteCodeForm.byteCodeArray[66] = new NoArgumentForm(66, "lstore_3");
        ByteCodeForm.byteCodeArray[67] = new NoArgumentForm(67, "fstore_0");
        ByteCodeForm.byteCodeArray[68] = new NoArgumentForm(68, "fstore_1");
        ByteCodeForm.byteCodeArray[69] = new NoArgumentForm(69, "fstore_2");
        ByteCodeForm.byteCodeArray[70] = new NoArgumentForm(70, "fstore_3");
        ByteCodeForm.byteCodeArray[71] = new NoArgumentForm(71, "dstore_0");
        ByteCodeForm.byteCodeArray[72] = new NoArgumentForm(72, "dstore_1");
        ByteCodeForm.byteCodeArray[73] = new NoArgumentForm(73, "dstore_2");
        ByteCodeForm.byteCodeArray[74] = new NoArgumentForm(74, "dstore_3");
        ByteCodeForm.byteCodeArray[75] = new NoArgumentForm(75, "astore_0");
        ByteCodeForm.byteCodeArray[76] = new NoArgumentForm(76, "astore_1");
        ByteCodeForm.byteCodeArray[77] = new NoArgumentForm(77, "astore_2");
        ByteCodeForm.byteCodeArray[78] = new NoArgumentForm(78, "astore_3");
        ByteCodeForm.byteCodeArray[79] = new NoArgumentForm(79, "iastore");
        ByteCodeForm.byteCodeArray[80] = new NoArgumentForm(80, "lastore");
        ByteCodeForm.byteCodeArray[81] = new NoArgumentForm(81, "fastore");
        ByteCodeForm.byteCodeArray[82] = new NoArgumentForm(82, "dastore");
        ByteCodeForm.byteCodeArray[83] = new NoArgumentForm(83, "aastore");
        ByteCodeForm.byteCodeArray[84] = new NoArgumentForm(84, "bastore");
        ByteCodeForm.byteCodeArray[85] = new NoArgumentForm(85, "castore");
        ByteCodeForm.byteCodeArray[86] = new NoArgumentForm(86, "sastore");
        ByteCodeForm.byteCodeArray[87] = new NoArgumentForm(87, "pop");
        ByteCodeForm.byteCodeArray[88] = new NoArgumentForm(88, "pop2");
        ByteCodeForm.byteCodeArray[89] = new NoArgumentForm(89, "dup");
        ByteCodeForm.byteCodeArray[90] = new NoArgumentForm(90, "dup_x1");
        ByteCodeForm.byteCodeArray[91] = new NoArgumentForm(91, "dup_x2");
        ByteCodeForm.byteCodeArray[92] = new NoArgumentForm(92, "dup2");
        ByteCodeForm.byteCodeArray[93] = new NoArgumentForm(93, "dup2_x1");
        ByteCodeForm.byteCodeArray[94] = new NoArgumentForm(94, "dup2_x2");
        ByteCodeForm.byteCodeArray[95] = new NoArgumentForm(95, "swap");
        ByteCodeForm.byteCodeArray[96] = new NoArgumentForm(96, "iadd");
        ByteCodeForm.byteCodeArray[97] = new NoArgumentForm(97, "ladd");
        ByteCodeForm.byteCodeArray[98] = new NoArgumentForm(98, "fadd");
        ByteCodeForm.byteCodeArray[99] = new NoArgumentForm(99, "dadd");
        ByteCodeForm.byteCodeArray[100] = new NoArgumentForm(100, "isub");
        ByteCodeForm.byteCodeArray[101] = new NoArgumentForm(101, "lsub");
        ByteCodeForm.byteCodeArray[102] = new NoArgumentForm(102, "fsub");
        ByteCodeForm.byteCodeArray[103] = new NoArgumentForm(103, "dsub");
        ByteCodeForm.byteCodeArray[104] = new NoArgumentForm(104, "imul");
        ByteCodeForm.byteCodeArray[105] = new NoArgumentForm(105, "lmul");
        ByteCodeForm.byteCodeArray[106] = new NoArgumentForm(106, "fmul");
        ByteCodeForm.byteCodeArray[107] = new NoArgumentForm(107, "dmul");
        ByteCodeForm.byteCodeArray[108] = new NoArgumentForm(108, "idiv");
        ByteCodeForm.byteCodeArray[109] = new NoArgumentForm(109, "ldiv");
        ByteCodeForm.byteCodeArray[110] = new NoArgumentForm(110, "fdiv");
        ByteCodeForm.byteCodeArray[111] = new NoArgumentForm(111, "ddiv");
        ByteCodeForm.byteCodeArray[112] = new NoArgumentForm(112, "irem");
        ByteCodeForm.byteCodeArray[113] = new NoArgumentForm(113, "lrem");
        ByteCodeForm.byteCodeArray[114] = new NoArgumentForm(114, "frem");
        ByteCodeForm.byteCodeArray[115] = new NoArgumentForm(115, "drem");
        ByteCodeForm.byteCodeArray[116] = new NoArgumentForm(116, "");
        ByteCodeForm.byteCodeArray[117] = new NoArgumentForm(117, "lneg");
        ByteCodeForm.byteCodeArray[118] = new NoArgumentForm(118, "fneg");
        ByteCodeForm.byteCodeArray[119] = new NoArgumentForm(119, "dneg");
        ByteCodeForm.byteCodeArray[120] = new NoArgumentForm(120, "ishl");
        ByteCodeForm.byteCodeArray[121] = new NoArgumentForm(121, "lshl");
        ByteCodeForm.byteCodeArray[122] = new NoArgumentForm(122, "ishr");
        ByteCodeForm.byteCodeArray[123] = new NoArgumentForm(123, "lshr");
        ByteCodeForm.byteCodeArray[124] = new NoArgumentForm(124, "iushr");
        ByteCodeForm.byteCodeArray[125] = new NoArgumentForm(125, "lushr");
        ByteCodeForm.byteCodeArray[126] = new NoArgumentForm(126, "iand");
        ByteCodeForm.byteCodeArray[127] = new NoArgumentForm(127, "land");
        ByteCodeForm.byteCodeArray[128] = new NoArgumentForm(128, "ior");
        ByteCodeForm.byteCodeArray[129] = new NoArgumentForm(129, "lor");
        ByteCodeForm.byteCodeArray[130] = new NoArgumentForm(130, "ixor");
        ByteCodeForm.byteCodeArray[131] = new NoArgumentForm(131, "lxor");
        ByteCodeForm.byteCodeArray[132] = new IincForm(132, "iinc", new int[]{132, -1, -1});
        ByteCodeForm.byteCodeArray[133] = new NoArgumentForm(133, "i2l");
        ByteCodeForm.byteCodeArray[134] = new NoArgumentForm(134, "i2f");
        ByteCodeForm.byteCodeArray[135] = new NoArgumentForm(135, "i2d");
        ByteCodeForm.byteCodeArray[136] = new NoArgumentForm(136, "l2i");
        ByteCodeForm.byteCodeArray[137] = new NoArgumentForm(137, "l2f");
        ByteCodeForm.byteCodeArray[138] = new NoArgumentForm(138, "l2d");
        ByteCodeForm.byteCodeArray[139] = new NoArgumentForm(139, "f2i");
        ByteCodeForm.byteCodeArray[140] = new NoArgumentForm(140, "f2l");
        ByteCodeForm.byteCodeArray[141] = new NoArgumentForm(141, "f2d");
        ByteCodeForm.byteCodeArray[142] = new NoArgumentForm(142, "d2i");
        ByteCodeForm.byteCodeArray[143] = new NoArgumentForm(143, "d2l");
        ByteCodeForm.byteCodeArray[144] = new NoArgumentForm(144, "d2f");
        ByteCodeForm.byteCodeArray[145] = new NoArgumentForm(145, "i2b");
        ByteCodeForm.byteCodeArray[146] = new NoArgumentForm(146, "i2c");
        ByteCodeForm.byteCodeArray[147] = new NoArgumentForm(147, "i2s");
        ByteCodeForm.byteCodeArray[148] = new NoArgumentForm(148, "lcmp");
        ByteCodeForm.byteCodeArray[149] = new NoArgumentForm(149, "fcmpl");
        ByteCodeForm.byteCodeArray[150] = new NoArgumentForm(150, "fcmpg");
        ByteCodeForm.byteCodeArray[151] = new NoArgumentForm(151, "dcmpl");
        ByteCodeForm.byteCodeArray[152] = new NoArgumentForm(152, "dcmpg");
        ByteCodeForm.byteCodeArray[153] = new LabelForm(153, "ifeq", new int[]{153, -1, -1});
        ByteCodeForm.byteCodeArray[154] = new LabelForm(154, "ifne", new int[]{154, -1, -1});
        ByteCodeForm.byteCodeArray[155] = new LabelForm(155, "iflt", new int[]{155, -1, -1});
        ByteCodeForm.byteCodeArray[156] = new LabelForm(156, "ifge", new int[]{156, -1, -1});
        ByteCodeForm.byteCodeArray[157] = new LabelForm(157, "ifgt", new int[]{157, -1, -1});
        ByteCodeForm.byteCodeArray[158] = new LabelForm(158, "ifle", new int[]{158, -1, -1});
        ByteCodeForm.byteCodeArray[159] = new LabelForm(159, "if_icmpeq", new int[]{159, -1, -1});
        ByteCodeForm.byteCodeArray[160] = new LabelForm(160, "if_icmpne", new int[]{160, -1, -1});
        ByteCodeForm.byteCodeArray[161] = new LabelForm(161, "if_icmplt", new int[]{161, -1, -1});
        ByteCodeForm.byteCodeArray[162] = new LabelForm(162, "if_icmpge", new int[]{162, -1, -1});
        ByteCodeForm.byteCodeArray[163] = new LabelForm(163, "if_icmpgt", new int[]{163, -1, -1});
        ByteCodeForm.byteCodeArray[164] = new LabelForm(164, "if_icmple", new int[]{164, -1, -1});
        ByteCodeForm.byteCodeArray[165] = new LabelForm(165, "if_acmpeq", new int[]{165, -1, -1});
        ByteCodeForm.byteCodeArray[166] = new LabelForm(166, "if_acmpne", new int[]{166, -1, -1});
        ByteCodeForm.byteCodeArray[167] = new LabelForm(167, "goto", new int[]{167, -1, -1});
        ByteCodeForm.byteCodeArray[168] = new LabelForm(168, "jsr", new int[]{168, -1, -1});
        ByteCodeForm.byteCodeArray[169] = new LocalForm(169, "ret", new int[]{169, -1});
        ByteCodeForm.byteCodeArray[170] = new TableSwitchForm(170, "tableswitch");
        ByteCodeForm.byteCodeArray[171] = new LookupSwitchForm(171, "lookupswitch");
        ByteCodeForm.byteCodeArray[172] = new NoArgumentForm(172, "ireturn");
        ByteCodeForm.byteCodeArray[173] = new NoArgumentForm(173, "lreturn");
        ByteCodeForm.byteCodeArray[174] = new NoArgumentForm(174, "freturn");
        ByteCodeForm.byteCodeArray[175] = new NoArgumentForm(175, "dreturn");
        ByteCodeForm.byteCodeArray[176] = new NoArgumentForm(176, "areturn");
        ByteCodeForm.byteCodeArray[177] = new NoArgumentForm(177, "return");
        ByteCodeForm.byteCodeArray[178] = new FieldRefForm(178, "getstatic", new int[]{178, -1, -1});
        ByteCodeForm.byteCodeArray[179] = new FieldRefForm(179, "putstatic", new int[]{179, -1, -1});
        ByteCodeForm.byteCodeArray[180] = new FieldRefForm(180, "getfield", new int[]{180, -1, -1});
        ByteCodeForm.byteCodeArray[181] = new FieldRefForm(181, "putfield", new int[]{181, -1, -1});
        ByteCodeForm.byteCodeArray[182] = new MethodRefForm(182, "invokevirtual", new int[]{182, -1, -1});
        ByteCodeForm.byteCodeArray[183] = new MethodRefForm(183, "invokespecial", new int[]{183, -1, -1});
        ByteCodeForm.byteCodeArray[184] = new MethodRefForm(184, "invokestatic", new int[]{184, -1, -1});
        ByteCodeForm.byteCodeArray[185] = new IMethodRefForm(185, "invokeinterface", new int[]{185, -1, -1, -1, 0});
        ByteCodeForm.byteCodeArray[186] = new NoArgumentForm(186, "xxxunusedxxx");
        ByteCodeForm.byteCodeArray[187] = new NewClassRefForm(187, "new", new int[]{187, -1, -1});
        ByteCodeForm.byteCodeArray[188] = new ByteForm(188, "newarray", new int[]{188, -1});
        ByteCodeForm.byteCodeArray[189] = new ClassRefForm(189, "anewarray", new int[]{189, -1, -1});
        ByteCodeForm.byteCodeArray[190] = new NoArgumentForm(190, "arraylength");
        ByteCodeForm.byteCodeArray[191] = new NoArgumentForm(191, "athrow");
        ByteCodeForm.byteCodeArray[192] = new ClassRefForm(192, "checkcast", new int[]{192, -1, -1});
        ByteCodeForm.byteCodeArray[193] = new ClassRefForm(193, "instanceof", new int[]{193, -1, -1});
        ByteCodeForm.byteCodeArray[194] = new NoArgumentForm(194, "monitorenter");
        ByteCodeForm.byteCodeArray[195] = new NoArgumentForm(195, "monitorexit");
        ByteCodeForm.byteCodeArray[196] = new WideForm(196, "wide");
        ByteCodeForm.byteCodeArray[197] = new MultiANewArrayForm(197, "multianewarray", new int[]{197, -1, -1, -1});
        ByteCodeForm.byteCodeArray[198] = new LabelForm(198, "ifnull", new int[]{198, -1, -1});
        ByteCodeForm.byteCodeArray[199] = new LabelForm(199, "ifnonnull", new int[]{199, -1, -1});
        ByteCodeForm.byteCodeArray[200] = new LabelForm(200, "goto_w", new int[]{200, -1, -1, -1, -1}, true);
        ByteCodeForm.byteCodeArray[201] = new LabelForm(201, "jsr_w", new int[]{201, -1, -1, -1, -1}, true);
        ByteCodeForm.byteCodeArray[202] = new ThisFieldRefForm(202, "getstatic_this", new int[]{178, -1, -1});
        ByteCodeForm.byteCodeArray[203] = new ThisFieldRefForm(203, "putstatic_this", new int[]{179, -1, -1});
        ByteCodeForm.byteCodeArray[204] = new ThisFieldRefForm(204, "getfield_this", new int[]{180, -1, -1});
        ByteCodeForm.byteCodeArray[205] = new ThisFieldRefForm(205, "putfield_this", new int[]{181, -1, -1});
        ByteCodeForm.byteCodeArray[206] = new ThisMethodRefForm(206, "invokevirtual_this", new int[]{182, -1, -1});
        ByteCodeForm.byteCodeArray[207] = new ThisMethodRefForm(207, "invokespecial_this", new int[]{183, -1, -1});
        ByteCodeForm.byteCodeArray[208] = new ThisMethodRefForm(208, "invokestatic_this", new int[]{184, -1, -1});
        ByteCodeForm.byteCodeArray[209] = new ThisFieldRefForm(209, "aload_0_getstatic_this", new int[]{42, 178, -1, -1});
        ByteCodeForm.byteCodeArray[210] = new ThisFieldRefForm(210, "aload_0_putstatic_this", new int[]{42, 179, -1, -1});
        ByteCodeForm.byteCodeArray[211] = new ThisFieldRefForm(211, "aload_0_getfield_this", new int[]{42, 180, -1, -1});
        ByteCodeForm.byteCodeArray[212] = new ThisFieldRefForm(212, "aload_0_putfield_this", new int[]{42, 181, -1, -1});
        ByteCodeForm.byteCodeArray[213] = new ThisMethodRefForm(213, "aload_0_invokevirtual_this", new int[]{42, 182, -1, -1});
        ByteCodeForm.byteCodeArray[214] = new ThisMethodRefForm(214, "aload_0_invokespecial_this", new int[]{42, 183, -1, -1});
        ByteCodeForm.byteCodeArray[215] = new ThisMethodRefForm(215, "aload_0_invokestatic_this", new int[]{42, 184, -1, -1});
        ByteCodeForm.byteCodeArray[216] = new SuperFieldRefForm(216, "getstatic_super", new int[]{178, -1, -1});
        ByteCodeForm.byteCodeArray[217] = new SuperFieldRefForm(217, "putstatic_super", new int[]{179, -1, -1});
        ByteCodeForm.byteCodeArray[218] = new SuperFieldRefForm(218, "getfield_super", new int[]{180, -1, -1});
        ByteCodeForm.byteCodeArray[219] = new SuperFieldRefForm(219, "putfield_super", new int[]{181, -1, -1});
        ByteCodeForm.byteCodeArray[220] = new SuperMethodRefForm(220, "invokevirtual_super", new int[]{182, -1, -1});
        ByteCodeForm.byteCodeArray[221] = new SuperMethodRefForm(221, "invokespecial_super", new int[]{183, -1, -1});
        ByteCodeForm.byteCodeArray[222] = new SuperMethodRefForm(222, "invokestatic_super", new int[]{184, -1, -1});
        ByteCodeForm.byteCodeArray[223] = new SuperFieldRefForm(223, "aload_0_getstatic_super", new int[]{42, 178, -1, -1});
        ByteCodeForm.byteCodeArray[224] = new SuperFieldRefForm(224, "aload_0_putstatic_super", new int[]{42, 179, -1, -1});
        ByteCodeForm.byteCodeArray[225] = new SuperFieldRefForm(225, "aload_0_getfield_super", new int[]{42, 180, -1, -1});
        ByteCodeForm.byteCodeArray[226] = new SuperFieldRefForm(226, "aload_0_putfield_super", new int[]{42, 181, -1, -1});
        ByteCodeForm.byteCodeArray[227] = new SuperMethodRefForm(227, "aload_0_invokevirtual_super", new int[]{42, 182, -1, -1});
        ByteCodeForm.byteCodeArray[228] = new SuperMethodRefForm(228, "aload_0_invokespecial_super", new int[]{42, 183, -1, -1});
        ByteCodeForm.byteCodeArray[229] = new SuperMethodRefForm(229, "aload_0_invokestatic_super", new int[]{42, 184, -1, -1});
        ByteCodeForm.byteCodeArray[230] = new ThisInitMethodRefForm(230, "invokespecial_this_init", new int[]{183, -1, -1});
        ByteCodeForm.byteCodeArray[231] = new SuperInitMethodRefForm(231, "invokespecial_super_init", new int[]{183, -1, -1});
        ByteCodeForm.byteCodeArray[232] = new NewInitMethodRefForm(232, "invokespecial_new_init", new int[]{183, -1, -1});
        ByteCodeForm.byteCodeArray[233] = new NarrowClassRefForm(233, "cldc", new int[]{18, -1});
        ByteCodeForm.byteCodeArray[234] = new IntRefForm(234, "ildc", new int[]{18, -1});
        ByteCodeForm.byteCodeArray[235] = new FloatRefForm(235, "fldc", new int[]{18, -1});
        ByteCodeForm.byteCodeArray[236] = new NarrowClassRefForm(236, "cldc_w", new int[]{19, -1, -1}, true);
        ByteCodeForm.byteCodeArray[237] = new IntRefForm(237, "ildc_w", new int[]{19, -1, -1}, true);
        ByteCodeForm.byteCodeArray[238] = new FloatRefForm(238, "fldc_w", new int[]{19, -1, -1}, true);
        ByteCodeForm.byteCodeArray[239] = new DoubleForm(239, "dldc2_w", new int[]{20, -1, -1});
        ByteCodeForm.byteCodeArray[254] = new NoArgumentForm(254, "impdep1");
        ByteCodeForm.byteCodeArray[255] = new NoArgumentForm(255, "impdep2");
        for (int i = 0; i < byteCodeArray.length; ++i) {
            ByteCodeForm byteCode = byteCodeArray[i];
            if (byteCode == null) continue;
            byteCodesByName.put(byteCode.getName(), byteCode);
        }
    }
}

