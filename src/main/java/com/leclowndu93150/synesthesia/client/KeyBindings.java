package com.leclowndu93150.synesthesia.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final KeyMapping TOGGLE_VISUALIZER = new KeyMapping(
            "key.synesthesia.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F25,
            "key.categories.synesthesia"
    );

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VISUALIZER);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        while (TOGGLE_VISUALIZER.consumeClick()) {
            VisualizerState.toggle();

            Component message;
            if (VisualizerState.isEnabled()) {
                message = Component.translatable("message.synesthesia.enabled");
            } else {
                message = Component.translatable("message.synesthesia.disabled");
            }

            mc.player.displayClientMessage(message, true);
        }
    }
}
