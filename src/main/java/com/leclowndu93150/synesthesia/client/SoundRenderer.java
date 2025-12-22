package com.leclowndu93150.synesthesia.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundRenderer {

    private static final double LABEL_STACK_THRESHOLD = 1.0;
    private static final float LABEL_OFFSET = 0.3f;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        if (!VisualizerState.isEnabled()) {
            return;
        }

        SoundVisualizerData.tick();

        List<ActiveSoundEntry> sounds = SoundVisualizerData.getActiveSounds();
        if (sounds.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        boolean renderThroughWalls = ClientConfig.RENDER_THROUGH_WALLS.get();
        if (renderThroughWalls) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        Map<Long, List<ActiveSoundEntry>> groupedSounds = groupSoundsByPosition(sounds);

        for (ActiveSoundEntry entry : sounds) {
            renderSoundHighlight(poseStack, camera, entry, cameraPos);
        }

        for (Map.Entry<Long, List<ActiveSoundEntry>> group : groupedSounds.entrySet()) {
            List<ActiveSoundEntry> entries = group.getValue();
            for (int i = 0; i < entries.size(); i++) {
                float yOffset = i * LABEL_OFFSET;
                renderSoundLabel(poseStack, bufferSource, entries.get(i), cameraPos, camera, yOffset);
            }
        }

        bufferSource.endBatch();

        if (renderThroughWalls) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static Map<Long, List<ActiveSoundEntry>> groupSoundsByPosition(List<ActiveSoundEntry> sounds) {
        Map<Long, Map<String, ActiveSoundEntry>> grouped = new HashMap<>();

        for (ActiveSoundEntry entry : sounds) {
            Vec3 pos = entry.getCurrentPosition();
            long key = packPosition(pos.x, pos.z);
            String soundId = entry.getSoundIdString();

            Map<String, ActiveSoundEntry> positionGroup = grouped.computeIfAbsent(key, k -> new HashMap<>());

            if (!positionGroup.containsKey(soundId) || entry.getOpacity() > positionGroup.get(soundId).getOpacity()) {
                positionGroup.put(soundId, entry);
            }
        }

        Map<Long, List<ActiveSoundEntry>> result = new HashMap<>();
        for (Map.Entry<Long, Map<String, ActiveSoundEntry>> entry : grouped.entrySet()) {
            List<ActiveSoundEntry> entries = new ArrayList<>(entry.getValue().values());
            entries.sort((a, b) -> Double.compare(a.getCurrentPosition().y, b.getCurrentPosition().y));
            result.put(entry.getKey(), entries);
        }

        return result;
    }

    private static long packPosition(double x, double z) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        return ((long) ix << 32) | (iz & 0xFFFFFFFFL);
    }

    private static void renderSoundHighlight(PoseStack poseStack, Camera camera,
                                             ActiveSoundEntry entry, Vec3 cameraPos) {
        Vec3 soundPos = entry.getCurrentPosition();
        float opacity = entry.getOpacity();
        int color = entry.getColor();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        int a = (int) (opacity * 180);

        poseStack.pushPose();

        Vec3 renderPos;
        double minX, minY, minZ, maxX, maxY, maxZ;

        if (entry.hasTrackedBlock()) {
            BlockPos blockPos = entry.getTrackedBlock();
            renderPos = Vec3.atLowerCornerOf(blockPos).subtract(cameraPos);
            minX = 0; minY = 0; minZ = 0;
            maxX = 1; maxY = 1; maxZ = 1;
        } else {
            renderPos = soundPos.subtract(cameraPos);
            double size = entry.hasTrackedEntity() ? 0.4 : 0.25;
            minX = -size; minY = -size; minZ = -size;
            maxX = size; maxY = size; maxZ = size;
        }

        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);

        Matrix4f mat = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Top face
        buf.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).endVertex();

        // Front face
        buf.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).endVertex();

        // Back face
        buf.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).endVertex();

        // Left face
        buf.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).endVertex();

        // Right face
        buf.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).endVertex();

        // Bottom face
        buf.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buf.end());
        poseStack.popPose();
    }

    private static void renderSoundLabel(PoseStack poseStack, MultiBufferSource bufferSource,
                                         ActiveSoundEntry entry, Vec3 cameraPos, Camera camera, float yOffset) {
        Vec3 soundPos = entry.getCurrentPosition();
        float opacity = entry.getOpacity();
        String labelText = entry.getSoundIdString();

        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();

        double labelY = soundPos.y + (entry.hasTrackedBlock() ? 1.5 : 0.6) + yOffset;
        Vec3 labelPos = new Vec3(soundPos.x, labelY, soundPos.z).subtract(cameraPos);

        poseStack.translate(labelPos.x, labelPos.y, labelPos.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix4f = poseStack.last().pose();
        float textX = -font.width(labelText) / 2.0f;

        int alpha = (int) (opacity * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        font.drawInBatch(labelText, textX, 0f, textColor, false, matrix4f, bufferSource, DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        font.drawInBatch(labelText, textX, 0f, textColor, false, matrix4f, bufferSource, DisplayMode.NORMAL, 0, 0xF000F0);

        poseStack.popPose();
    }
}
