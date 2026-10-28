package me.darragh.lmc4j.codegen.impl;

import lombok.RequiredArgsConstructor;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.ast.InstructionType;
import me.darragh.lmc4j.codegen.BinaryCodeGenerator;
import me.darragh.lmc4j.codegen.CodeGeneratorConfiguration;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @see BinaryCodeGenerator
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class JvmClassCodeGenerator implements BinaryCodeGenerator<ClassNode>, Opcodes {
    private final @NotNull CodeGeneratorConfiguration configuration;
    private final @NotNull String className;

    public JvmClassCodeGenerator(@NotNull String className) {
        this(CodeGeneratorConfiguration.DEFAULT, className);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public @NotNull ClassNode generate(@NotNull List<Instruction> instructions) {
        ClassNode classNode = new ClassNode();
        classNode.name = this.className;
        classNode.version = Opcodes.V1_8;
        classNode.superName = "java/lang/Object";
        classNode.access = ACC_PUBLIC;

        // public static void main(String[] args) throws IOException
        MethodNode methodNode = new MethodNode(
                ACC_STATIC | ACC_PUBLIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                new String[] { "java/io/IOException" }
        );
        classNode.methods.add(methodNode);

        // memory (short[])
        methodNode.instructions.add(new LdcInsnNode(this.configuration.memorySize()));
        methodNode.instructions.add(new IntInsnNode(NEWARRAY, T_SHORT));
        methodNode.instructions.add(new VarInsnNode(ASTORE, 0));

        // accumulator (short/int)
        methodNode.instructions.add(new InsnNode(ICONST_0));
        methodNode.instructions.add(new VarInsnNode(ISTORE, 1));

        // load data into memory
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction.type() == InstructionType.DATA) {
                methodNode.instructions.add(new VarInsnNode(ALOAD, 0));
                methodNode.instructions.add(new LdcInsnNode(i));
                methodNode.instructions.add(new LdcInsnNode((int) instruction.value()));
                methodNode.instructions.add(new InsnNode(SASTORE));
            }
        }

        // pre-create branch targets
        Map<Integer, LabelNode> labels = new LinkedHashMap<>();
        for (int i = 0; i < instructions.size(); i++) {
            labels.put(i, new LabelNode());
        }

        // generate code instructions
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            methodNode.instructions.add(labels.get(i));

            if (instruction.type() == InstructionType.DATA) {
                continue;
            }

            switch (instruction.type()) {
                case ADD -> {
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new VarInsnNode(ALOAD, 0));
                    methodNode.instructions.add(new LdcInsnNode((int) instruction.value()));
                    methodNode.instructions.add(new InsnNode(SALOAD));
                    methodNode.instructions.add(new InsnNode(IADD));
                    emitWrapAndStoreAccumulator(methodNode);
                }
                case SUBTRACT -> {
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new VarInsnNode(ALOAD, 0));
                    methodNode.instructions.add(new LdcInsnNode((int) instruction.value()));
                    methodNode.instructions.add(new InsnNode(SALOAD));
                    methodNode.instructions.add(new InsnNode(ISUB));
                    emitWrapAndStoreAccumulator(methodNode);
                }
                case STORE -> {
                    methodNode.instructions.add(new VarInsnNode(ALOAD, 0));
                    methodNode.instructions.add(new LdcInsnNode((int) instruction.value()));
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new InsnNode(SASTORE));
                }
                case LOAD -> {
                    methodNode.instructions.add(new VarInsnNode(ALOAD, 0));
                    methodNode.instructions.add(new LdcInsnNode((int) instruction.value()));
                    methodNode.instructions.add(new InsnNode(SALOAD));
                    methodNode.instructions.add(new VarInsnNode(ISTORE, 1));
                }
                case BRANCH -> {
                    LabelNode target = labels.computeIfAbsent((int) instruction.value(), k -> new LabelNode());
                    methodNode.instructions.add(new JumpInsnNode(GOTO, target));
                }
                case BRANCH_IF_ZERO -> {
                    LabelNode target = labels.computeIfAbsent((int) instruction.value(), k -> new LabelNode());
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new JumpInsnNode(IFEQ, target));
                }
                case BRANCH_IF_POSITIVE -> {
                    LabelNode target = labels.computeIfAbsent((int) instruction.value(), k -> new LabelNode());
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new JumpInsnNode(IFGE, target));
                }
                case INPUT -> {
                    methodNode.instructions.add(new TypeInsnNode(NEW, "java/util/Scanner"));
                    methodNode.instructions.add(new InsnNode(DUP));
                    methodNode.instructions.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;"));
                    methodNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false));
                    methodNode.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false));
                    emitWrapAndStoreAccumulator(methodNode);
                }
                case OUTPUT -> {
                    methodNode.instructions.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false));
                }
                case OUTPUT_AS_CHARACTER -> {
                    methodNode.instructions.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                    methodNode.instructions.add(new VarInsnNode(ILOAD, 1));
                    methodNode.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V", false));
                }
                case HALT -> methodNode.instructions.add(new InsnNode(RETURN));
            }
        }

        methodNode.instructions.add(new InsnNode(RETURN));
        return classNode;
    }

    private static void emitWrapAndStoreAccumulator(@NotNull MethodNode methodNode) {
        methodNode.instructions.add(new LdcInsnNode(999));
        methodNode.instructions.add(new InsnNode(IADD));
        methodNode.instructions.add(new LdcInsnNode(1999));
        methodNode.instructions.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "floorMod", "(II)I", false));
        methodNode.instructions.add(new LdcInsnNode(999));
        methodNode.instructions.add(new InsnNode(ISUB));
        methodNode.instructions.add(new InsnNode(I2S));
        methodNode.instructions.add(new VarInsnNode(ISTORE, 1));
    }
}