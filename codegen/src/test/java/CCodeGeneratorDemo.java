import lombok.SneakyThrows;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.codegen.impl.CCodeGenerator;
import me.darragh.lmc4j.parser.Parser;

@SneakyThrows
void main() {
    Parser parser = new Parser();
    List<Instruction> instructions = parser.parse("""
                    INP
            loop    OUT
                    STA count
                    SUB one
                    STA count
                    BRP loop
                    HLT
            one     DAT 1
            count   DAT\s
            """);

    CCodeGenerator generator = new CCodeGenerator();
    String code = generator.generate(instructions);

    Path path = Path.of("code.c");
    Files.writeString(path, code);
    System.out.println("Generated code written to " + path.toAbsolutePath());
    System.out.println("Compile + run it using: gcc -O3 code.c -o code && code");
}