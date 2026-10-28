package me.darragh.lmc4j.codegen;

import me.darragh.lmc4j.ast.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Acts as a translator (used in transpilation), translating Little Man Computing assembly into a different language.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public interface SourceCodeGenerator extends CodeGenerator {
    String NEW_LINE = "\n"; // TODO: Adjust per operating system

    @NotNull String generate(@NotNull List<Instruction> instructions);
}
