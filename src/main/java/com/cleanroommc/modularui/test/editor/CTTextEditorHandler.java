package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.common.widget.textfield.TextFieldHandler;

public class CTTextEditorHandler extends TextFieldHandler {

    private final Document document = new Document();

    public Document getDocument() {
        return document;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        document.scan(getText());
    }
}
