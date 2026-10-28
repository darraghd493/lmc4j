import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.interpreter.Interpreter;
import me.darragh.lmc4j.interpreter.InterpreterConfiguration;
import me.darragh.lmc4j.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    private Parser parser;

    @BeforeEach
    void setUp() {
        parser = new Parser();
    }

    private static class RecordingOutputProvider implements InterpreterConfiguration.OutputProvider {
        final List<Short> values = new ArrayList<>();
        final StringBuilder chars = new StringBuilder();

        @Override
        public void outputValue(short value) {
            values.add(value);
        }

        @Override
        public void outputAscii(char c) {
            chars.append(c);
        }
    }

    private Interpreter createInterpreter(List<Short> inputs, RecordingOutputProvider outputProvider) {
        Queue<Short> inputQueue = new ArrayDeque<>(inputs);
        InterpreterConfiguration configuration = new InterpreterConfiguration(
                100,
                () -> {
                    if (inputQueue.isEmpty()) {
                        throw new IllegalStateException("No more input available in test queue");
                    }
                    return inputQueue.poll();
                },
                outputProvider
        );
        return new Interpreter(configuration);
    }

    @Test
    @DisplayName("Halts execution immediately when encountering HLT")
    void testHaltInstruction() {
        String code = """
                HLT
                OUT
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of(), output);

        interpreter.interpret(instructions);

        assertTrue(output.values.isEmpty(), "OUT after HLT should not be executed");
    }

    @Test
    @DisplayName("Reads numeric input via INP and outputs via OUT")
    void testInputAndOutput() {
        String code = """
                INP
                OUT
                HLT
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of((short) 42), output);

        interpreter.interpret(instructions);

        assertEquals(List.of((short) 42), output.values);
    }

    @Test
    @DisplayName("Stores accumulator value in memory and loads it back")
    void testStoreAndLoad() {
        String code = """
                INP
                STA VALUE
                LDA VALUE
                OUT
                HLT
                VALUE DAT 0
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of((short) 99), output);

        interpreter.interpret(instructions);

        assertEquals(List.of((short) 99), output.values);
        assertEquals(99, interpreter.getMemory().get(5));
    }

    @Test
    @DisplayName("Adds memory value to accumulator")
    void testAddInstruction() {
        String code = """
                LDA FIRST
                ADD SECOND
                OUT
                HLT
                FIRST DAT 15
                SECOND DAT 27
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of(), output);

        interpreter.interpret(instructions);

        assertEquals(List.of((short) 42), output.values);
    }

    @Test
    @DisplayName("Subtracts memory value from accumulator")
    void testSubtractInstruction() {
        String code = """
                LDA FIRST
                SUB SECOND
                OUT
                HLT
                FIRST DAT 50
                SECOND DAT 18
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of(), output);

        interpreter.interpret(instructions);

        assertEquals(List.of((short) 32), output.values);
    }

    @Test
    @DisplayName("Branches unconditionally with BRA instruction")
    void testUnconditionalBranch() {
        String code = """
                BRA TARGET
                LDA WRONG
                OUT
                TARGET LDA RIGHT
                OUT
                HLT
                WRONG DAT 11
                RIGHT DAT 99
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of(), output);

        interpreter.interpret(instructions);

        assertEquals(List.of((short) 99), output.values);
    }

    @Test
    @DisplayName("Branches with BRZ when accumulator is zero, falls through when non-zero")
    void testBranchIfZero() {
        String code = """
                INP
                BRZ ZERO_BRANCH
                LDA VAL_ONE
                OUT
                HLT
                ZERO_BRANCH LDA VAL_TWO
                OUT
                HLT
                VAL_ONE DAT 1
                VAL_TWO DAT 2
                """;
        List<Instruction> instructions = parser.parse(code);

        RecordingOutputProvider outputZero = new RecordingOutputProvider();
        Interpreter interpreterZero = createInterpreter(List.of((short) 0), outputZero);
        interpreterZero.interpret(instructions);
        assertEquals(List.of((short) 2), outputZero.values);

        RecordingOutputProvider outputNonZero = new RecordingOutputProvider();
        Interpreter interpreterNonZero = createInterpreter(List.of((short) 5), outputNonZero);
        interpreterNonZero.interpret(instructions);
        assertEquals(List.of((short) 1), outputNonZero.values);
    }

    @Test
    @DisplayName("Branches with BRP when accumulator is positive or zero, falls through when negative")
    void testBranchIfPositive() {
        String code = """
                LDA FIRST
                SUB SECOND
                BRP POSITIVE_BRANCH
                LDA NEG_FLAG
                OUT
                HLT
                POSITIVE_BRANCH LDA POS_FLAG
                OUT
                HLT
                FIRST DAT 5
                SECOND DAT 10
                POS_FLAG DAT 1
                NEG_FLAG DAT 2
                """;
        List<Instruction> instructions = parser.parse(code);

        RecordingOutputProvider outputNegative = new RecordingOutputProvider();
        Interpreter interpreterNeg = createInterpreter(List.of(), outputNegative);
        interpreterNeg.interpret(instructions);
        assertEquals(List.of((short) 2), outputNegative.values);

        String codePos = """
                LDA FIRST
                SUB SECOND
                BRP POSITIVE_BRANCH
                LDA NEG_FLAG
                OUT
                HLT
                POSITIVE_BRANCH LDA POS_FLAG
                OUT
                HLT
                FIRST DAT 10
                SECOND DAT 5
                POS_FLAG DAT 1
                NEG_FLAG DAT 2
                """;

        List<Instruction> instructionsPos = parser.parse(codePos);
        RecordingOutputProvider outputPos = new RecordingOutputProvider();
        Interpreter interpreterPos = createInterpreter(List.of(), outputPos);
        interpreterPos.interpret(instructionsPos);
        assertEquals(List.of((short) 1), outputPos.values);
    }

    @Test
    @DisplayName("Executes countdown loop program correctly")
    void testCountdownLoop() {
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
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of((short) 3), output);

        interpreter.interpret(instructions);

        assertEquals(List.of((short) 2, (short) 1, (short) 0, (short) -1), output.values);
    }

    @Test
    @DisplayName("Correctly initializes memory with DAT instruction values")
    void testMemoryInitializationFromDataInstructions() {
        String code = """
                HLT
                A DAT 123
                B DAT -45
                """;
        List<Instruction> instructions = parser.parse(code);
        RecordingOutputProvider output = new RecordingOutputProvider();
        Interpreter interpreter = createInterpreter(List.of(), output);

        interpreter.interpret(instructions);

        assertEquals(123, interpreter.getMemory().get(1));
        assertEquals(-45, interpreter.getMemory().get(2));
    }
}
