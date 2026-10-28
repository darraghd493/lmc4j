package me.darragh.lmc4j.interpreter;

import lombok.Getter;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.ast.InstructionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Handles the interpretation of the AST and executes the instructions.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public final class Interpreter {
    private final @NotNull InterpreterConfiguration configuration;

    @Getter
    private final @NotNull InterpreterMemory memory;

    public Interpreter(@NotNull InterpreterConfiguration configuration) {
        this.configuration = configuration;
        this.memory = new InterpreterMemory(this.configuration.memorySize());
    }

    public Interpreter() {
        this(InterpreterConfiguration.DEFAULT);
    }

    public void interpret(@NotNull List<Instruction> instructions) {
        this.packInstructionsIntoMemory(instructions);

        short accumulator = 0; // accumulator
        int cir = 0; // current instruction registry

        boolean reachedHalt = false;
        while (!reachedHalt && cir < this.configuration.memorySize()) {
            Instruction instruction = Instruction.unpack(memory.get(cir++));
            switch (instruction.type()) {
                case HALT -> reachedHalt = true;
                case ADD -> accumulator = wrapAccumulator(accumulator + memory.get(instruction.value()));
                case SUBTRACT -> accumulator = wrapAccumulator(accumulator - memory.get(instruction.value()));
                case STORE -> memory.set(instruction.value(), accumulator);
                case LOAD -> accumulator = memory.get(instruction.value());
                case BRANCH -> cir = instruction.value();
                case BRANCH_IF_ZERO -> {
                    if (accumulator == 0) {
                        cir = instruction.value();
                    }
                }
                case BRANCH_IF_POSITIVE -> {
                    if (accumulator >= 0) {
                        cir = instruction.value();
                    }
                }
                case INPUT -> accumulator = this.configuration.inputProvider().getInput();
                case OUTPUT -> this.configuration.outputProvider().outputValue(accumulator);
                case OUTPUT_AS_CHARACTER -> this.configuration.outputProvider().outputAscii((char) accumulator);
            }
        }
    }

    private void packInstructionsIntoMemory(@NotNull List<Instruction> instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            this.memory.set(i, instruction.type() == InstructionType.DATA ? instruction.value() : instruction.pack());
        }
    }

    private static short wrapAccumulator(int value) {
        return (short) (Math.floorMod(value + 999, 1999) - 999);
    }
}
