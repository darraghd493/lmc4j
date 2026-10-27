package me.darragh.lmc4j.parser;

import org.jetbrains.annotations.NotNull;

/**
 * @see Parser
 * @author darraghd493
 * @since 1.0.0
 */
public record ParserConfiguration(
        @NotNull Comments comments,
        @NotNull ParserConfiguration.SplitLine splitLine
) {
    public static final ParserConfiguration DEFAULT = new ParserConfiguration(Comments.DEFAULT, SplitLine.DEFAULT);

    public record Comments(
            boolean enabled,
            @NotNull String lineDeliminator
    ) {
        public static final Comments DEFAULT = new Comments(true, "//");
    }

    public record SplitLine(
            boolean enabled,
            @NotNull String lineDeliminator
    ) {
        public static final SplitLine DEFAULT = new SplitLine(true, ";");
    }
}
