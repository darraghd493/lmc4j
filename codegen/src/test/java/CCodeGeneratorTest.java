import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.codegen.CodeGeneratorConfiguration;
import me.darragh.lmc4j.codegen.SourceCodeGenerator;
import me.darragh.lmc4j.codegen.impl.CCodeGenerator;
import me.darragh.lmc4j.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CCodeGeneratorTest {
    private Parser parser;
    private SourceCodeGenerator generator;

    @BeforeEach
    void setUp() {
        parser = new Parser();
        generator = new CCodeGenerator();
    }

    @Test
    @DisplayName("Generates C boilerplate including headers, memory, accumulator, and main function")
    void testBoilerplateGeneration() {
        String code = "HLT";
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.startsWith("#include <stdio.h>\n#include <stdlib.h>\n"));
        assertTrue(cCode.contains("#define put_memory_value(memory, address, value) memory[address] = value;"));
        assertTrue(cCode.contains("short memory[100];"));
        assertTrue(cCode.contains("short accumulator = 0;"));
        assertTrue(cCode.contains("void main() {"));
        assertTrue(cCode.endsWith("}"));
    }

    @Test
    @DisplayName("Applies custom memory size configuration")
    void testCustomMemorySize() {
        SourceCodeGenerator customGenerator = new CCodeGenerator(new CodeGeneratorConfiguration(256));
        String code = "HLT";
        List<Instruction> instructions = parser.parse(code);

        String cCode = customGenerator.generate(instructions);

        assertTrue(cCode.contains("short memory[256];"));
    }

    @Test
    @DisplayName("Initializes data section in memory before code execution")
    void testDataInitialization() {
        String code = """
                LDA VALUE
                HLT
                VALUE DAT 42
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("memory[2] = 42;"));
        int dataInitIndex = cCode.indexOf("memory[2] = 42;");
        int loadIndex = cCode.indexOf("accumulator = memory[2];");
        assertTrue(dataInitIndex < loadIndex, "Data initialization should occur before instruction execution");
    }

    @Test
    @DisplayName("Generates C code for arithmetic instructions (ADD and SUB)")
    void testArithmeticInstructions() {
        String code = """
                ADD FIRST
                SUB SECOND
                HLT
                FIRST DAT 10
                SECOND DAT 5
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("accumulator += memory[3];"));
        assertTrue(cCode.contains("accumulator -= memory[4];"));
    }

    @Test
    @DisplayName("Generates C code for memory operations (LDA and STA)")
    void testMemoryInstructions() {
        String code = """
                LDA NUM
                STA NUM
                HLT
                NUM DAT 7
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("accumulator = memory[3];"));
        assertTrue(cCode.contains("put_memory_value(memory, 3, accumulator);"));
    }

    @Test
    @DisplayName("Generates C code for IO and HALT instructions")
    void testIoAndHaltInstructions() {
        String code = """
                INP
                OUT
                OTC
                HLT
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("scanf(\"%d\", &accumulator);"));
        assertTrue(cCode.contains("printf(\"%d\\n\", accumulator);"));
        assertTrue(cCode.contains("printf(\"%c\", accumulator);"));
        assertTrue(cCode.contains("exit(0);"));
    }

    @Test
    @DisplayName("Generates goto for unconditional branch instruction (BRA)")
    void testBranchInstruction() {
        String code = """
                START INP
                BRA START
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("L_0:\nscanf(\"%d\", &accumulator);"));
        assertTrue(cCode.contains("goto L_0;"));
    }

    @Test
    @DisplayName("Generates conditional branches for BRZ and BRP")
    void testConditionalBranchInstructions() {
        String code = """
                LOOP INP
                BRZ ZERO
                BRP POS
                ZERO HLT
                POS HLT
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("if (accumulator == 0) {\ngoto L_"));
        assertTrue(cCode.contains("if (accumulator >= 0) {\ngoto L_"));
    }

    @Test
    @DisplayName("Generates complete C program for an LMC countdown loop")
    void testCompleteCountdownProgram() {
        String code = """
                INP
                STA CUR
                LOOP LDA CUR
                SUB ONE
                OUT
                STA CUR
                BRP LOOP
                HLT
                ONE DAT 1
                CUR DAT 0
                """;
        List<Instruction> instructions = parser.parse(code);

        String cCode = generator.generate(instructions);

        assertTrue(cCode.contains("memory[8] = 1;"));
        assertTrue(cCode.contains("memory[9] = 0;"));
        assertTrue(cCode.contains("scanf(\"%d\", &accumulator);"));
        assertTrue(cCode.contains("put_memory_value(memory, 9, accumulator);"));
        assertTrue(cCode.contains("accumulator = memory[9];"));
        assertTrue(cCode.contains("accumulator -= memory[8];"));
        assertTrue(cCode.contains("printf(\"%d\\n\", accumulator);"));
        assertTrue(cCode.contains("if (accumulator >= 0) {"));
        assertTrue(cCode.contains("exit(0);"));
    }
}
