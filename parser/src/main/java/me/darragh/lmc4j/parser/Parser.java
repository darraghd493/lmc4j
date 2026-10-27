package me.darragh.lmc4j.parser;

import lombok.RequiredArgsConstructor;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.ast.InstructionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Parses LMC assembly code into an abstract syntax tree (AST).
 *
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class Parser {
    private final @NotNull ParserConfiguration configuration;

    public Parser() {
        this.configuration = ParserConfiguration.DEFAULT;
    }

    public @NotNull List<Instruction> parse(@NotNull String code) {
        List<String> lines = this.removeComments(this.splitLines(code));

        List<Instruction> instructions = new LinkedList<>();
        Map<String, Short> labels = new HashMap<>(); // label->line number
        Map<Short, IntermediaryInstruction> intermediaryInstructions = new HashMap<>(); // index->intermediary instruction

        // parse the lines into instructions and labels
        for (String line : lines) {
            line = line.replaceAll("//.*", "").trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            System.out.println("Parsing line: " + line + " -> tokens: " + Arrays.toString(tokens));

            switch (tokens.length) {
                case 1 -> {
                    String element = tokens[0];
                    Instruction instruction = new Instruction(InstructionType.fromString(element));
                    instructions.add(instruction);
                }
                case 2 -> {
                    // determine if the first element is a label or an instruction
                    String firstElement = tokens[0], secondElement = tokens[1];

                    InstructionType instructionType = null;
                    try {
                        instructionType = InstructionType.fromString(firstElement);
                    } catch (IllegalArgumentException ignored) {
                    }

                    if (instructionType == null) {
                        // fallback to second element
                        try {
                            instructionType = InstructionType.fromString(secondElement);
                        } catch (IllegalArgumentException ignored) {
                        }

                        if (instructionType == null) {
                            throw new IllegalArgumentException("Invalid instruction: " + line);
                        } else {
                            // first element is a label
                            if (labels.containsKey(firstElement)) {
                                throw new IllegalArgumentException("Duplicate label: " + firstElement);
                            }
                            labels.put(firstElement, (short) instructions.size());

                            IntermediaryInstruction intermediaryInstruction = new IntermediaryInstruction(instructionType, null);
                            intermediaryInstructions.put((short) instructions.size(), intermediaryInstruction);
                            instructions.add(new Instruction(instructionType));
                        }
                    } else {
                        // first element is an instruction
                        IntermediaryInstruction intermediaryInstruction = new IntermediaryInstruction(instructionType, secondElement);
                        intermediaryInstructions.put((short) instructions.size(), intermediaryInstruction);
                        instructions.add(new Instruction(instructionType));
                    }
                }
                case 3 -> {
                    // label, instruction, reference label
                    String label = tokens[0], instructionElement = tokens[1], value = tokens[2];
                    InstructionType instructionType = InstructionType.fromString(instructionElement);

                    if (labels.containsKey(label)) {
                        throw new IllegalArgumentException("Duplicate label: " + label);
                    }
                    labels.put(label, (short) instructions.size());

                    try {
                        int valueAsInt = Integer.parseInt(value);
                        instructions.add(new Instruction(instructionType, (short) valueAsInt));
                    } catch (Exception ignored) {
                        IntermediaryInstruction intermediaryInstruction = new IntermediaryInstruction(instructionType, value);
                        intermediaryInstructions.put((short) instructions.size(), intermediaryInstruction);
                        instructions.add(new Instruction(instructionType));
                    }
                }
                default -> throw new IllegalArgumentException("Invalid instruction: " + line);
            }
        }

        // resolve the labels in the intermediary instructions
        for (Map.Entry<Short, IntermediaryInstruction> entry : intermediaryInstructions.entrySet()) {
            short index = entry.getKey();
            IntermediaryInstruction intermediaryInstruction = entry.getValue();
            String referencedLabel = intermediaryInstruction.referencedLabel();
            if (referencedLabel != null) {
                Short referencedIndex = labels.get(referencedLabel);
                if (referencedIndex == null) {
                    throw new IllegalArgumentException("Undefined label: " + referencedLabel);
                }
                instructions.set(index, new Instruction(intermediaryInstruction.type(), referencedIndex));
            }
        }

        return instructions;
    }

    private @NotNull List<String> splitLines(@NotNull String code) {
        Queue<String> lines = new LinkedList<>(Arrays.asList(code.split("\n")));
        List<String> result = new LinkedList<>();
        while (!lines.isEmpty()) {
            String line = lines.poll().trim();
            if (line.isBlank()) continue;
            if (configuration.splitLine().enabled()) {
                String[] split = line.split(configuration.splitLine().lineDeliminator());
                for (String s : split) {
                    result.add(s.trim());
                }
            } else {
                result.add(line.trim());
            }
        }
        return result;
    }

    private @NotNull List<String> removeComments(@NotNull List<String> lines) {
        if (!configuration.comments().enabled()) return lines;
        List<String> result = new LinkedList<>();
        for (String line : lines) {
            int commentIndex = line.indexOf(configuration.comments().lineDeliminator());
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex).trim();
            }
            if (!line.isBlank()) {
                result.add(line);
            }
        }
        return result;
    }

    // these allow for us to easily resolve labels after parsing the instructions
    private record IntermediaryInstruction(@NotNull InstructionType type, @Nullable String referencedLabel) {
    }
}
