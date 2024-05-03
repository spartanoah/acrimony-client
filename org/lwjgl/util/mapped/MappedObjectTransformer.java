/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.ClassAdapter
 *  org.objectweb.asm.ClassReader
 *  org.objectweb.asm.ClassVisitor
 *  org.objectweb.asm.ClassWriter
 *  org.objectweb.asm.FieldVisitor
 *  org.objectweb.asm.MethodVisitor
 *  org.objectweb.asm.Opcodes
 *  org.objectweb.asm.Type
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.AnnotationNode
 *  org.objectweb.asm.tree.FieldInsnNode
 *  org.objectweb.asm.tree.FieldNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.IntInsnNode
 *  org.objectweb.asm.tree.LdcInsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TypeInsnNode
 *  org.objectweb.asm.tree.VarInsnNode
 *  org.objectweb.asm.tree.analysis.Analyzer
 *  org.objectweb.asm.tree.analysis.AnalyzerException
 *  org.objectweb.asm.tree.analysis.BasicValue
 *  org.objectweb.asm.tree.analysis.Frame
 *  org.objectweb.asm.tree.analysis.Interpreter
 *  org.objectweb.asm.tree.analysis.SimpleVerifier
 *  org.objectweb.asm.util.TraceClassVisitor
 */
package org.lwjgl.util.mapped;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.MemoryUtil;
import org.lwjgl.util.mapped.CacheLinePad;
import org.lwjgl.util.mapped.CacheUtil;
import org.lwjgl.util.mapped.MappedField;
import org.lwjgl.util.mapped.MappedHelper;
import org.lwjgl.util.mapped.MappedObject;
import org.lwjgl.util.mapped.MappedObjectClassLoader;
import org.lwjgl.util.mapped.MappedObjectUnsafe;
import org.lwjgl.util.mapped.MappedSet;
import org.lwjgl.util.mapped.MappedSet2;
import org.lwjgl.util.mapped.MappedSet3;
import org.lwjgl.util.mapped.MappedSet4;
import org.lwjgl.util.mapped.MappedType;
import org.lwjgl.util.mapped.Pointer;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.TraceClassVisitor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MappedObjectTransformer {
    static final boolean PRINT_ACTIVITY = LWJGLUtil.DEBUG && LWJGLUtil.getPrivilegedBoolean("org.lwjgl.util.mapped.PrintActivity");
    static final boolean PRINT_TIMING = PRINT_ACTIVITY && LWJGLUtil.getPrivilegedBoolean("org.lwjgl.util.mapped.PrintTiming");
    static final boolean PRINT_BYTECODE = LWJGLUtil.DEBUG && LWJGLUtil.getPrivilegedBoolean("org.lwjgl.util.mapped.PrintBytecode");
    static final Map<String, MappedSubtypeInfo> className_to_subtype;
    static final String MAPPED_OBJECT_JVM;
    static final String MAPPED_HELPER_JVM;
    static final String MAPPEDSET_PREFIX;
    static final String MAPPED_SET2_JVM;
    static final String MAPPED_SET3_JVM;
    static final String MAPPED_SET4_JVM;
    static final String CACHE_LINE_PAD_JVM;
    static final String VIEWADDRESS_METHOD_NAME = "getViewAddress";
    static final String NEXT_METHOD_NAME = "next";
    static final String ALIGN_METHOD_NAME = "getAlign";
    static final String SIZEOF_METHOD_NAME = "getSizeof";
    static final String CAPACITY_METHOD_NAME = "capacity";
    static final String VIEW_CONSTRUCTOR_NAME = "constructView$LWJGL";
    static final Map<Integer, String> OPCODE_TO_NAME;
    static final Map<Integer, String> INSNTYPE_TO_NAME;
    static boolean is_currently_computing_frames;

    public static void register(Class<? extends MappedObject> type) {
        MappedSubtypeInfo mappedType;
        if (MappedObjectClassLoader.FORKED) {
            return;
        }
        MappedType mapped = type.getAnnotation(MappedType.class);
        if (mapped != null && mapped.padding() < 0) {
            throw new ClassFormatError("Invalid mapped type padding: " + mapped.padding());
        }
        if (type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
            throw new InternalError("only top-level or static inner classes are allowed");
        }
        String className = MappedObjectTransformer.jvmClassName(type);
        HashMap<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
        long sizeof = 0L;
        Field[] arr$ = type.getDeclaredFields();
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; ++i$) {
            Field field;
            FieldInfo fieldInfo = MappedObjectTransformer.registerField(mapped == null || mapped.autoGenerateOffsets(), className, sizeof, field = arr$[i$]);
            if (fieldInfo == null) continue;
            fields.put(field.getName(), fieldInfo);
            sizeof = Math.max(sizeof, fieldInfo.offset + fieldInfo.lengthPadded);
        }
        int align = 4;
        int padding = 0;
        boolean cacheLinePadded = false;
        if (mapped != null) {
            align = mapped.align();
            if (mapped.cacheLinePadding()) {
                if (mapped.padding() != 0) {
                    throw new ClassFormatError("Mapped type padding cannot be specified together with cacheLinePadding.");
                }
                int cacheLineMod = (int)(sizeof % (long)CacheUtil.getCacheLineSize());
                if (cacheLineMod != 0) {
                    padding = CacheUtil.getCacheLineSize() - cacheLineMod;
                }
                cacheLinePadded = true;
            } else {
                padding = mapped.padding();
            }
        }
        if (className_to_subtype.put(className, mappedType = new MappedSubtypeInfo(className, fields, (int)(sizeof += (long)padding), align, padding, cacheLinePadded)) != null) {
            throw new InternalError("duplicate mapped type: " + mappedType.className);
        }
    }

    private static FieldInfo registerField(boolean autoGenerateOffsets, String className, long advancingOffset, Field field) {
        long byteLength;
        if (Modifier.isStatic(field.getModifiers())) {
            return null;
        }
        if (!field.getType().isPrimitive() && field.getType() != ByteBuffer.class) {
            throw new ClassFormatError("field '" + className + "." + field.getName() + "' not supported: " + field.getType());
        }
        MappedField meta = field.getAnnotation(MappedField.class);
        if (meta == null && !autoGenerateOffsets) {
            throw new ClassFormatError("field '" + className + "." + field.getName() + "' missing annotation " + MappedField.class.getName() + ": " + className);
        }
        Pointer pointer = field.getAnnotation(Pointer.class);
        if (pointer != null && field.getType() != Long.TYPE) {
            throw new ClassFormatError("The @Pointer annotation can only be used on long fields. @Pointer field found: " + className + "." + field.getName() + ": " + field.getType());
        }
        if (Modifier.isVolatile(field.getModifiers()) && (pointer != null || field.getType() == ByteBuffer.class)) {
            throw new ClassFormatError("The volatile keyword is not supported for @Pointer or ByteBuffer fields. Volatile field found: " + className + "." + field.getName() + ": " + field.getType());
        }
        if (field.getType() == Long.TYPE || field.getType() == Double.TYPE) {
            byteLength = pointer == null ? 8L : (long)MappedObjectUnsafe.INSTANCE.addressSize();
        } else if (field.getType() == Double.TYPE) {
            byteLength = 8L;
        } else if (field.getType() == Integer.TYPE || field.getType() == Float.TYPE) {
            byteLength = 4L;
        } else if (field.getType() == Character.TYPE || field.getType() == Short.TYPE) {
            byteLength = 2L;
        } else if (field.getType() == Byte.TYPE) {
            byteLength = 1L;
        } else if (field.getType() == ByteBuffer.class) {
            byteLength = meta.byteLength();
            if (byteLength < 0L) {
                throw new IllegalStateException("invalid byte length for mapped ByteBuffer field: " + className + "." + field.getName() + " [length=" + byteLength + "]");
            }
        } else {
            throw new ClassFormatError(field.getType().getName());
        }
        if (field.getType() != ByteBuffer.class && advancingOffset % byteLength != 0L) {
            throw new IllegalStateException("misaligned mapped type: " + className + "." + field.getName());
        }
        CacheLinePad pad = field.getAnnotation(CacheLinePad.class);
        long byteOffset = advancingOffset;
        if (meta != null && meta.byteOffset() != -1L) {
            if (meta.byteOffset() < 0L) {
                throw new ClassFormatError("Invalid field byte offset: " + className + "." + field.getName() + " [byteOffset=" + meta.byteOffset() + "]");
            }
            if (pad != null) {
                throw new ClassFormatError("A field byte offset cannot be specified together with cache-line padding: " + className + "." + field.getName());
            }
            byteOffset = meta.byteOffset();
        }
        long byteLengthPadded = byteLength;
        if (pad != null) {
            if (pad.before() && byteOffset % (long)CacheUtil.getCacheLineSize() != 0L) {
                byteOffset += (long)CacheUtil.getCacheLineSize() - (byteOffset & (long)(CacheUtil.getCacheLineSize() - 1));
            }
            if (pad.after() && (byteOffset + byteLength) % (long)CacheUtil.getCacheLineSize() != 0L) {
                byteLengthPadded += (long)CacheUtil.getCacheLineSize() - (byteOffset + byteLength) % (long)CacheUtil.getCacheLineSize();
            }
            assert (!pad.before() || byteOffset % (long)CacheUtil.getCacheLineSize() == 0L);
            assert (!pad.after() || (byteOffset + byteLengthPadded) % (long)CacheUtil.getCacheLineSize() == 0L);
        }
        if (PRINT_ACTIVITY) {
            LWJGLUtil.log(MappedObjectTransformer.class.getSimpleName() + ": " + className + "." + field.getName() + " [type=" + field.getType().getSimpleName() + ", offset=" + byteOffset + "]");
        }
        return new FieldInfo(byteOffset, byteLength, byteLengthPadded, Type.getType(field.getType()), Modifier.isVolatile(field.getModifiers()), pointer != null);
    }

    static byte[] transformMappedObject(byte[] bytecode) {
        ClassWriter cw = new ClassWriter(0);
        ClassAdapter cv = new ClassAdapter((ClassVisitor)cw){
            private final String[] DEFINALIZE_LIST = new String[]{"getViewAddress", "next", "getAlign", "getSizeof", "capacity"};

            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                for (String method : this.DEFINALIZE_LIST) {
                    if (!name.equals(method)) continue;
                    access &= 0xFFFFFFEF;
                    break;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
        new ClassReader(bytecode).accept((ClassVisitor)cv, 0);
        return cw.toByteArray();
    }

    static byte[] transformMappedAPI(String className, byte[] bytecode) {
        TransformationAdapter ta;
        ClassWriter cw = new ClassWriter(2){

            protected String getCommonSuperClass(String a, String b) {
                if (is_currently_computing_frames && !a.startsWith("java/") || !b.startsWith("java/")) {
                    return "java/lang/Object";
                }
                return super.getCommonSuperClass(a, b);
            }
        };
        TransformationAdapter cv = ta = new TransformationAdapter((ClassVisitor)cw, className);
        if (className_to_subtype.containsKey(className)) {
            cv = MappedObjectTransformer.getMethodGenAdapter(className, (ClassVisitor)cv);
        }
        new ClassReader(bytecode).accept((ClassVisitor)cv, 4);
        if (!ta.transformed) {
            return bytecode;
        }
        bytecode = cw.toByteArray();
        if (PRINT_BYTECODE) {
            MappedObjectTransformer.printBytecode(bytecode);
        }
        return bytecode;
    }

    private static ClassAdapter getMethodGenAdapter(final String className, ClassVisitor cv) {
        return new ClassAdapter(cv){

            public void visitEnd() {
                MappedSubtypeInfo mappedSubtype = className_to_subtype.get(className);
                this.generateViewAddressGetter();
                this.generateCapacity();
                this.generateAlignGetter(mappedSubtype);
                this.generateSizeofGetter();
                this.generateNext();
                for (String fieldName : mappedSubtype.fields.keySet()) {
                    FieldInfo field = mappedSubtype.fields.get(fieldName);
                    if (field.type.getDescriptor().length() > 1) {
                        this.generateByteBufferGetter(fieldName, field);
                        continue;
                    }
                    this.generateFieldGetter(fieldName, field);
                    this.generateFieldSetter(fieldName, field);
                }
                super.visitEnd();
            }

            private void generateViewAddressGetter() {
                MethodVisitor mv = super.visitMethod(1, MappedObjectTransformer.VIEWADDRESS_METHOD_NAME, "(I)J", null, null);
                mv.visitCode();
                mv.visitVarInsn(25, 0);
                mv.visitFieldInsn(180, MAPPED_OBJECT_JVM, "baseAddress", "J");
                mv.visitVarInsn(21, 1);
                mv.visitFieldInsn(178, className, "SIZEOF", "I");
                mv.visitInsn(104);
                mv.visitInsn(133);
                mv.visitInsn(97);
                if (MappedObject.CHECKS) {
                    mv.visitInsn(92);
                    mv.visitVarInsn(25, 0);
                    mv.visitMethodInsn(184, MAPPED_HELPER_JVM, "checkAddress", "(JL" + MAPPED_OBJECT_JVM + ";)V");
                }
                mv.visitInsn(173);
                mv.visitMaxs(3, 2);
                mv.visitEnd();
            }

            private void generateCapacity() {
                MethodVisitor mv = super.visitMethod(1, MappedObjectTransformer.CAPACITY_METHOD_NAME, "()I", null, null);
                mv.visitCode();
                mv.visitVarInsn(25, 0);
                mv.visitMethodInsn(182, MAPPED_OBJECT_JVM, "backingByteBuffer", "()L" + MappedObjectTransformer.jvmClassName(ByteBuffer.class) + ";");
                mv.visitInsn(89);
                mv.visitMethodInsn(182, MappedObjectTransformer.jvmClassName(ByteBuffer.class), MappedObjectTransformer.CAPACITY_METHOD_NAME, "()I");
                mv.visitInsn(95);
                mv.visitMethodInsn(184, MappedObjectTransformer.jvmClassName(MemoryUtil.class), "getAddress0", "(L" + MappedObjectTransformer.jvmClassName(Buffer.class) + ";)J");
                mv.visitVarInsn(25, 0);
                mv.visitFieldInsn(180, MAPPED_OBJECT_JVM, "baseAddress", "J");
                mv.visitInsn(101);
                mv.visitInsn(136);
                mv.visitInsn(96);
                mv.visitFieldInsn(178, className, "SIZEOF", "I");
                mv.visitInsn(108);
                mv.visitInsn(172);
                mv.visitMaxs(3, 1);
                mv.visitEnd();
            }

            private void generateAlignGetter(MappedSubtypeInfo mappedSubtype) {
                MethodVisitor mv = super.visitMethod(1, MappedObjectTransformer.ALIGN_METHOD_NAME, "()I", null, null);
                mv.visitCode();
                MappedObjectTransformer.visitIntNode(mv, mappedSubtype.sizeof);
                mv.visitInsn(172);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }

            private void generateSizeofGetter() {
                MethodVisitor mv = super.visitMethod(1, MappedObjectTransformer.SIZEOF_METHOD_NAME, "()I", null, null);
                mv.visitCode();
                mv.visitFieldInsn(178, className, "SIZEOF", "I");
                mv.visitInsn(172);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }

            private void generateNext() {
                MethodVisitor mv = super.visitMethod(1, MappedObjectTransformer.NEXT_METHOD_NAME, "()V", null, null);
                mv.visitCode();
                mv.visitVarInsn(25, 0);
                mv.visitInsn(89);
                mv.visitFieldInsn(180, MAPPED_OBJECT_JVM, "viewAddress", "J");
                mv.visitFieldInsn(178, className, "SIZEOF", "I");
                mv.visitInsn(133);
                mv.visitInsn(97);
                mv.visitMethodInsn(182, className, "setViewAddress", "(J)V");
                mv.visitInsn(177);
                mv.visitMaxs(3, 1);
                mv.visitEnd();
            }

            private void generateByteBufferGetter(String fieldName, FieldInfo field) {
                MethodVisitor mv = super.visitMethod(9, MappedObjectTransformer.getterName(fieldName), "(L" + className + ";I)" + field.type.getDescriptor(), null, null);
                mv.visitCode();
                mv.visitVarInsn(25, 0);
                mv.visitVarInsn(21, 1);
                mv.visitMethodInsn(182, className, MappedObjectTransformer.VIEWADDRESS_METHOD_NAME, "(I)J");
                MappedObjectTransformer.visitIntNode(mv, (int)field.offset);
                mv.visitInsn(133);
                mv.visitInsn(97);
                MappedObjectTransformer.visitIntNode(mv, (int)field.length);
                mv.visitMethodInsn(184, MAPPED_HELPER_JVM, "newBuffer", "(JI)L" + MappedObjectTransformer.jvmClassName(ByteBuffer.class) + ";");
                mv.visitInsn(176);
                mv.visitMaxs(3, 2);
                mv.visitEnd();
            }

            private void generateFieldGetter(String fieldName, FieldInfo field) {
                MethodVisitor mv = super.visitMethod(9, MappedObjectTransformer.getterName(fieldName), "(L" + className + ";I)" + field.type.getDescriptor(), null, null);
                mv.visitCode();
                mv.visitVarInsn(25, 0);
                mv.visitVarInsn(21, 1);
                mv.visitMethodInsn(182, className, MappedObjectTransformer.VIEWADDRESS_METHOD_NAME, "(I)J");
                MappedObjectTransformer.visitIntNode(mv, (int)field.offset);
                mv.visitInsn(133);
                mv.visitInsn(97);
                mv.visitMethodInsn(184, MAPPED_HELPER_JVM, field.getAccessType() + "get", "(J)" + field.type.getDescriptor());
                mv.visitInsn(field.type.getOpcode(172));
                mv.visitMaxs(3, 2);
                mv.visitEnd();
            }

            private void generateFieldSetter(String fieldName, FieldInfo field) {
                MethodVisitor mv = super.visitMethod(9, MappedObjectTransformer.setterName(fieldName), "(L" + className + ";I" + field.type.getDescriptor() + ")V", null, null);
                mv.visitCode();
                int load = 0;
                switch (field.type.getSort()) {
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: {
                        load = 21;
                        break;
                    }
                    case 6: {
                        load = 23;
                        break;
                    }
                    case 7: {
                        load = 22;
                        break;
                    }
                    case 8: {
                        load = 24;
                    }
                }
                mv.visitVarInsn(load, 2);
                mv.visitVarInsn(25, 0);
                mv.visitVarInsn(21, 1);
                mv.visitMethodInsn(182, className, MappedObjectTransformer.VIEWADDRESS_METHOD_NAME, "(I)J");
                MappedObjectTransformer.visitIntNode(mv, (int)field.offset);
                mv.visitInsn(133);
                mv.visitInsn(97);
                mv.visitMethodInsn(184, MAPPED_HELPER_JVM, field.getAccessType() + "put", "(" + field.type.getDescriptor() + "J)V");
                mv.visitInsn(177);
                mv.visitMaxs(4, 4);
                mv.visitEnd();
            }
        };
    }

    static int transformMethodCall(InsnList instructions, int i, Map<AbstractInsnNode, Frame<BasicValue>> frameMap, MethodInsnNode methodInsn, MappedSubtypeInfo mappedType, Map<Integer, MappedSubtypeInfo> arrayVars) {
        switch (methodInsn.getOpcode()) {
            case 182: {
                if ("asArray".equals(methodInsn.name) && methodInsn.desc.equals("()[L" + MAPPED_OBJECT_JVM + ";")) {
                    AbstractInsnNode nextInstruction = methodInsn.getNext();
                    MappedObjectTransformer.checkInsnAfterIsArray(nextInstruction, 192);
                    nextInstruction = nextInstruction.getNext();
                    MappedObjectTransformer.checkInsnAfterIsArray(nextInstruction, 58);
                    Frame<BasicValue> frame = frameMap.get(nextInstruction);
                    String targetType = ((BasicValue)frame.getStack(frame.getStackSize() - 1)).getType().getElementType().getInternalName();
                    if (!methodInsn.owner.equals(targetType)) {
                        throw new ClassCastException("Source: " + methodInsn.owner + " - Target: " + targetType);
                    }
                    VarInsnNode varInstruction = (VarInsnNode)nextInstruction;
                    arrayVars.put(varInstruction.var, mappedType);
                    instructions.remove(methodInsn.getNext());
                    instructions.remove((AbstractInsnNode)methodInsn);
                }
                if ("dup".equals(methodInsn.name) && methodInsn.desc.equals("()L" + MAPPED_OBJECT_JVM + ";")) {
                    i = MappedObjectTransformer.replace(instructions, i, (AbstractInsnNode)methodInsn, MappedObjectTransformer.generateDupInstructions(methodInsn));
                    break;
                }
                if ("slice".equals(methodInsn.name) && methodInsn.desc.equals("()L" + MAPPED_OBJECT_JVM + ";")) {
                    i = MappedObjectTransformer.replace(instructions, i, (AbstractInsnNode)methodInsn, MappedObjectTransformer.generateSliceInstructions(methodInsn));
                    break;
                }
                if ("runViewConstructor".equals(methodInsn.name) && "()V".equals(methodInsn.desc)) {
                    i = MappedObjectTransformer.replace(instructions, i, (AbstractInsnNode)methodInsn, MappedObjectTransformer.generateRunViewConstructorInstructions(methodInsn));
                    break;
                }
                if ("copyTo".equals(methodInsn.name) && methodInsn.desc.equals("(L" + MAPPED_OBJECT_JVM + ";)V")) {
                    i = MappedObjectTransformer.replace(instructions, i, (AbstractInsnNode)methodInsn, MappedObjectTransformer.generateCopyToInstructions(mappedType));
                    break;
                }
                if (!"copyRange".equals(methodInsn.name) || !methodInsn.desc.equals("(L" + MAPPED_OBJECT_JVM + ";I)V")) break;
                i = MappedObjectTransformer.replace(instructions, i, (AbstractInsnNode)methodInsn, MappedObjectTransformer.generateCopyRangeInstructions(mappedType));
                break;
            }
            case 183: {
                if (!methodInsn.owner.equals(MAPPED_OBJECT_JVM) || !"<init>".equals(methodInsn.name) || !"()V".equals(methodInsn.desc)) break;
                instructions.remove(methodInsn.getPrevious());
                instructions.remove((AbstractInsnNode)methodInsn);
                i -= 2;
                break;
            }
            case 184: {
                boolean isMallocMethod;
                boolean isMapDirectMethod = "map".equals(methodInsn.name) && methodInsn.desc.equals("(JI)L" + MAPPED_OBJECT_JVM + ";");
                boolean isMapBufferMethod = "map".equals(methodInsn.name) && methodInsn.desc.equals("(Ljava/nio/ByteBuffer;)L" + MAPPED_OBJECT_JVM + ";");
                boolean bl = isMallocMethod = "malloc".equals(methodInsn.name) && methodInsn.desc.equals("(I)L" + MAPPED_OBJECT_JVM + ";");
                if (!isMapDirectMethod && !isMapBufferMethod && !isMallocMethod) break;
                i = MappedObjectTransformer.replace(instructions, i, (AbstractInsnNode)methodInsn, MappedObjectTransformer.generateMapInstructions(mappedType, methodInsn.owner, isMapDirectMethod, isMallocMethod));
            }
        }
        return i;
    }

    private static InsnList generateCopyRangeInstructions(MappedSubtypeInfo mappedType) {
        InsnList list = new InsnList();
        list.add(MappedObjectTransformer.getIntNode(mappedType.sizeof));
        list.add((AbstractInsnNode)new InsnNode(104));
        list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "copy", "(L" + MAPPED_OBJECT_JVM + ";L" + MAPPED_OBJECT_JVM + ";I)V"));
        return list;
    }

    private static InsnList generateCopyToInstructions(MappedSubtypeInfo mappedType) {
        InsnList list = new InsnList();
        list.add(MappedObjectTransformer.getIntNode(mappedType.sizeof - mappedType.padding));
        list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "copy", "(L" + MAPPED_OBJECT_JVM + ";L" + MAPPED_OBJECT_JVM + ";I)V"));
        return list;
    }

    private static InsnList generateRunViewConstructorInstructions(MethodInsnNode methodInsn) {
        InsnList list = new InsnList();
        list.add((AbstractInsnNode)new InsnNode(89));
        list.add((AbstractInsnNode)new MethodInsnNode(182, methodInsn.owner, VIEW_CONSTRUCTOR_NAME, "()V"));
        return list;
    }

    private static InsnList generateSliceInstructions(MethodInsnNode methodInsn) {
        InsnList list = new InsnList();
        list.add((AbstractInsnNode)new TypeInsnNode(187, methodInsn.owner));
        list.add((AbstractInsnNode)new InsnNode(89));
        list.add((AbstractInsnNode)new MethodInsnNode(183, methodInsn.owner, "<init>", "()V"));
        list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "slice", "(L" + MAPPED_OBJECT_JVM + ";L" + MAPPED_OBJECT_JVM + ";)L" + MAPPED_OBJECT_JVM + ";"));
        return list;
    }

    private static InsnList generateDupInstructions(MethodInsnNode methodInsn) {
        InsnList list = new InsnList();
        list.add((AbstractInsnNode)new TypeInsnNode(187, methodInsn.owner));
        list.add((AbstractInsnNode)new InsnNode(89));
        list.add((AbstractInsnNode)new MethodInsnNode(183, methodInsn.owner, "<init>", "()V"));
        list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "dup", "(L" + MAPPED_OBJECT_JVM + ";L" + MAPPED_OBJECT_JVM + ";)L" + MAPPED_OBJECT_JVM + ";"));
        return list;
    }

    private static InsnList generateMapInstructions(MappedSubtypeInfo mappedType, String className, boolean mapDirectMethod, boolean mallocMethod) {
        InsnList trg = new InsnList();
        if (mallocMethod) {
            trg.add(MappedObjectTransformer.getIntNode(mappedType.sizeof));
            trg.add((AbstractInsnNode)new InsnNode(104));
            trg.add((AbstractInsnNode)new MethodInsnNode(184, mappedType.cacheLinePadded ? MappedObjectTransformer.jvmClassName(CacheUtil.class) : MappedObjectTransformer.jvmClassName(BufferUtils.class), "createByteBuffer", "(I)L" + MappedObjectTransformer.jvmClassName(ByteBuffer.class) + ";"));
        } else if (mapDirectMethod) {
            trg.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "newBuffer", "(JI)L" + MappedObjectTransformer.jvmClassName(ByteBuffer.class) + ";"));
        }
        trg.add((AbstractInsnNode)new TypeInsnNode(187, className));
        trg.add((AbstractInsnNode)new InsnNode(89));
        trg.add((AbstractInsnNode)new MethodInsnNode(183, className, "<init>", "()V"));
        trg.add((AbstractInsnNode)new InsnNode(90));
        trg.add((AbstractInsnNode)new InsnNode(95));
        trg.add(MappedObjectTransformer.getIntNode(mappedType.align));
        trg.add(MappedObjectTransformer.getIntNode(mappedType.sizeof));
        trg.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "setup", "(L" + MAPPED_OBJECT_JVM + ";Ljava/nio/ByteBuffer;II)V"));
        return trg;
    }

    static InsnList transformFieldAccess(FieldInsnNode fieldInsn) {
        MappedSubtypeInfo mappedSubtype = className_to_subtype.get(fieldInsn.owner);
        if (mappedSubtype == null) {
            if ("view".equals(fieldInsn.name) && fieldInsn.owner.startsWith(MAPPEDSET_PREFIX)) {
                return MappedObjectTransformer.generateSetViewInstructions(fieldInsn);
            }
            return null;
        }
        if ("SIZEOF".equals(fieldInsn.name)) {
            return MappedObjectTransformer.generateSIZEOFInstructions(fieldInsn, mappedSubtype);
        }
        if ("view".equals(fieldInsn.name)) {
            return MappedObjectTransformer.generateViewInstructions(fieldInsn, mappedSubtype);
        }
        if ("baseAddress".equals(fieldInsn.name) || "viewAddress".equals(fieldInsn.name)) {
            return MappedObjectTransformer.generateAddressInstructions(fieldInsn);
        }
        FieldInfo field = mappedSubtype.fields.get(fieldInsn.name);
        if (field == null) {
            return null;
        }
        if (fieldInsn.desc.equals("L" + MappedObjectTransformer.jvmClassName(ByteBuffer.class) + ";")) {
            return MappedObjectTransformer.generateByteBufferInstructions(fieldInsn, mappedSubtype, field.offset);
        }
        return MappedObjectTransformer.generateFieldInstructions(fieldInsn, field);
    }

    private static InsnList generateSetViewInstructions(FieldInsnNode fieldInsn) {
        if (fieldInsn.getOpcode() == 180) {
            MappedObjectTransformer.throwAccessErrorOnReadOnlyField(fieldInsn.owner, fieldInsn.name);
        }
        if (fieldInsn.getOpcode() != 181) {
            throw new InternalError();
        }
        InsnList list = new InsnList();
        if (MAPPED_SET2_JVM.equals(fieldInsn.owner)) {
            list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "put_views", "(L" + MAPPED_SET2_JVM + ";I)V"));
        } else if (MAPPED_SET3_JVM.equals(fieldInsn.owner)) {
            list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "put_views", "(L" + MAPPED_SET3_JVM + ";I)V"));
        } else if (MAPPED_SET4_JVM.equals(fieldInsn.owner)) {
            list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "put_views", "(L" + MAPPED_SET4_JVM + ";I)V"));
        } else {
            throw new InternalError();
        }
        return list;
    }

    private static InsnList generateSIZEOFInstructions(FieldInsnNode fieldInsn, MappedSubtypeInfo mappedSubtype) {
        if (!"I".equals(fieldInsn.desc)) {
            throw new InternalError();
        }
        InsnList list = new InsnList();
        if (fieldInsn.getOpcode() == 178) {
            list.add(MappedObjectTransformer.getIntNode(mappedSubtype.sizeof));
            return list;
        }
        if (fieldInsn.getOpcode() == 179) {
            MappedObjectTransformer.throwAccessErrorOnReadOnlyField(fieldInsn.owner, fieldInsn.name);
        }
        throw new InternalError();
    }

    private static InsnList generateViewInstructions(FieldInsnNode fieldInsn, MappedSubtypeInfo mappedSubtype) {
        if (!"I".equals(fieldInsn.desc)) {
            throw new InternalError();
        }
        InsnList list = new InsnList();
        if (fieldInsn.getOpcode() == 180) {
            if (mappedSubtype.sizeof_shift != 0) {
                list.add(MappedObjectTransformer.getIntNode(mappedSubtype.sizeof_shift));
                list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "get_view_shift", "(L" + MAPPED_OBJECT_JVM + ";I)I"));
            } else {
                list.add(MappedObjectTransformer.getIntNode(mappedSubtype.sizeof));
                list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "get_view", "(L" + MAPPED_OBJECT_JVM + ";I)I"));
            }
            return list;
        }
        if (fieldInsn.getOpcode() == 181) {
            if (mappedSubtype.sizeof_shift != 0) {
                list.add(MappedObjectTransformer.getIntNode(mappedSubtype.sizeof_shift));
                list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "put_view_shift", "(L" + MAPPED_OBJECT_JVM + ";II)V"));
            } else {
                list.add(MappedObjectTransformer.getIntNode(mappedSubtype.sizeof));
                list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "put_view", "(L" + MAPPED_OBJECT_JVM + ";II)V"));
            }
            return list;
        }
        throw new InternalError();
    }

    private static InsnList generateAddressInstructions(FieldInsnNode fieldInsn) {
        if (!"J".equals(fieldInsn.desc)) {
            throw new IllegalStateException();
        }
        if (fieldInsn.getOpcode() == 180) {
            return null;
        }
        if (fieldInsn.getOpcode() == 181) {
            MappedObjectTransformer.throwAccessErrorOnReadOnlyField(fieldInsn.owner, fieldInsn.name);
        }
        throw new InternalError();
    }

    private static InsnList generateByteBufferInstructions(FieldInsnNode fieldInsn, MappedSubtypeInfo mappedSubtype, long fieldOffset) {
        if (fieldInsn.getOpcode() == 181) {
            MappedObjectTransformer.throwAccessErrorOnReadOnlyField(fieldInsn.owner, fieldInsn.name);
        }
        if (fieldInsn.getOpcode() == 180) {
            InsnList list = new InsnList();
            list.add((AbstractInsnNode)new FieldInsnNode(180, mappedSubtype.className, "viewAddress", "J"));
            list.add((AbstractInsnNode)new LdcInsnNode((Object)fieldOffset));
            list.add((AbstractInsnNode)new InsnNode(97));
            list.add((AbstractInsnNode)new LdcInsnNode((Object)mappedSubtype.fields.get((Object)fieldInsn.name).length));
            list.add((AbstractInsnNode)new InsnNode(136));
            list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, "newBuffer", "(JI)L" + MappedObjectTransformer.jvmClassName(ByteBuffer.class) + ";"));
            return list;
        }
        throw new InternalError();
    }

    private static InsnList generateFieldInstructions(FieldInsnNode fieldInsn, FieldInfo field) {
        InsnList list = new InsnList();
        if (fieldInsn.getOpcode() == 181) {
            list.add(MappedObjectTransformer.getIntNode((int)field.offset));
            list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, field.getAccessType() + "put", "(L" + MAPPED_OBJECT_JVM + ";" + fieldInsn.desc + "I)V"));
            return list;
        }
        if (fieldInsn.getOpcode() == 180) {
            list.add(MappedObjectTransformer.getIntNode((int)field.offset));
            list.add((AbstractInsnNode)new MethodInsnNode(184, MAPPED_HELPER_JVM, field.getAccessType() + "get", "(L" + MAPPED_OBJECT_JVM + ";I)" + fieldInsn.desc));
            return list;
        }
        throw new InternalError();
    }

    static int transformArrayAccess(InsnList instructions, int i, Map<AbstractInsnNode, Frame<BasicValue>> frameMap, VarInsnNode loadInsn, MappedSubtypeInfo mappedSubtype, int var2) {
        int loadStackSize = frameMap.get(loadInsn).getStackSize() + 1;
        VarInsnNode nextInsn = loadInsn;
        while (true) {
            if ((nextInsn = nextInsn.getNext()) == null) {
                throw new InternalError();
            }
            Frame<BasicValue> frame = frameMap.get(nextInsn);
            if (frame == null) continue;
            int stackSize = frame.getStackSize();
            if (stackSize == loadStackSize + 1 && nextInsn.getOpcode() == 50) {
                VarInsnNode aaLoadInsn = nextInsn;
                while ((nextInsn = nextInsn.getNext()) != null) {
                    FieldInsnNode fieldInsn;
                    frame = frameMap.get(nextInsn);
                    if (frame == null) continue;
                    stackSize = frame.getStackSize();
                    if (stackSize == loadStackSize + 1 && nextInsn.getOpcode() == 181) {
                        fieldInsn = (FieldInsnNode)nextInsn;
                        instructions.insert((AbstractInsnNode)nextInsn, (AbstractInsnNode)new MethodInsnNode(184, mappedSubtype.className, MappedObjectTransformer.setterName(fieldInsn.name), "(L" + mappedSubtype.className + ";I" + fieldInsn.desc + ")V"));
                        instructions.remove((AbstractInsnNode)nextInsn);
                        break;
                    }
                    if (stackSize == loadStackSize && nextInsn.getOpcode() == 180) {
                        fieldInsn = (FieldInsnNode)nextInsn;
                        instructions.insert((AbstractInsnNode)nextInsn, (AbstractInsnNode)new MethodInsnNode(184, mappedSubtype.className, MappedObjectTransformer.getterName(fieldInsn.name), "(L" + mappedSubtype.className + ";I)" + fieldInsn.desc));
                        instructions.remove((AbstractInsnNode)nextInsn);
                        break;
                    }
                    if (stackSize == loadStackSize && nextInsn.getOpcode() == 89 && nextInsn.getNext().getOpcode() == 180) {
                        fieldInsn = (FieldInsnNode)nextInsn.getNext();
                        MethodInsnNode getter = new MethodInsnNode(184, mappedSubtype.className, MappedObjectTransformer.getterName(fieldInsn.name), "(L" + mappedSubtype.className + ";I)" + fieldInsn.desc);
                        instructions.insert((AbstractInsnNode)nextInsn, (AbstractInsnNode)new InsnNode(92));
                        instructions.insert(nextInsn.getNext(), (AbstractInsnNode)getter);
                        instructions.remove((AbstractInsnNode)nextInsn);
                        instructions.remove((AbstractInsnNode)fieldInsn);
                        nextInsn = getter;
                        continue;
                    }
                    if (stackSize >= loadStackSize) continue;
                    throw new ClassFormatError("Invalid " + mappedSubtype.className + " view array usage detected: " + MappedObjectTransformer.getOpcodeName((AbstractInsnNode)nextInsn));
                }
                instructions.remove((AbstractInsnNode)aaLoadInsn);
                return i;
            }
            if (stackSize == loadStackSize && nextInsn.getOpcode() == 190) {
                if (LWJGLUtil.DEBUG && loadInsn.getNext() != nextInsn) {
                    throw new InternalError();
                }
                instructions.remove((AbstractInsnNode)nextInsn);
                loadInsn.var = var2;
                instructions.insert((AbstractInsnNode)loadInsn, (AbstractInsnNode)new MethodInsnNode(182, mappedSubtype.className, CAPACITY_METHOD_NAME, "()I"));
                return i + 1;
            }
            if (stackSize < loadStackSize) break;
        }
        throw new ClassFormatError("Invalid " + mappedSubtype.className + " view array usage detected: " + MappedObjectTransformer.getOpcodeName((AbstractInsnNode)nextInsn));
    }

    private static void getClassEnums(Class clazz, Map<Integer, String> map, String ... prefixFilters) {
        try {
            block2: for (Field field : clazz.getFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || field.getType() != Integer.TYPE) continue;
                for (String filter : prefixFilters) {
                    if (field.getName().startsWith(filter)) continue block2;
                }
                if (map.put((Integer)field.get(null), field.getName()) == null) continue;
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getOpcodeName(AbstractInsnNode insn) {
        String op = OPCODE_TO_NAME.get(insn.getOpcode());
        return INSNTYPE_TO_NAME.get(insn.getType()) + ": " + insn.getOpcode() + (op == null ? "" : " [" + OPCODE_TO_NAME.get(insn.getOpcode()) + "]");
    }

    static String jvmClassName(Class<?> type) {
        return type.getName().replace('.', '/');
    }

    static String getterName(String fieldName) {
        return "get$" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "$LWJGL";
    }

    static String setterName(String fieldName) {
        return "set$" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "$LWJGL";
    }

    private static void checkInsnAfterIsArray(AbstractInsnNode instruction, int opcode) {
        if (instruction == null) {
            throw new ClassFormatError("Unexpected end of instructions after .asArray() method.");
        }
        if (instruction.getOpcode() != opcode) {
            throw new ClassFormatError("The result of .asArray() must be stored to a local variable. Found: " + MappedObjectTransformer.getOpcodeName(instruction));
        }
    }

    static AbstractInsnNode getIntNode(int value) {
        if (value <= 5 && -1 <= value) {
            return new InsnNode(2 + value + 1);
        }
        if (value >= -128 && value <= 127) {
            return new IntInsnNode(16, value);
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(17, value);
        }
        return new LdcInsnNode((Object)value);
    }

    static void visitIntNode(MethodVisitor mv, int value) {
        if (value <= 5 && -1 <= value) {
            mv.visitInsn(2 + value + 1);
        } else if (value >= -128 && value <= 127) {
            mv.visitIntInsn(16, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(17, value);
        } else {
            mv.visitLdcInsn((Object)value);
        }
    }

    static int replace(InsnList instructions, int i, AbstractInsnNode location, InsnList list) {
        int size = list.size();
        instructions.insert(location, list);
        instructions.remove(location);
        return i + (size - 1);
    }

    private static void throwAccessErrorOnReadOnlyField(String className, String fieldName) {
        throw new IllegalAccessError("The " + className + "." + fieldName + " field is final.");
    }

    private static void printBytecode(byte[] bytecode) {
        StringWriter sw = new StringWriter();
        TraceClassVisitor tracer = new TraceClassVisitor((ClassVisitor)new ClassWriter(0), new PrintWriter(sw));
        new ClassReader(bytecode).accept((ClassVisitor)tracer, 0);
        String dump = sw.toString();
        LWJGLUtil.log(dump);
    }

    static {
        MAPPED_OBJECT_JVM = MappedObjectTransformer.jvmClassName(MappedObject.class);
        MAPPED_HELPER_JVM = MappedObjectTransformer.jvmClassName(MappedHelper.class);
        MAPPEDSET_PREFIX = MappedObjectTransformer.jvmClassName(MappedSet.class);
        MAPPED_SET2_JVM = MappedObjectTransformer.jvmClassName(MappedSet2.class);
        MAPPED_SET3_JVM = MappedObjectTransformer.jvmClassName(MappedSet3.class);
        MAPPED_SET4_JVM = MappedObjectTransformer.jvmClassName(MappedSet4.class);
        CACHE_LINE_PAD_JVM = "L" + MappedObjectTransformer.jvmClassName(CacheLinePad.class) + ";";
        OPCODE_TO_NAME = new HashMap<Integer, String>();
        INSNTYPE_TO_NAME = new HashMap<Integer, String>();
        MappedObjectTransformer.getClassEnums(Opcodes.class, OPCODE_TO_NAME, "V1_", "ACC_", "T_", "F_", "MH_");
        MappedObjectTransformer.getClassEnums(AbstractInsnNode.class, INSNTYPE_TO_NAME, new String[0]);
        className_to_subtype = new HashMap<String, MappedSubtypeInfo>();
        className_to_subtype.put(MAPPED_OBJECT_JVM, new MappedSubtypeInfo(MAPPED_OBJECT_JVM, null, -1, -1, -1, false));
        String vmName = System.getProperty("java.vm.name");
        if (vmName != null && !vmName.contains("Server")) {
            System.err.println("Warning: " + MappedObject.class.getSimpleName() + "s have inferiour performance on Client VMs, please consider switching to a Server VM.");
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class MappedSubtypeInfo {
        final String className;
        final int sizeof;
        final int sizeof_shift;
        final int align;
        final int padding;
        final boolean cacheLinePadded;
        final Map<String, FieldInfo> fields;

        MappedSubtypeInfo(String className, Map<String, FieldInfo> fields, int sizeof, int align, int padding, boolean cacheLinePadded) {
            this.className = className;
            this.sizeof = sizeof;
            this.sizeof_shift = (sizeof - 1 & sizeof) == 0 ? MappedSubtypeInfo.getPoT(sizeof) : 0;
            this.align = align;
            this.padding = padding;
            this.cacheLinePadded = cacheLinePadded;
            this.fields = fields;
        }

        private static int getPoT(int value) {
            int pot = -1;
            while (value > 0) {
                ++pot;
                value >>= 1;
            }
            return pot;
        }
    }

    private static class FieldInfo {
        final long offset;
        final long length;
        final long lengthPadded;
        final Type type;
        final boolean isVolatile;
        final boolean isPointer;

        FieldInfo(long offset, long length, long lengthPadded, Type type, boolean isVolatile, boolean isPointer) {
            this.offset = offset;
            this.length = length;
            this.lengthPadded = lengthPadded;
            this.type = type;
            this.isVolatile = isVolatile;
            this.isPointer = isPointer;
        }

        String getAccessType() {
            return this.isPointer ? "a" : this.type.getDescriptor().toLowerCase() + (this.isVolatile ? "v" : "");
        }
    }

    private static class TransformationAdapter
    extends ClassAdapter {
        final String className;
        boolean transformed;

        TransformationAdapter(ClassVisitor cv, String className) {
            super(cv);
            this.className = className;
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            MappedSubtypeInfo mappedSubtype = className_to_subtype.get(this.className);
            if (mappedSubtype != null && mappedSubtype.fields.containsKey(name)) {
                if (PRINT_ACTIVITY) {
                    LWJGLUtil.log(MappedObjectTransformer.class.getSimpleName() + ": discarding field: " + this.className + "." + name + ":" + desc);
                }
                return null;
            }
            if ((access & 8) == 0) {
                return new FieldNode(access, name, desc, signature, value){

                    public void visitEnd() {
                        if (this.visibleAnnotations == null) {
                            this.accept(TransformationAdapter.this.cv);
                            return;
                        }
                        boolean before = false;
                        boolean after = false;
                        int byteLength = 0;
                        for (AnnotationNode pad : this.visibleAnnotations) {
                            if (!CACHE_LINE_PAD_JVM.equals(pad.desc)) continue;
                            if ("J".equals(this.desc) || "D".equals(this.desc)) {
                                byteLength = 8;
                            } else if ("I".equals(this.desc) || "F".equals(this.desc)) {
                                byteLength = 4;
                            } else if ("S".equals(this.desc) || "C".equals(this.desc)) {
                                byteLength = 2;
                            } else if ("B".equals(this.desc) || "Z".equals(this.desc)) {
                                byteLength = 1;
                            } else {
                                throw new ClassFormatError("The @CacheLinePad annotation cannot be used on non-primitive fields: " + TransformationAdapter.this.className + "." + this.name);
                            }
                            TransformationAdapter.this.transformed = true;
                            after = true;
                            if (pad.values == null) break;
                            for (int i = 0; i < pad.values.size(); i += 2) {
                                boolean value = pad.values.get(i + 1).equals(Boolean.TRUE);
                                if ("before".equals(pad.values.get(i))) {
                                    before = value;
                                    continue;
                                }
                                after = value;
                            }
                        }
                        if (before) {
                            int count;
                            for (int i = count = CacheUtil.getCacheLineSize() / byteLength - 1; i >= 1; --i) {
                                TransformationAdapter.this.cv.visitField(this.access | 1 | 0x1000, this.name + "$PAD_" + i, this.desc, this.signature, null);
                            }
                        }
                        this.accept(TransformationAdapter.this.cv);
                        if (after) {
                            int count = CacheUtil.getCacheLineSize() / byteLength - 1;
                            for (int i = 1; i <= count; ++i) {
                                TransformationAdapter.this.cv.visitField(this.access | 1 | 0x1000, this.name + "$PAD" + i, this.desc, this.signature, null);
                            }
                        }
                    }
                };
            }
            return super.visitField(access, name, desc, signature, value);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MappedSubtypeInfo mappedSubtype;
            if ("<init>".equals(name) && (mappedSubtype = className_to_subtype.get(this.className)) != null) {
                if (!"()V".equals(desc)) {
                    throw new ClassFormatError(this.className + " can only have a default constructor, found: " + desc);
                }
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                mv.visitVarInsn(25, 0);
                mv.visitMethodInsn(183, MAPPED_OBJECT_JVM, "<init>", "()V");
                mv.visitInsn(177);
                mv.visitMaxs(0, 0);
                name = MappedObjectTransformer.VIEW_CONSTRUCTOR_NAME;
            }
            final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodNode(access, name, desc, signature, exceptions){
                boolean needsTransformation;

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                public void visitMaxs(int a, int b) {
                    try {
                        is_currently_computing_frames = true;
                        super.visitMaxs(a, b);
                    } finally {
                        is_currently_computing_frames = false;
                    }
                }

                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (className_to_subtype.containsKey(owner) || owner.startsWith(MAPPEDSET_PREFIX)) {
                        this.needsTransformation = true;
                    }
                    super.visitFieldInsn(opcode, owner, name, desc);
                }

                public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                    if (className_to_subtype.containsKey(owner)) {
                        this.needsTransformation = true;
                    }
                    super.visitMethodInsn(opcode, owner, name, desc);
                }

                public void visitEnd() {
                    if (this.needsTransformation) {
                        TransformationAdapter.this.transformed = true;
                        try {
                            this.transformMethod(this.analyse());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    this.accept(mv);
                }

                private Frame<BasicValue>[] analyse() throws AnalyzerException {
                    Analyzer a = new Analyzer((Interpreter)new SimpleVerifier());
                    a.analyze(TransformationAdapter.this.className, (MethodNode)this);
                    return a.getFrames();
                }

                private void transformMethod(Frame<BasicValue>[] frames) {
                    int i;
                    InsnList instructions = this.instructions;
                    HashMap<Integer, MappedSubtypeInfo> arrayVars = new HashMap<Integer, MappedSubtypeInfo>();
                    HashMap<AbstractInsnNode, Frame<BasicValue>> frameMap = new HashMap<AbstractInsnNode, Frame<BasicValue>>();
                    for (i = 0; i < frames.length; ++i) {
                        frameMap.put(instructions.get(i), frames[i]);
                    }
                    block6: for (i = 0; i < instructions.size(); ++i) {
                        AbstractInsnNode instruction = instructions.get(i);
                        switch (instruction.getType()) {
                            case 2: {
                                if (instruction.getOpcode() != 25) continue block6;
                                VarInsnNode varInsn = (VarInsnNode)instruction;
                                MappedSubtypeInfo mappedSubtype = (MappedSubtypeInfo)arrayVars.get(varInsn.var);
                                if (mappedSubtype == null) continue block6;
                                i = MappedObjectTransformer.transformArrayAccess(instructions, i, frameMap, varInsn, mappedSubtype, varInsn.var);
                                continue block6;
                            }
                            case 4: {
                                FieldInsnNode fieldInsn = (FieldInsnNode)instruction;
                                InsnList list = MappedObjectTransformer.transformFieldAccess(fieldInsn);
                                if (list == null) continue block6;
                                i = MappedObjectTransformer.replace(instructions, i, instruction, list);
                                continue block6;
                            }
                            case 5: {
                                MethodInsnNode methodInsn = (MethodInsnNode)instruction;
                                MappedSubtypeInfo mappedType = className_to_subtype.get(methodInsn.owner);
                                if (mappedType == null) continue block6;
                                i = MappedObjectTransformer.transformMethodCall(instructions, i, frameMap, methodInsn, mappedType, arrayVars);
                            }
                        }
                    }
                }
            };
        }
    }
}

