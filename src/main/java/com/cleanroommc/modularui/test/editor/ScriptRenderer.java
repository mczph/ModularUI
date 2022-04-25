package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ScriptRenderer extends TextFieldRenderer {

    public ScriptRenderer(TextFieldHandler handler) {
        super(handler);
    }

    @Override
    public void draw(List<String> lines) {
        drawCursors(measureLines(lines));
        drawScriptLines(measureScriptLines(lines));
    }

    protected List<Pair<Document.Line, Float>> measureScriptLines(List<String> lines) {
        List<Pair<Document.Line, Float>> measuredLines = new ArrayList<>();
        Document doc = ((CTTextEditorHandler) this.handler).getDocument();
        for (int i = 0; i < lines.size(); i++) {
            Document.Line line = doc.getLine(i);
            if (line == null) continue;
            float width = FR.getStringWidth(line.rawText) * scale;
            measuredLines.add(Pair.of(line, width));
        }
        return measuredLines;
    }

    protected void drawScriptLines(List<Pair<Document.Line, Float>> measuredLines) {
        float maxW = 0;
        float y0 = getStartY(measuredLines.size());
        for (Pair<Document.Line, Float> measuredLine : measuredLines) {
            float x0 = getStartX(measuredLine.getRight());
            for (Document.Token token : measuredLine.getKey().tokens) {
                color = ScriptStyle.ONE_UI.getColor(token.type);
                x0 += draw(token.text, x0, y0);
            }
            maxW = Math.max(x0, maxW);
            y0 += FR.FONT_HEIGHT * scale;
        }
        this.lastWidth = maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW;
        this.lastHeight = measuredLines.size() * FR.FONT_HEIGHT * scale;
    }

    @Override
    protected void drawMeasuredLines(List<Pair<String, Float>> measuredLines) {
        drawCursors(measuredLines);
        float maxW = 0;
        float y0 = getStartY(measuredLines.size());
        for (Pair<String, Float> measuredLine : measuredLines) {
            float x0 = getStartX(measuredLine.getRight());
            maxW = Math.max(draw(measuredLine.getLeft(), x0, y0), maxW);
            y0 += FR.FONT_HEIGHT * scale;
        }
        this.lastWidth = maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW;
        this.lastHeight = measuredLines.size() * FR.FONT_HEIGHT * scale;
    }
}
