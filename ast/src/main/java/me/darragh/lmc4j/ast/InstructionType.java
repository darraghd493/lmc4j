package me.darragh.lmc4j.ast;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the type of instruction in the AST.
 *
 * @author darraghd493
 * @since 0.0.1
 */
@Getter
public enum InstructionType {
    HALT((byte) 0, false, "hlt"),
    ADD((byte) 1, true, "add"),
    SUBTRACT((byte) 2, true, "sub"),
    STORE((byte) 3, true, "sto", "sta"),
    LOAD((byte) 4, true, "lda"),
    BRANCH((byte) 5, true, "bra"),
    BRANCH_IF_ZERO((byte) 6, true, "brz"),
    BRANCH_IF_POSITIVE((byte) 7, true, "brp"),
    INPUT((byte) 8, false, "inp"),
    OUTPUT((byte) 9, false, "out"),
    OUTPUT_AS_CHARACTER((byte) 9, false, "otc"),
    DATA((byte) 9, true, "dat");

    private final byte code;
    private final @NotNull String[] mnemonics;
    private final boolean requiresValue;

    InstructionType(byte code, boolean requiresValue, @NotNull String... mnemonics) {
        this.code = code;
        this.mnemonics = mnemonics;
        this.requiresValue = requiresValue;
    }

    public static @NotNull InstructionType fromString(@NotNull String s) {
        for (InstructionType type : InstructionType.values()) {
            for (String mnemonic : type.mnemonics) {
                if (mnemonic.equalsIgnoreCase(s)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Unknown instruction type: " + s);
    }
}
