package me.darragh.lmc4j.ast;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an instruction in the AST.
 *
 * @author darraghd493
 * @since 0.0.1
 */
public record Instruction(@NotNull InstructionType type, short value) {
    public Instruction(@NotNull InstructionType type) {
        this(type, (short) 0);
    }

    public short pack() {
        return (short) (this.type.getCode() * 100 + this.value); // can become corrupted
    }

    public static @NotNull Instruction unpack(short packed) {
        short type = (short) Math.floorDiv(packed, 100),
                value = (short) Math.floorMod(packed, 100);
        return new Instruction(InstructionType.values()[type], value);
    }
}
