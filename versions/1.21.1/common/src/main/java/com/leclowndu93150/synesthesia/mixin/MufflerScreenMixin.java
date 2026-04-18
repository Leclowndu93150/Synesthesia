package com.leclowndu93150.synesthesia.mixin;

import com.leclowndu93150.synesthesia.client.VisualizerButton;
import com.leclowndu93150.synesthesia.client.VisualizerState;
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
    private VisualizerButton syn_visualizerButton;

    protected MufflerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "addButtons", at = @At("TAIL"), remap = false)
    private void syn_addVisualizerButton(CallbackInfo ci) {
        int guiX = (this.width - 256) / 2;
        int guiY = (this.height - 202) / 2;

        int buttonX = guiX + 182;
        int buttonY = guiY + 179;

        if (searchBar != null) {
            searchBar.setWidth(103);
        }

        syn_visualizerButton = new VisualizerButton(buttonX, buttonY, button -> {
            VisualizerState.toggle();
        });

        this.addRenderableWidget(syn_visualizerButton);
    }

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void syn_renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (syn_visualizerButton != null && syn_visualizerButton.isHovered()) {
            guiGraphics.renderTooltip(this.font, syn_visualizerButton.getTooltipText(), mouseX, mouseY);
        }
    }
}
