package com.leclowndu93150.extreme_sound_visualizer.client;

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
            "key.extremesoundvisualizer.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.extremesoundvisualizer"
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
                message = Component.translatable("message.extremesoundvisualizer.enabled");
            } else {
                message = Component.translatable("message.extremesoundvisualizer.disabled");
            }

            mc.player.displayClientMessage(message, true);
        }
    }
}
