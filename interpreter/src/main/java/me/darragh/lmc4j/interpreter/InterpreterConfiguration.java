package me.darragh.lmc4j.interpreter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @see Interpreter
 * @author darraghd493
 * @since 1.0.0
 */
public record InterpreterConfiguration(
        int memorySize,
        @NotNull InputProvider inputProvider,
        @NotNull OutputProvider outputProvider
) {
    public static final InterpreterConfiguration DEFAULT = new InterpreterConfiguration(
            100,
            InputProvider.DEFAULT,
            OutputProvider.DEFAULT
    );

    @FunctionalInterface
    public interface InputProvider {
        InputProvider DEFAULT = () -> {
            try { // TODO: Use input stream scanner5
                return (short) Integer.parseInt(String.valueOf((char) System.in.read())); // holy slop
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        short getInput();
    }

    public interface OutputProvider {
        OutputProvider DEFAULT = new OutputProvider() {
            @Override
            public void outputValue(short value) {
                System.out.println(value);
            }

            @Override
            public void outputAscii(char c) {
                System.out.print(c);
            }
        };

        void outputValue(short value);

        void outputAscii(char c);
    }
}
