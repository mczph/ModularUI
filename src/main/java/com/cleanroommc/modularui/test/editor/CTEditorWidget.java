package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.common.widget.textfield.TextEditorWidget;

public class CTEditorWidget extends TextEditorWidget {

    public CTEditorWidget() {
        this.handler = new CTTextEditorHandler();
        this.renderer = new ScriptRenderer(this.handler);
    }
}
