import me.darragh.lmc4j.parser.Parser;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.ast.InstructionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private Parser parser;

    @BeforeEach
    void setUp() {
        parser = new Parser();
    }

    @Test
    @DisplayName("Parses single-token instructions without operands")
    void testSingleTokenInstructions() {
        String code = """
                INP
                OUT
                HLT
                """;

        List<Instruction> instructions = parser.parse(code);

        assertEquals(3, instructions.size());
        assertEquals(InstructionType.fromString("INP"), instructions.get(0).type());
        assertEquals(InstructionType.fromString("OUT"), instructions.get(1).type());
        assertEquals(InstructionType.fromString("HLT"), instructions.get(2).type());
    }

    @Test
    @DisplayName("Resolves 2-token instructions with label references")
    void testInstructionWithReferencedLabel() {
        String code = """
                LDA VALUE
                VALUE DAT
                """;

        List<Instruction> instructions = parser.parse(code);

        assertEquals(2, instructions.size());
        assertEquals(InstructionType.fromString("LDA"), instructions.get(0).type());
        assertEquals(1, instructions.get(0).value()); // resolves to index of VALUE
        assertEquals(InstructionType.fromString("DAT"), instructions.get(1).type());
    }

    @Test
    @DisplayName("Parses 2-token line where the first element is a label")
    void testLabelWithInstructionNoOperand() {
        String code = """
                START INP
                BRA START
                """;

        List<Instruction> instructions = parser.parse(code);

        assertEquals(2, instructions.size());
        assertEquals(InstructionType.fromString("INP"), instructions.get(0).type());
        assertEquals(InstructionType.fromString("BRA"), instructions.get(1).type());
        assertEquals(0, instructions.get(1).value()); // resolves to index 0 (START)
    }

    @Test
    @DisplayName("Parses 3-token line with label, instruction, and operand")
    void testThreeTokenInstruction() {
        String code = """
                LOOP ADD ONE
                ONE DAT
                """;

        List<Instruction> instructions = parser.parse(code);

        assertEquals(2, instructions.size());
        assertEquals(InstructionType.fromString("ADD"), instructions.get(0).type());
        assertEquals(1, instructions.get(0).value()); // points to index of ONE
        assertEquals(InstructionType.fromString("DAT"), instructions.get(1).type());
    }

    @Test
    @DisplayName("Ignores empty lines, trailing whitespace, and comments")
    void testCommentsAndBlankLines() {
        String code = """
                INP // Read first number
                
                OUT // Output number
                HLT
                """;

        List<Instruction> instructions = parser.parse(code);

        assertEquals(3, instructions.size());
        assertEquals(InstructionType.fromString("INP"), instructions.get(0).type());
        assertEquals(InstructionType.fromString("OUT"), instructions.get(1).type());
        assertEquals(InstructionType.fromString("HLT"), instructions.get(2).type());
    }

    @Test
    @DisplayName("Throws exception when encountering an undefined label")
    void testUndefinedLabelThrowsException() {
        String code = "LDA MISSING_LABEL";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(code));
        assertTrue(exception.getMessage().contains("Undefined label: MISSING_LABEL"));
    }

    @Test
    @DisplayName("Throws exception on duplicate label definitions")
    void testDuplicateLabelThrowsException() {
        String code = """
                LABEL INP
                LABEL OUT
                """;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(code));
        assertTrue(exception.getMessage().contains("Duplicate label: LABEL"));
    }

    @Test
    @DisplayName("Throws exception on malformed lines with too many tokens")
    void testTooManyTokensThrowsException() {
        String code = "LABEL INP EXTRA TOKENS";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(code));
        assertTrue(exception.getMessage().contains("Invalid instruction"));
    }

    @Test
    @DisplayName("Throws exception on 2-token line where neither token is a valid instruction")
    void testTwoInvalidTokensThrowsException() {
        String code = "FOO BAR";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(code));
        assertTrue(exception.getMessage().contains("Invalid instruction: FOO BAR"));
    }
}