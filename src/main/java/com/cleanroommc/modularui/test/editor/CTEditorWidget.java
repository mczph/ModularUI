package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.common.widget.textfield.TextEditorWidget;
import net.minecraft.client.renderer.GlStateManager;

public class CTEditorWidget extends TextEditorWidget {

    private final TextRenderer lineNumberRenderer = new TextRenderer();
    private final int lineNumberWidth = 25;
    private final IDrawable separator = new Rectangle().setColor(Color.GREY.dark(1));

    public CTEditorWidget() {
        this.handler = new CTTextEditorHandler();
        this.handler.setMaxLines(10000);
        this.renderer = new ScriptRenderer(this.handler);
        this.handler.setRenderer(this.renderer);

        this.lineNumberRenderer.setSimulate(false);
        this.lineNumberRenderer.setAlignment(Alignment.CenterRight, lineNumberWidth - 5);
        this.lineNumberRenderer.setColor(Color.GREY.dark(1));
    }

    @Override
    public void draw(float partialTicks) {
        separator.draw(lineNumberWidth - 3, 0, 1, size.height, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5f, 0.5f - scrollOffsetY, 0);
        GuiHelper.useScissor(pos.x, pos.y, lineNumberWidth, size.height, () -> {
            lineNumberRenderer.setScale(scale);
            for (int i = 1, n = Math.max(1, handler.getText().size()); i <= n; i++) {
                lineNumberRenderer.setPos(0, (i - 1) * lineNumberRenderer.getFontHeight());
                lineNumberRenderer.draw(String.valueOf(i));
            }
        });
        GuiHelper.useScissor(pos.x + lineNumberWidth, pos.y, size.width - lineNumberWidth, size.height, () -> {
            renderer.setSimulate(false);
            renderer.setScale(scale);
            GlStateManager.translate(lineNumberWidth - scrollOffsetX, 0, 0);
            renderer.setAlignment(textAlignment, -1, size.height);
            renderer.draw(handler.getText());
        });
        GlStateManager.popMatrix();
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!isRightBelowMouse()) {
            return ClickResult.IGNORE;
        }
        int x = getContext().getCursor().getX() - pos.x + scrollOffsetX - lineNumberWidth;
        int y = getContext().getCursor().getY() - pos.y + scrollOffsetY;
        handler.setCursor(renderer.getCursorPos(handler.getText(), x, y));
        return ClickResult.SUCCESS;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        int x = getContext().getCursor().getX() - pos.x + scrollOffsetX - lineNumberWidth;
        int y = getContext().getCursor().getY() - pos.y + scrollOffsetY;
        handler.setMainCursor(renderer.getCursorPos(handler.getText(), x, y));
    }

    @Override
    public int getActualWidth() {
        return super.getActualWidth() + lineNumberWidth;
    }
}
