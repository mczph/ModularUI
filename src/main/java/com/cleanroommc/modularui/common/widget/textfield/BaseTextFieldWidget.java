package com.cleanroommc.modularui.common.widget.textfield;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.api.widget.scroll.IHorizontalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.IVerticalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.ScrollType;
import com.cleanroommc.modularui.common.widget.ScrollBar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The base of a text input widget. Handles mouse/keyboard input and rendering.
 */
public class BaseTextFieldWidget extends Widget implements IWidgetParent, Interactable, IHorizontalScrollable, IVerticalScrollable {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*([+\\-*/%^][0-9]*)*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*([+\\-*/%^][0-9]*)*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[A-Za-z0-9\\s_+\\-.,~!@#$%^&*(){}:;\\\\/|<>\"'\\[\\]?=]");

    protected TextFieldHandler handler = new TextFieldHandler();
    protected TextFieldRenderer renderer = new TextFieldRenderer(handler);
    protected Alignment textAlignment = Alignment.TopLeft;
    protected int scrollOffsetX = 0;
    protected int scrollOffsetY = 0;
    protected float scale = 1f;
    private int cursorTimer;

    protected ScrollBar scrollBarX, scrollBarY;
    private final List<Widget> children = new ArrayList<>();

    public BaseTextFieldWidget() {
        this.handler.setRenderer(renderer);
    }

    @Override
    public void initChildren() {
        if (scrollBarX != null) {
            children.add(scrollBarX);
        }
        if (scrollBarY != null) {
            children.add(scrollBarY);
        }
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    @Override
    public void onScreenUpdate() {
        if (isFocused() && ++cursorTimer == 10) {
            renderer.toggleCursor();
            cursorTimer = 0;
        }
    }

    @Override
    public void draw(float partialTicks) {
        GuiHelper.useScissor(pos.x, pos.y, size.width, size.height, () -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5f - scrollOffsetX, 0.5f - scrollOffsetY, 0);
            renderer.setSimulate(false);
            renderer.setScale(scale);
            renderer.setAlignment(textAlignment, -1, size.height);
            renderer.draw(handler.getText());
            GlStateManager.popMatrix();
        });
    }

    @Override
    public boolean shouldGetFocus() {
        this.cursorTimer = 0;
        this.renderer.setCursor(true);
        return true;
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!isRightBelowMouse()) {
            return ClickResult.IGNORE;
        }
        handler.setCursor(renderer.getCursorPos(handler.getText(), getContext().getCursor().getX() - pos.x + scrollOffsetX, getContext().getCursor().getY() - pos.y + scrollOffsetY));
        return ClickResult.SUCCESS;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        handler.setMainCursor(renderer.getCursorPos(handler.getText(), getContext().getCursor().getX() - pos.x + scrollOffsetX, getContext().getCursor().getY() - pos.y + scrollOffsetY));
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (canScrollHorizontal() && (canScrollVertical() && Interactable.hasShiftDown()) || !canScrollVertical()) {
            return scrollBarX.onMouseScroll(direction);
        }
        if (canScrollVertical()) {
            return scrollBarY.onMouseScroll(direction);
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(char character, int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_RETURN:
                if (getMaxLines() > 1) {
                    handler.newLine();
                } else {
                    removeFocus();
                }
                return true;
            case Keyboard.KEY_ESCAPE:
                if (isFocused()) {
                    removeFocus();
                    return true;
                }
                return false;
            case Keyboard.KEY_LEFT: {
                handler.moveCursorLeft(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_RIGHT: {
                handler.moveCursorRight(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_UP: {
                handler.moveCursorUp(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_DOWN: {
                handler.moveCursorDown(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_DELETE:
                handler.delete(true);
                return true;
            case Keyboard.KEY_BACK:
                handler.delete();
                return true;
        }

        if (character == Character.MIN_VALUE) {
            return false;
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(handler.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            // paste copied text in marked text
            handler.insert(GuiScreen.getClipboardString());
            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode) && handler.hasTextMarked()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(handler.getSelectedText());
            handler.delete();
            return true;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            handler.markAll();
            return true;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches()) {
            // insert typed char
            handler.insert(String.valueOf(character));
            return true;
        }
        return false;
    }

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();
        this.renderer.setCursor(false);
        this.cursorTimer = 0;
        this.scrollOffsetX = 0;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        int height = maxHeight;
        if (scrollBarY == null) {
            height = (int) (renderer.getFontHeight() * getMaxLines() + 0.5);
        }
        return new Size(maxWidth, height);
    }

    public boolean canScrollHorizontal() {
        return this.scrollBarX != null && this.scrollBarX.isActive();
    }

    public boolean canScrollVertical() {
        return this.scrollBarY != null && this.scrollBarY.isActive();
    }

    @Override
    public void setHorizontalScrollOffset(int offset) {
        if (this.scrollBarX != null && this.scrollBarX.isActive()) {
            this.scrollOffsetX = offset;
        } else {
            this.scrollOffsetX = 0;
        }
    }

    @Override
    public int getHorizontalScrollOffset() {
        return this.scrollOffsetX;
    }

    @Override
    public int getVisibleWidth() {
        return size.width;
    }

    @Override
    public int getActualWidth() {
        return (int) Math.ceil(renderer.getLastWidth());
    }

    @Override
    public void setVerticalScrollOffset(int offset) {
        if (this.scrollBarY != null && this.scrollBarY.isActive()) {
            this.scrollOffsetY = offset;
        } else {
            this.scrollOffsetY = 0;
        }
    }

    @Override
    public int getVerticalScrollOffset() {
        return scrollOffsetY;
    }

    @Override
    public int getVisibleHeight() {
        return size.height;
    }

    @Override
    public int getActualHeight() {
        return (int) renderer.getLastHeight();
    }

    public int getMaxLines() {
        return handler.getMaxLines();
    }

    public BaseTextFieldWidget setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public BaseTextFieldWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public BaseTextFieldWidget setHorizontalScrollBar(@Nullable ScrollBar scrollBarX) {
        this.scrollBarX = scrollBarX;
        this.handler.setScrollBar(scrollBarX);
        if (this.scrollBarX != null) {
            this.scrollBarX.setScrollType(ScrollType.HORIZONTAL, this, null);
        }
        return this;
    }

    public BaseTextFieldWidget setVerticalScrollBar(@Nullable ScrollBar scrollBarY) {
        this.scrollBarY = scrollBarY;
        if (this.scrollBarY != null) {
            this.scrollBarY.setScrollType(ScrollType.VERTICAL, null, this);
        }
        return this;
    }

    public BaseTextFieldWidget setScrollable(boolean horizontal, boolean vertical) {
        if (horizontal) {
            setHorizontalScrollBar(new ScrollBar()
                    .setBarTexture(new Rectangle().setColor(Color.WHITE.normal).setCornerRadius(1)));
        }
        if (vertical) {
            setVerticalScrollBar(new ScrollBar()
                    .setBarTexture(new Rectangle().setColor(Color.WHITE.normal).setCornerRadius(1)));
        }
        return this;
    }

    public BaseTextFieldWidget setTextColor(int color) {
        this.renderer.setColor(color);
        return this;
    }
}
