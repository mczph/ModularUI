package com.cleanroommc.modularui.test.editor;

import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.TabContainer;

public class TestScriptEditor {

    public static ModularWindow openWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builderFullScreen();
        builder.setBackground(new Rectangle()
                .setColor(Color.argb(30, 30, 30, 120)))
                .widget(new Rectangle()
                        .setColor(Color.WHITE.dark(1))
                        .asWidget()
                        .setPos(0, 24)
                        .setSizeProvider((screenSize, window, parent) -> new Size(screenSize.width, 3)))
                .widget(new Rectangle()
                        .setColor(Color.WHITE.dark(1))
                        .asWidget()
                        .setPos(70, 0)
                        .setSizeProvider((screenSize, window, parent) -> new Size(3, screenSize.height)))
                .widget(new TabContainer()
                        .addPage(new CTEditorWidget()
                                .setTextColor(Color.WHITE.dark(1))
                                .setScrollable(true, true)
                                .setPos(2, 2)
                                .setSizeProvider((screenSize, window, parent) -> screenSize.shrink(77, 31)))
                        .setPos(73, 27));

        return builder.build();
    }
}
