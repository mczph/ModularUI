package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.UIInfos;
import com.cleanroommc.modularui.common.internal.JsonLoader;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.test.KeyBind;
import com.cleanroommc.modularui.test.editor.TestScriptEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ModularUI.ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void postInit() {
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this::onReload);
    }

    public void onReload(IResourceManager manager) {
        ModularUI.LOGGER.info("Reloading GUIs");
        JsonLoader.loadJson();
    }

    @SubscribeEvent
    public static void mouseScreenInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (event.getGui() instanceof ModularGui) {
            int w = Mouse.getEventDWheel();
            int wheel = MathHelper.clamp(w, -1, 1);
            if (wheel != 0) {
                ((ModularGui) event.getGui()).mouseScroll(wheel);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void key(InputEvent.KeyInputEvent event) {
        if (KeyBind.OPEN_EDITOR.isKeyDown() || KeyBind.OPEN_EDITOR.isPressed()) {
            UIInfos.openClientUI(Minecraft.getMinecraft().player, TestScriptEditor::openWindow);
        }
    }
}
