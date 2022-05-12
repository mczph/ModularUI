package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.ModularUI;
import com.google.common.collect.Sets;

import java.util.*;

public class ScriptReader {

    public static class Error {
        public static final String BRACKET = "Invalid character in bracket!";
    }

    public static final Set<Character> OPERATOR = Sets.newHashSet('+', '-', '*', '/', '%', '~', '=', '&', '|', '^', '!', '<', '>');
    public static final Set<Character> OTHER = Sets.newHashSet('(', ')', '{', '}', '[', ']', ';', ',', '.');
    public static final Set<String> KEY_WORD = Sets.newHashSet("import", "val", "var", "static", "global", "in", "has", "for", "while", "if", "else", "..", "function", "void", "as", "true", "false");
    public static final Set<String> PRIMITIVES = Sets.newHashSet("string", "int", "long", "float", "double", "boolean");

    public final Map<String, String> variables = new HashMap<>();
    public final List<Document.Line> lines = new ArrayList<>();
    private List<String> rawLines;
    private String currentLine;
    private int charIndex;
    private Document.Type globalType = Document.Type.UNDEFINED;
    private StringBuilder word = new StringBuilder();
    private CodeCompleter.ZsClass currentClass = null;
    private boolean staticRef = false;

    public void read(List<String> rawLines) {
        this.rawLines = rawLines;
        this.lines.clear();
        this.variables.clear();
        this.globalType = Document.Type.UNDEFINED;
        resetWord();

        for (String line : rawLines) {
            this.currentLine = line;
            readLine();
        }
    }

    public void readLine() {
        this.charIndex = 0;
        List<Document.Token> tokens = new ArrayList<>();
        Document.Token token = nextToken();
        while (token != null) {
            tokens.add(token);
            token = nextToken();
        }
        lines.add(new Document.Line(this.currentLine, tokens));
    }

    public Document.Token nextToken() {
        int start = charIndex;
        resetWord();
        Document.Type type = globalType;
        //int type = 0; // 0 = nothing, 1 = any letters, 2 = number, 3 = operator, 4 = space/tab, 5 = potential bracket (<), 6 = string, 7 = other like (){}[],;. , 8 = comment
        for (; charIndex < currentLine.length(); charIndex++) {
            char c = currentLine.charAt(charIndex);
            if (type == Document.Type.UNDEFINED) {
                if (Character.isDigit(c)) {
                    type = Document.Type.NUMBER;
                } else if (Character.isLetter(c)) {
                    type = Document.Type.WORD;
                } else if (c == '<') {
                    type = Document.Type.BRACKET_HANDLER;
                } else if (OPERATOR.contains(c)) {
                    type = Document.Type.OPERATOR;
                } else if (c == ' ' || c == '\t') {
                    if (c == '\t') {
                        c = ' ';
                        this.word.append("   ");
                    }
                    type = Document.Type.SPACE;
                } else if (c == '"') {
                    type = Document.Type.STRING;
                } else if (OTHER.contains(c)) {
                    type = Document.Type.SYMBOLS;
                } else if (c == '#') {
                    type = Document.Type.COMMENT;
                } else {
                    ModularUI.LOGGER.info("Undefined char {}", c);
                }
            } else {
                switch (type) {
                    case NUMBER: {
                        if (!Character.isDigit(c)) {
                            return new Document.Token(Document.Type.NUMBER, word.toString(), start, null);
                        }
                        break;
                    }
                    case WORD: {
                        if (!isCharLetterLike(c)) {
                            String word = this.word.toString();
                            if (KEY_WORD.contains(word)) {
                                type = Document.Type.KEYWORD;
                            } else if (c == '(') {
                                type = Document.Type.FUNCTION;
                            }
                            return new Document.Token(type, word, start, null);
                        }
                        break;
                    }
                    case BRACKET_HANDLER: {
                        if (word.length() == 1) {
                            if (c == '=') {
                                word.append(c);
                                charIndex++;
                                return new Document.Token(Document.Type.OPERATOR, word.toString(), start, null);
                            }
                            if (!Character.isLetter(c)) {
                                return new Document.Token(Document.Type.OPERATOR, word.toString(), start, null);
                            }
                            break;
                        } else {
                            if (c == '>') {
                                word.append(c);
                                charIndex++;
                                return new Document.Token(Document.Type.BRACKET_HANDLER, word.toString(), start, null);
                            }
                            if (!isCharLetterLike(c) && c != ':' && c != '.') {
                                charIndex = start + 1;
                                return new Document.Token(Document.Type.OPERATOR, "<", start, null);
                            }
                        }
                        break;
                    }
                    case OPERATOR: {
                        if (!OPERATOR.contains(c)) {
                            return new Document.Token(Document.Type.OPERATOR, word.toString(), start, null);
                        }
                        if (word.length() == 1) {
                            char last = word.charAt(0);
                            if ((c == '+' && last == '+') || (c == '-' && last == '-') || (c == '=' && (last == '+' || last == '-' || last == '*' || last == '/' || last == '>' || last == '!' || last == '='))) {
                                word.append(c);
                                charIndex++;
                                return new Document.Token(Document.Type.OPERATOR, word.toString(), start, null);
                            }
                            if (c == '/' && last == '/') {
                                type = Document.Type.COMMENT;
                            } else if (c == '*' && last == '/') {
                                globalType = Document.Type.COMMENT;
                                type = Document.Type.COMMENT;
                            } else {
                                return new Document.Token(Document.Type.OPERATOR, word.toString(), start, null);
                            }
                        }
                        break;
                    }
                    case SPACE: {
                        if (c == '\t') {
                            c = ' ';
                            this.word.append("   ");
                        } else if (c != ' ') {
                            return new Document.Token(Document.Type.SPACE, word.toString(), start, null);
                        }
                        break;
                    }
                    case STRING: {
                        if (c == '"') {
                            word.append(c);
                            charIndex++;
                            return new Document.Token(Document.Type.STRING, word.toString(), start, null);
                        }
                        break;
                    }
                    case SYMBOLS: {
                        if (!OTHER.contains(c)) {
                            return new Document.Token(Document.Type.SYMBOLS, word.toString(), start, null);
                        }
                        break;
                    }
                    case COMMENT: {
                        if (c == '/' && word.length() > 2 && word.charAt(word.length() - 1) == '*') {
                            word.append(c);
                            charIndex++;
                            globalType = Document.Type.UNDEFINED;
                            return new Document.Token(Document.Type.COMMENT, word.toString(), start, null);
                        }
                    }
                }
            }
            word.append(c);
        }
        String word = this.word.toString();
        if (word.trim().isEmpty()) {
            return null;
        }
        switch (type) {
            case WORD: {
                if (KEY_WORD.contains(word)) {
                    type = Document.Type.KEYWORD;
                }
                break;
            }
            case BRACKET_HANDLER: {
                if (word.charAt(word.length() - 1) != '>') {
                    charIndex = start + 1;
                    return new Document.Token(Document.Type.OPERATOR, "<", start, null);
                }
            }
        }
        return new Document.Token(type, word, start, null);
    }

    private void resetWord() {
        word.delete(0, word.length());
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
}
