package com.cleanroommc.modularui.test.editor;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {

    public static class Error {
        public static final String EXPECTED = "Expected '%s', but got '%s'";
    }

    public static final ScriptReader READER = new ScriptReader();
    public final Map<String, String> variables = new HashMap<>();
    public final List<Line> lines = new ArrayList<>();

    @Nullable
    public Line getLine(int index) {
        if (index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index);
    }

    public Token getToken(int lineIndex, int charIndex) {
        Line line = getLine(lineIndex);
        return line == null ? null : line.getToken(charIndex);
    }

    public void scan(List<String> lines) {
        scan(READER, lines);
    }

    public void scan(ScriptReader reader, List<String> lines) {
        reader.read(lines);
        this.lines.clear();
        this.variables.clear();
        this.lines.addAll(reader.lines);
        this.variables.putAll(reader.variables);
    }

    public static class Line {
        public final String rawText;
        public final List<Token> tokens;

        public Line(String rawText, List<Token> tokens) {
            this.rawText = rawText;
            this.tokens = tokens;
        }

        @Nullable
        public Token getToken(int charIndex) {
            Token lastToken = null;
            for (Token token : tokens) {
                if (charIndex == token.startIndex) {
                    return token;
                }
                if (charIndex > token.startIndex) {
                    lastToken = token;
                } else {
                    return lastToken;
                }
            }
            return lastToken;
        }
    }

    public static class Token {
        public final Type type;
        public final String text;
        public final int startIndex;
        @Nullable
        public final String errorMsg;

        public Token(Type type, String text, int startIndex, @Nullable String errorMsg) {
            this.type = type;
            this.text = text;
            this.startIndex = startIndex;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "Type " + type.name() + "-[" + text + "]";
        }
    }

    public enum Type {
        UNDEFINED,
        NUMBER,
        WORD,
        SPACE,
        SYMBOLS,
        COMMENT,
        OPERATOR,
        KEYWORD,
        CLASS,
        STRING,
        FUNCTION,
        BRACKET_HANDLER;
    }
}
