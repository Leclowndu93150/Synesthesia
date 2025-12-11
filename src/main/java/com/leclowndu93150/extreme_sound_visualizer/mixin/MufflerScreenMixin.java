package com.leclowndu93150.extreme_sound_visualizer.mixin;

import com.leclowndu93150.extreme_sound_visualizer.client.VisualizerButton;
import com.leclowndu93150.extreme_sound_visualizer.client.VisualizerState;
import com.leobeliik.extremesoundmuffler.gui.MufflerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MufflerScreen.class)
public abstract class MufflerScreenMixin extends Screen {

    @Shadow(remap = false)
    private EditBox searchBar;

    @Unique
    private VisualizerButton esv_visualizerButton;

    protected MufflerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "addButtons", at = @At("TAIL"), remap = false)
    private void esv_addVisualizerButton(CallbackInfo ci) {
        int guiX = (this.width - 256) / 2;
        int guiY = (this.height - 202) / 2;

        int buttonX = guiX + 182;
        int buttonY = guiY + 179;

        if (searchBar != null) {
            searchBar.setWidth(103);
        }

        esv_visualizerButton = new VisualizerButton(buttonX, buttonY, button -> {
            VisualizerState.toggle();
        });

        this.addRenderableWidget(esv_visualizerButton);
    }

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void esv_renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (esv_visualizerButton != null && esv_visualizerButton.isHovered()) {
            guiGraphics.renderTooltip(this.font, esv_visualizerButton.getTooltipText(), mouseX, mouseY);
        }
    }
}
