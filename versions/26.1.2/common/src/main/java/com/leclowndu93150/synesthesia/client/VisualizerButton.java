package com.leclowndu93150.synesthesia.client;

import com.leclowndu93150.synesthesia.Synesthesia;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class VisualizerButton extends Button {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Synesthesia.MODID, "textures/gui/visualizer_button.png"
    );

    private static final int TEXTURE_WIDTH = 23;
    private static final int TEXTURE_HEIGHT = 19;

    public VisualizerButton(int x, int y, OnPress onPress) {
        super(x, y, 17, 17, Component.empty(), onPress, DEFAULT_NARRATION);
        this.setTooltip(Tooltip.create(Component.translatable("synesthesia.button.visualizer.disabled")));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int texX = this.getX() - (TEXTURE_WIDTH - 17);
        int texY = this.getY() - (TEXTURE_HEIGHT - 17);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, texX, texY, 0f, 0f, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        Component tip = VisualizerState.isEnabled()
                ? Component.translatable("synesthesia.button.visualizer.enabled")
                : Component.translatable("synesthesia.button.visualizer.disabled");
        this.setTooltip(Tooltip.create(tip));
    }
}
