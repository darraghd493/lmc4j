import lombok.SneakyThrows;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.codegen.impl.CCodeGenerator;
import me.darragh.lmc4j.parser.Parser;

@SneakyThrows
void main() { // TODO: Add tests
    Parser parser = new Parser();
    List<Instruction> instructions = parser.parse("""
            INP
            STA VALUE
            LDA VALUE
            OUT
            HLT
            VALUE DAT 0""");

    System.out.println(instructions);

    CCodeGenerator generator = new CCodeGenerator();
    String ccode = generator.generate(instructions);

    Path path = Paths.get("./code.c");
    Files.deleteIfExists(path);
    Files.writeString(path, ccode);

    System.out.println("Please run: gcc -O3 -o code code.c && code");
}