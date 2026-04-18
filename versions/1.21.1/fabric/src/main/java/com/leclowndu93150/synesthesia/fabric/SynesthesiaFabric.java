package com.leclowndu93150.synesthesia.fabric;

import com.leclowndu93150.synesthesia.Synesthesia;
import com.leclowndu93150.synesthesia.client.ClientConfig;
import com.leclowndu93150.synesthesia.client.SoundRenderer;
import com.leclowndu93150.synesthesia.client.VisualizerState;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SynesthesiaFabric implements ClientModInitializer {

    public static final KeyMapping TOGGLE_VISUALIZER = new KeyMapping(
            "key.synesthesia.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F25,
            "key.categories.synesthesia"
    );

    @Override
    public void onInitializeClient() {
        ClientConfig.setRenderThroughWallsSupplier(FabricClientConfig::renderThroughWalls);
        FabricClientConfig.load();

        KeyBindingHelper.registerKeyBinding(TOGGLE_VISUALIZER);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (TOGGLE_VISUALIZER.consumeClick()) {
                VisualizerState.toggle();
                Component message = Component.translatable(
                        VisualizerState.isEnabled() ? "message.synesthesia.enabled" : "message.synesthesia.disabled"
                );
                client.player.displayClientMessage(message, true);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            SoundRenderer.render(context.matrixStack(), context.camera());
        });
    }
}
