package me.darragh.lmc4j.interpreter;

/**
 * Represents the memory of the Little Man Computer (LMC) interpreter.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public final class InterpreterMemory {

    private final short[] memory; // [-999, 999]

    public InterpreterMemory(int memorySize) {
        this.memory = new short[memorySize];
    }

    public short get(int address) {
        return this.memory[address];
    }

    public void set(int address, short value) {
        this.memory[address] = (short) (Math.floorMod(value + 999, 1999) - 999); // allows for overflow
    }
}
