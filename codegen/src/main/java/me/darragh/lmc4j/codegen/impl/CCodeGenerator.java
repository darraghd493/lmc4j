package me.darragh.lmc4j.codegen.impl;

import lombok.RequiredArgsConstructor;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.ast.InstructionType;
import me.darragh.lmc4j.codegen.CodeGenerator;
import me.darragh.lmc4j.codegen.CodeGeneratorConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @see me.darragh.lmc4j.codegen.CodeGenerator
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CCodeGenerator implements CodeGenerator { // TODO: Clean up the generated code to be more readable and efficient
    private final @NotNull CodeGeneratorConfiguration configuration;

    public CCodeGenerator() {
        this(CodeGeneratorConfiguration.DEFAULT);
    }

    @Override
    public @NotNull String generate(@NotNull List<Instruction> instructions) {
        StringBuilder builder = new StringBuilder();
        builder.append("#include <stdio.h>").append(NEW_LINE) // we will only be making standard input/output std calls so this is fine
                .append("#include <stdlib.h>").append(NEW_LINE)
                .append("#define put_memory_value(memory, address, value) memory[address] = value;").append(NEW_LINE)
                .append("short memory[").append(this.configuration.memorySize()).append("];").append(NEW_LINE)
                .append("short accumulator = 0;").append(NEW_LINE)
                .append("void main() {").append(NEW_LINE);

        Map<Integer, String> generatedLabels = new LinkedHashMap<>(); // line number->string

        // prepare data section first so it can be used in the code section
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction.type() == InstructionType.DATA) {
                builder.append("memory[").append(i).append("] = ").append(instruction.value()).append(";").append(NEW_LINE);
            }
        }

        // generate all code instructions
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction.type() == InstructionType.DATA) continue;

            builder.append(getLabel(i, generatedLabels)).append(":").append(NEW_LINE);
            switch (instruction.type()) {
                case ADD -> builder.append("accumulator += memory[").append(instruction.value()).append("];").append(NEW_LINE);
                case SUBTRACT -> builder.append("accumulator -= memory[").append(instruction.value()).append("];").append(NEW_LINE);
                case STORE -> builder.append("put_memory_value(memory, ").append(instruction.value()).append(", accumulator);").append(NEW_LINE);
                case LOAD -> builder.append("accumulator = memory[").append(instruction.value()).append("];").append(NEW_LINE);
                case BRANCH -> builder.append("goto ").append(getLabel(instruction.value(), generatedLabels)).append(";").append(NEW_LINE);
                case BRANCH_IF_ZERO -> {
                    builder.append("if (accumulator == 0) {").append(NEW_LINE);
                    builder.append("goto ").append(getLabel(instruction.value(), generatedLabels)).append(";").append(NEW_LINE);
                    builder.append("}").append(NEW_LINE);
                }
                case BRANCH_IF_POSITIVE -> {
                    builder.append("if (accumulator >= 0) {").append(NEW_LINE);
                    builder.append("goto ").append(getLabel(instruction.value(), generatedLabels)).append(";").append(NEW_LINE);
                    builder.append("}").append(NEW_LINE);
                }
                case INPUT -> builder.append("scanf(\"%d\", &accumulator);").append(NEW_LINE);
                case OUTPUT -> builder.append("printf(\"%d\\n\", accumulator);").append(NEW_LINE);
                case OUTPUT_AS_CHARACTER -> builder.append("printf(\"%c\", accumulator);").append(NEW_LINE);
                case HALT -> builder.append("exit(0);").append(NEW_LINE);
            }
        }

        return builder.append("}").toString();
    }

    private static @NotNull String getLabel(int lineNumber, @NotNull Map<Integer, String> generatedLabels) {
        if (generatedLabels.containsKey(lineNumber)) {
            return generatedLabels.get(lineNumber);
        }
        String label = "L_" + generatedLabels.size();
        generatedLabels.put(lineNumber, label);
        return label;
    }
}
