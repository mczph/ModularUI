package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.api.math.Color;

public class ScriptStyle {

    public static final ScriptStyle ONE_UI = new ScriptStyle();

    static {
        ONE_UI.operatorColor = Color.WHITE.normal;
        ONE_UI.keywordColor = Color.PURPLE.normal;
        ONE_UI.classColor = Color.YELLOW.normal;
        ONE_UI.functionColor = Color.BLUE.normal;
        ONE_UI.stringColor = Color.LIGHT_GREEN.normal;
        ONE_UI.commentColor = Color.BLUE.bright(4);
        ONE_UI.bracketColor = Color.GREEN.normal;
    }

    public int operatorColor;
    public int keywordColor;
    public int classColor;
    public int functionColor;
    public int stringColor;
    public int commentColor;
    public int bracketColor;
    public int defaultColor = Color.WHITE.normal;

    public int getColor(Document.Type type) {
        switch (type) {
            case OPERATOR: return operatorColor;
            case KEYWORD: return keywordColor;
            case FUNCTION: return functionColor;
            case CLASS: return classColor;
            case BRACKET_HANDLER: return bracketColor;
            case COMMENT: return commentColor;
            default: return defaultColor;
        }
    }
}
