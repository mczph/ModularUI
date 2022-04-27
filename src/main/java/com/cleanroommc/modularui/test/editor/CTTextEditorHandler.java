package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.common.widget.textfield.TextFieldHandler;

import java.awt.*;

public class CTTextEditorHandler extends TextFieldHandler {

    private final Document document = new Document();

    public Document getDocument() {
        return document;
    }

    @Override
    public void newLine() {
        if (hasTextMarked()) {
            delete(false);
        }
        String line = this.text.get(cursor.y);
        if (line.length() == 0) {
            this.text.add(cursor.y + 1, "");
            setCursor(cursor.y + 1, 0);
            return;
        }
        this.text.set(cursor.y, cursor.x > line.length() ? line : line.substring(0, cursor.x));
        int indent = getIndent(line);
        if (cursor.x < line.length() && cursor.x > 0 && line.charAt(cursor.x - 1) == '{' && line.charAt(cursor.x) == '}') {
            this.text.add(cursor.y + 1, createIndent(indent + 4));
            this.text.add(cursor.y + 2, createIndent(indent) + line.substring(cursor.x));
            setCursor(cursor.y + 1, indent + 4);
        } else {
            this.text.add(cursor.y + 1, cursor.x > line.length() ? createIndent(indent) : createIndent(indent) + line.substring(cursor.x));
            setCursor(cursor.y + 1, indent);
        }
        onChanged();
    }

    public static int getIndent(String line) {
        int c = 0;
        while (line.length() > c && line.charAt(c) == ' ') {
            c++;
        }
        return c;
    }

    public static String createIndent(int amount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    @Override
    protected void onChanged() {
        super.onChanged();
        document.scan(getText());
    }

    @Override
    protected void onCharInserted(char c) {
        Point main = getMainCursor();
        char other = Character.MIN_VALUE;
        if (c == '(') other = ')';
        else if (c == '{') other = '}';
        else if (c == '[') other = ']';
        else if (c == '<') other = '>';
        if (other != Character.MIN_VALUE) {
            String line = getText().get(main.y);
            String part1 = line.substring(0, main.x);
            String part2 = line.substring(main.x);
            getText().set(main.y, part1 + other + part2);
        }
    }
}
