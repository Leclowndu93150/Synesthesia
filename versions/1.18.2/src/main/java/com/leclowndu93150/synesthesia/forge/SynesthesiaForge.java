package com.leclowndu93150.synesthesia.forge;

import com.leclowndu93150.synesthesia.Synesthesia;
import com.leclowndu93150.synesthesia.client.ClientConfig;
import com.leclowndu93150.synesthesia.client.SoundRenderer;
import com.leclowndu93150.synesthesia.client.VisualizerState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(Synesthesia.MODID)
public class SynesthesiaForge {

    public static final KeyMapping TOGGLE_VISUALIZER = new KeyMapping(
            "key.synesthesia.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F25,
            "key.categories.synesthesia"
    );

    public SynesthesiaForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig.SPEC);
        ClientConfig.setRenderThroughWallsSupplier(() -> ForgeClientConfig.RENDER_THROUGH_WALLS.get());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(SynesthesiaForge::onClientSetup);
        MinecraftForge.EVENT_BUS.register(Events.class);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ClientRegistry.registerKeyBinding(TOGGLE_VISUALIZER));
    }

    @Mod.EventBusSubscriber(modid = Synesthesia.MODID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            while (TOGGLE_VISUALIZER.consumeClick()) {
                VisualizerState.toggle();
                TranslatableComponent message = new TranslatableComponent(
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
