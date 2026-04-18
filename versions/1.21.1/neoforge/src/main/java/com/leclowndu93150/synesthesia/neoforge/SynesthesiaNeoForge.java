package com.leclowndu93150.synesthesia.neoforge;

import com.leclowndu93150.synesthesia.Synesthesia;
import com.leclowndu93150.synesthesia.client.ClientConfig;
import com.leclowndu93150.synesthesia.client.SoundRenderer;
import com.leclowndu93150.synesthesia.client.VisualizerState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = Synesthesia.MODID, dist = Dist.CLIENT)
public class SynesthesiaNeoForge {

    public static final KeyMapping TOGGLE_VISUALIZER = new KeyMapping(
            "key.synesthesia.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F25,
            "key.categories.synesthesia"
    );

    public SynesthesiaNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC);
        ClientConfig.setRenderThroughWallsSupplier(() -> NeoForgeClientConfig.RENDER_THROUGH_WALLS.get());

        NeoForge.EVENT_BUS.register(Events.class);
        modEventBus.addListener(SynesthesiaNeoForge::onRegisterKeyMappings);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VISUALIZER);
    }

    @EventBusSubscriber(modid = Synesthesia.MODID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            while (TOGGLE_VISUALIZER.consumeClick()) {
                VisualizerState.toggle();
                Component message = Component.translatable(
                        VisualizerState.isEnabled() ? "message.synesthesia.enabled" : "message.synesthesia.disabled"
                );
                mc.player.displayClientMessage(message, true);
            }
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                return;
            }
            SoundRenderer.render(event.getPoseStack(), event.getCamera());
        }
    }
}
