import lombok.SneakyThrows;
import me.darragh.lmc4j.ast.Instruction;
import me.darragh.lmc4j.codegen.impl.JvmClassCodeGenerator;
import me.darragh.lmc4j.parser.Parser;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

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

    JvmClassCodeGenerator generator = new JvmClassCodeGenerator("Main");
    ClassNode classNode = generator.generate(instructions);

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    classNode.accept(classWriter);
    byte[] bytecode = classWriter.toByteArray();
    Path path = Path.of("Main.class");
    Files.write(path, bytecode);
    System.out.println("Bytecode written to " + path.toAbsolutePath());
}