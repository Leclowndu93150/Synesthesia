package com.leclowndu93150.synesthesia.client;

import com.leclowndu93150.synesthesia.mixin.AgeableListModelAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundRenderer {

    private static final float LABEL_OFFSET = 0.3f;

    public static void render(PoseStack poseStack, Camera camera) {
        if (!VisualizerState.isEnabled()) {
            return;
        }

        SoundVisualizerData.tick();

        List<ActiveSoundEntry> sounds = SoundVisualizerData.getActiveSounds();
        if (sounds.isEmpty()) {
            return;
        }

        float partialTick = Minecraft.getInstance().getFrameTime();
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        boolean renderThroughWalls = ClientConfig.renderThroughWalls();
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
            renderSoundHighlight(poseStack, entry, cameraPos, partialTick);
        }

        for (Map.Entry<Long, List<ActiveSoundEntry>> group : groupedSounds.entrySet()) {
            List<ActiveSoundEntry> entries = group.getValue();
            for (int i = 0; i < entries.size(); i++) {
                float yOffset = i * LABEL_OFFSET;
                renderSoundLabel(poseStack, bufferSource, entries.get(i), cameraPos, camera, yOffset, partialTick);
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

    private static void renderSoundHighlight(PoseStack poseStack, ActiveSoundEntry entry, Vec3 cameraPos, float partialTick) {
        Entity trackedEntity = entry.getTrackedEntity();
        if (trackedEntity instanceof LivingEntity && tryRenderEntityModel(poseStack, (LivingEntity) trackedEntity, cameraPos, entry, partialTick)) {
            return;
        }

        Vec3 soundPos = entry.getCurrentPosition(partialTick);
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
        } else if (trackedEntity != null) {
            AABB box = trackedEntity.getBoundingBox();
            Vec3 entityPos = trackedEntity.getPosition(partialTick);
            Vec3 ticked = trackedEntity.position();
            renderPos = entityPos.subtract(cameraPos);
            minX = box.minX - ticked.x; minY = box.minY - ticked.y; minZ = box.minZ - ticked.z;
            maxX = box.maxX - ticked.x; maxY = box.maxY - ticked.y; maxZ = box.maxZ - ticked.z;
        } else {
            renderPos = soundPos.subtract(cameraPos);
            double size = 0.25;
            minX = -size; minY = -size; minZ = -size;
            maxX = size; maxY = size; maxZ = size;
        }

        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);

        Matrix4f mat = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int ri = (int) (r * 255);
        int gi = (int) (g * 255);
        int bi = (int) (b * 255);

        buf.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(ri, gi, bi, a).endVertex();

        buf.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) minZ).color(ri, gi, bi, a).endVertex();

        buf.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(ri, gi, bi, a).endVertex();

        buf.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(ri, gi, bi, a).endVertex();

        buf.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(ri, gi, bi, a).endVertex();

        buf.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(ri, gi, bi, a).endVertex();
        buf.vertex(mat, (float) minX, (float) minY, (float) minZ).color(ri, gi, bi, a).endVertex();

        buf.end();
        BufferUploader.end(buf);

        poseStack.popPose();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean tryRenderEntityModel(PoseStack poseStack, LivingEntity entity, Vec3 cameraPos, ActiveSoundEntry entry, float partialTick) {
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof LivingEntityRenderer)) {
            return false;
        }
        LivingEntityRenderer<?, ?> livingRenderer = (LivingEntityRenderer<?, ?>) renderer;
        EntityModel<?> model = livingRenderer.getModel();

        List<ModelPart> roots = new ArrayList<>();
        if (model instanceof HierarchicalModel<?>) {
            roots.add(((HierarchicalModel<?>) model).root());
        } else if (model instanceof AgeableListModel<?>) {
            AgeableListModel<?> alm = (AgeableListModel<?>) model;
            AgeableListModelAccessor acc = (AgeableListModelAccessor) alm;
            acc.syn_headParts().forEach(roots::add);
            acc.syn_bodyParts().forEach(roots::add);
        } else {
            return false;
        }

        try {
            ((EntityModel) model).setupAnim(entity, 0f, 0f, entity.tickCount + partialTick, 0f, 0f);
        } catch (Exception ignored) {
        }

        int color = entry.getColor();
        float opacity = entry.getOpacity();
        int ri = (color >> 16) & 0xFF;
        int gi = (color >> 8) & 0xFF;
        int bi = color & 0xFF;
        int ai = (int) (opacity * 180);

        Vec3 entityPos = entity.getPosition(partialTick);
        Vec3 renderPos = entityPos.subtract(cameraPos);

        poseStack.pushPose();
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180f - bodyRot));
        poseStack.scale(-1f, -1f, 1f);
        poseStack.translate(0f, -1.501f, 0f);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (ModelPart root : roots) {
            root.visit(poseStack, (pose, name, idx, cube) -> drawCube(buf, pose.pose(), cube, ri, gi, bi, ai));
        }

        buf.end();
        BufferUploader.end(buf);
        poseStack.popPose();
        return true;
    }

    private static void drawCube(BufferBuilder buf, Matrix4f mat, ModelPart.Cube cube, int r, int g, int b, int a) {
        float minX = cube.minX / 16.0f;
        float minY = cube.minY / 16.0f;
        float minZ = cube.minZ / 16.0f;
        float maxX = cube.maxX / 16.0f;
        float maxY = cube.maxY / 16.0f;
        float maxZ = cube.maxZ / 16.0f;

        buf.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buf.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();

        buf.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buf.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();

        buf.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        buf.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buf.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
    }

    private static void renderSoundLabel(PoseStack poseStack, MultiBufferSource bufferSource,
                                         ActiveSoundEntry entry, Vec3 cameraPos, Camera camera, float yOffset, float partialTick) {
        Vec3 soundPos = entry.getCurrentPosition(partialTick);
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

        font.drawInBatch(labelText, textX, 0f, textColor, false, matrix4f, bufferSource, true, 0, 0xF000F0);
        font.drawInBatch(labelText, textX, 0f, textColor, false, matrix4f, bufferSource, false, 0, 0xF000F0);

        poseStack.popPose();
    }
}
