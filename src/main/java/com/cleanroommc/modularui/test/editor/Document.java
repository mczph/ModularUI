package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.ModularUI;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Document {

    public static class Error {
        public static final String EXPECTED = "Expected '%s', but got '%s'";
    }

    public static final Set<String> OPERATOR = Sets.newHashSet("+", "-", "*", "/", "%", "+", "-", "~", "=", "&", "|", "^", "!");
    public static final Set<String> KEY_WORD = Sets.newHashSet("import", "val", "var", "static", "global", "in", "has", "for", "while", "if", "else", "..", "function", "void", "as");
    public static final String SEGMENT_PATTERN = "[ .;]";
    public final Map<String, String> variables = new HashMap<>();
    public final List<Line> lines = new ArrayList<>();

    private List<Token> tokens;
    private final StringBuilder word = new StringBuilder();
    private int lastStart = 0;
    private Type currentType;
    // 0 = nothing, 1 = comment, 2 = number, 3 = any word
    private byte type = 0;
    private int openBrace = 0, openCurlyBrace = 0;
    private char expectedBrace = Character.MIN_VALUE;
    private String previousClassRef = null;
    private boolean staticRef = false;


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
        variables.clear();
        this.lines.clear();

        for (String line : lines) {
            boolean doScan = true;
            String line1 = line;
            if (currentType == Type.COMMENT) {
                int end = line.indexOf("*/");
                String comment = line;
                if (end >= 0) {
                    comment = line.substring(0, end + 2);
                    line1 = line.substring(end + 2);
                } else {
                    doScan = false;
                }
                word.append(comment);
                addToken(Type.COMMENT, end + 1);
            }
            if (doScan) {
                scanLine(line1);
            }
            this.lines.add(new Line(line, tokens));
        }
    }

    private void scanLine(String rawLine) {
        tokens = new ArrayList<>();
        lastStart = 0;

        for (int i = 0; i < rawLine.length(); i++) {
            char c = rawLine.charAt(i);
            // comment
            if (c == '#' || (getLastChar() == '/' && (c == '/' || c == '*'))) {
                if (c == '/' || c == '*') {
                    word.deleteCharAt(word.length() - 1);
                    addToken(Type.UNDEFINED, i - 1);
                    word.append('/');
                } else {
                    addToken(Type.UNDEFINED, i);
                }
                word.append(rawLine.substring(i));
                addToken(Type.COMMENT, 0);
                if (c == '*') {
                    currentType = Type.COMMENT;
                }
                break;
            }

            if (isCharLetterLike(c)) {
                word.append(c);
            } else {
                if (KEY_WORD.contains(word.toString())) {
                    addToken(Type.KEYWORD, i);
                    handleChar(i, c);
                    continue;
                }
                if (OPERATOR.contains(String.valueOf(c))) {

                }
            }
        }
    }

    private void handleChar(int index, char c) {
        if (c == ')' || c == '}' || c == ']' || c == '>') {
            if (expectedBrace != c) {

            }
        }
    }

    private void addToken(Type type, int newStart) {
        addToken(type, newStart, null);
    }

    private void addToken(Type type, int newStart, String error) {
        this.tokens.add(new Token(type, word.toString(), lastStart, error));
        lastStart = newStart;
        resetWord();
    }

    private void resetWord() {
        word.delete(0, word.length());
    }

    private char getLastChar() {
        return getLastChar(0);
    }

    private char getLastChar(int index) {
        return index >= 0 && index < word.length() ? word.charAt(word.length() - index - 1) : Character.MIN_VALUE;
    }

    public static boolean isCharLetterLike(char c) {
        if (Character.isLetter(c) || Character.isDigit(c)) {
            return true;
        }
        switch (c) {
            case '$':
            case '_':
                return true;
            default:
                return false;
        }
    }

    public void scan2(List<String> rawLines) {
        String errorMsg = null;
        ModularUI.LOGGER.info("Scanning text {}", rawLines);
        variables.clear();
        lines.clear();
        Type type = Type.UNDEFINED;
        boolean openBracket = false;
        boolean string = false;
        StringBuilder word = new StringBuilder();
        linesLoop:
        for (String rawLine : rawLines) {
            List<Token> tokens = new ArrayList<>();
            int lastStart = 0;
            for (int i = 0; i < rawLine.length(); i++) {
                char c = rawLine.charAt(i);

                // block comment & end
                if (type == Type.COMMENT) {
                    if (word.length() > 0 && word.charAt(word.length() - 1) == '*' && c == '/') {
                        word.append(c);
                        tokens.add(new Token(Type.COMMENT, word.toString(), lastStart, errorMsg));
                        word = new StringBuilder();
                        lastStart = i + 1;
                        type = Type.UNDEFINED;
                        continue;
                    }
                    word.append(c);
                    continue;
                }

                if (OPERATOR.contains(Character.toString(c))) {

                }

                // white space
                if (c == ' ' || c == '\t') {
                    if (word.length() > 0) {
                        if (word.length() <= 2 && OPERATOR.contains(word.toString())) {
                            tokens.add(new Token(Type.OPERATOR, word.toString(), lastStart, errorMsg));
                            word = new StringBuilder();
                            lastStart = i + 1;
                            continue;
                        }
                        if (KEY_WORD.contains(word.toString())) {
                            tokens.add(new Token(Type.KEYWORD, word.toString(), lastStart, errorMsg));
                            word = new StringBuilder();
                            lastStart = i + 1;
                            continue;
                        }
                        tokens.add(new Token(type, word.toString(), lastStart, errorMsg));
                        word = new StringBuilder();
                    }
                    lastStart = i + 1;
                    continue;
                }

                // block comment start
                if (word.length() > 0) {
                    if (word.charAt(word.length() - 1) == '/' && c == '*') {
                        word.deleteCharAt(word.length() - 1);
                        if (word.length() > 0) {
                            tokens.add(new Token(Type.UNDEFINED, word.toString(), lastStart, errorMsg));
                            word = new StringBuilder();
                            lastStart = i + 1;
                        }
                        word.append('/').append(c);
                        type = Type.COMMENT;
                    }
                }

                // line comment
                if (c == '#' || (word.length() > 0 && word.charAt(word.length() - 1) == '/' && c == '/')) {
                    if (c == '/') {
                        word.deleteCharAt(word.length() - 1);
                    }
                    if (word.length() > 0) {
                        tokens.add(new Token(Type.KEYWORD, word + rawLine.substring(i + 1), lastStart, errorMsg));
                        word = new StringBuilder();
                        lastStart = i;
                    }
                    if (c == '/') {
                        word.append('/');
                    }
                    word.append(c);
                    tokens.add(new Token(Type.COMMENT, word + rawLine.substring(i), lastStart, errorMsg));
                    break;
                }

                if (c == '"') {
                    if (string) {
                        word.append(c);
                        tokens.add(new Token(Type.STRING, word.toString(), lastStart, errorMsg));
                        word = new StringBuilder();
                        lastStart = i;
                        string = false;
                    } else {
                        tokens.add(new Token(Type.UNDEFINED, word.toString(), lastStart, errorMsg));
                        word = new StringBuilder();
                        lastStart = i;
                        string = true;
                        word.append(c);
                    }
                    continue;
                }

                // bracket handler
                if (c == '<') {
                    openBracket = true;
                    if (word.length() > 0) {
                        tokens.add(new Token(type, word.toString(), lastStart, errorMsg));
                        word = new StringBuilder();
                        lastStart = i;
                    }
                    word.append(c);
                    continue;
                }
                if (c == '>' && openBracket) {
                    openBracket = false;
                    word.append(c);
                    tokens.add(new Token(Type.BRACKET_HANDLER, word.toString(), lastStart, errorMsg));
                    continue;
                }

                if (c == '.' && word.length() > 0) {
                    tokens.add(new Token(type == Type.UNDEFINED ? Type.REF : type, word.toString(), lastStart, errorMsg));
                    word = new StringBuilder();
                    lastStart = i;
                    continue;
                }

                if (c == '(' && word.length() > 0) {
                    tokens.add(new Token(Type.FUNCTION, word.toString(), lastStart, errorMsg));
                    word = new StringBuilder();
                    lastStart = i + 1;
                    continue;
                }
                word.append(c);
            }
            if (word.length() > 0) {
                tokens.add(new Token(Type.UNDEFINED, word.toString(), lastStart, errorMsg));
            }
            ModularUI.LOGGER.info(" - adding line {}, {}", tokens, rawLine);
            lines.add(new Line(rawLine, tokens));
        }
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
        COMMENT,
        OPERATOR,
        KEYWORD,
        CLASS,
        VAR_DEF,
        STRING,
        FUNCTION,
        REF,
        BRACKET_HANDLER;
    }

    public enum Bracket {
        ROUND, SQUARE, CURLY, POINTY
    }
}
