package me.darragh.lmc4j.codegen;

import me.darragh.lmc4j.ast.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Generates a binary from a list of instructions.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public interface BinaryCodeGenerator<T> extends CodeGenerator {
    @NotNull T generate(@NotNull List<Instruction> instructions);
}
