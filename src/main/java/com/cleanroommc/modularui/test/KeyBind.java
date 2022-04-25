package com.cleanroommc.modularui.test;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.function.Supplier;

public enum KeyBind {

    OPEN_EDITOR("editor", Keyboard.KEY_V);

    public static final KeyBind[] VALUES = values();

    @SideOnly(Side.CLIENT)
    private KeyBinding keybinding;
    @SideOnly(Side.CLIENT)
    private boolean isPressed, isKeyDown;

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    KeyBind(Supplier<Supplier<KeyBinding>> keybindingGetter) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = keybindingGetter.get().get();
        }
    }

    KeyBind(String langKey, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, button, "GregTech");
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    KeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, ctx, button, "GregTech");
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    @SideOnly(Side.CLIENT)
    public KeyBinding toMinecraft() {
        return this.keybinding;
    }

    @SideOnly(Side.CLIENT)
    public boolean isPressed() {
        return this.keybinding.isPressed();
    }

    @SideOnly(Side.CLIENT)
    public boolean isKeyDown() {
        return this.keybinding.isKeyDown();
    }
}
