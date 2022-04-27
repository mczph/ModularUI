package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.api.math.Color;

public class ScriptStyle {

    public static final ScriptStyle ONE_UI = new ScriptStyle();

    static {
        ONE_UI.operatorColor = Color.WHITE.normal;
        ONE_UI.keywordColor = 0xD55FDE;
        ONE_UI.classColor = 0xE5C07B;
        ONE_UI.functionColor = 0x61AFEF;
        ONE_UI.stringColor = 0x89CA78;
        ONE_UI.numberColor = 0xD19A66;
        ONE_UI.commentColor = 0x5C6370;
        ONE_UI.bracketColor = Color.GREEN.normal;
    }

    public int operatorColor;
    public int keywordColor;
    public int classColor;
    public int functionColor;
    public int stringColor;
    public int numberColor;
    public int commentColor;
    public int bracketColor;
    public int defaultColor = Color.WHITE.normal;

    public int getColor(Document.Type type) {
        switch (type) {
            case OPERATOR: return operatorColor;
            case KEYWORD: return keywordColor;
            case FUNCTION: return functionColor;
            case WORD:
            case CLASS: return classColor;
            case BRACKET_HANDLER: return bracketColor;
            case STRING: return stringColor;
            case NUMBER: return numberColor;
            case COMMENT: return commentColor;
            default: return defaultColor;
        }
    }
}
