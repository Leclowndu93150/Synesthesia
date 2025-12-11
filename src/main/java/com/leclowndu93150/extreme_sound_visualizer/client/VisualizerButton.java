package com.leclowndu93150.extreme_sound_visualizer.client;

import com.leclowndu93150.extreme_sound_visualizer.ExtremeSoundVisualizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class VisualizerButton extends Button {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExtremeSoundVisualizer.MODID, "textures/gui/visualizer_button.png"
    );

    private static final int TEXTURE_WIDTH = 23;
    private static final int TEXTURE_HEIGHT = 19;

    public VisualizerButton(int x, int y, OnPress onPress) {
        super(x, y, 17, 17, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int texX = this.getX() - (TEXTURE_WIDTH - 17);
        int texY = this.getY() - (TEXTURE_HEIGHT - 17);

        guiGraphics.blit(TEXTURE, texX, texY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public Component getTooltipText() {
        if (VisualizerState.isEnabled()) {
            return Component.translatable("extremesoundvisualizer.button.visualizer.enabled");
        } else {
            return Component.translatable("extremesoundvisualizer.button.visualizer.disabled");
        }
    }
}
