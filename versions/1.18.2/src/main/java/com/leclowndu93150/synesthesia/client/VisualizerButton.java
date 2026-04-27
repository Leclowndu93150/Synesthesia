package com.leclowndu93150.synesthesia.client;

import com.leclowndu93150.synesthesia.Synesthesia;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class VisualizerButton extends Button {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Synesthesia.MODID, "textures/gui/visualizer_button.png"
    );

    private static final int TEXTURE_WIDTH = 23;
    private static final int TEXTURE_HEIGHT = 19;

    public VisualizerButton(int x, int y, OnPress onPress) {
        super(x, y, 17, 17, new TextComponent(""), onPress);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int texX = this.x - (TEXTURE_WIDTH - 17);
        int texY = this.y - (TEXTURE_HEIGHT - 17);

        blit(poseStack, texX, texY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public Component getTooltipText() {
        if (VisualizerState.isEnabled()) {
            return new TranslatableComponent("synesthesia.button.visualizer.enabled");
        } else {
            return new TranslatableComponent("synesthesia.button.visualizer.disabled");
        }
    }
}
