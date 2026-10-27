package me.darragh.lmc4j.ast;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the input/output code in the AST.
 *
 * @see InstructionType
 * @author darraghd493
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum IoCode {
    IN((byte) 1),
    OUT((byte) 2),
    OUT_AS_CHARACTER((byte) 22);

    private final byte code;
}
