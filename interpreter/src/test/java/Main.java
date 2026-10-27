import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.interpreter.Interpreter;
import me.darragh.lmc4j.parser.Parser;

void main() { // TODO: Add tests
    Parser parser = new Parser();
    List<Instruction> instructions = parser.parse("""
            INP
            STA CUR
            
            LOOP LDA CUR
            SUB ONE
            OUT
            STA CUR
            
            BRP LOOP
            HLT
            
            ONE DAT 1
            TEN DAT 10
            CUR DAT 0""");

    Interpreter interpreter = new Interpreter();
    interpreter.interpret(instructions);
}