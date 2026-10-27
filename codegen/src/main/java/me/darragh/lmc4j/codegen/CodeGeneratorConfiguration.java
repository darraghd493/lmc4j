package me.darragh.lmc4j.codegen;

/**
 * @author darraghd493
 * @since 1.0.0
 */
public record CodeGeneratorConfiguration(int memorySize) {
    public static final CodeGeneratorConfiguration DEFAULT = new CodeGeneratorConfiguration(100);
}
