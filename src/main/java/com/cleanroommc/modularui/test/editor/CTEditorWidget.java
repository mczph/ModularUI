package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.common.widget.textfield.TextEditorWidget;

public class CTEditorWidget extends TextEditorWidget {

    public CTEditorWidget() {
        this.handler = new CTTextEditorHandler();
        this.handler.setMaxLines(10000);
        this.renderer = new ScriptRenderer(this.handler);
        this.handler.setRenderer(this.renderer);
    }
}
