package com.leclowndu93150.synesthesia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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

        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 cameraPos = camera.position();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        Map<Long, List<ActiveSoundEntry>> groupedSounds = groupSoundsByPosition(sounds);

        for (ActiveSoundEntry entry : sounds) {
            renderSoundHighlight(poseStack, bufferSource, entry, cameraPos, partialTick);
        }

        for (Map.Entry<Long, List<ActiveSoundEntry>> group : groupedSounds.entrySet()) {
            List<ActiveSoundEntry> entries = group.getValue();
            for (int i = 0; i < entries.size(); i++) {
                float yOffset = i * LABEL_OFFSET;
                renderSoundLabel(poseStack, bufferSource, entries.get(i), cameraPos, camera, yOffset, partialTick);
            }
        }

        bufferSource.endBatch();
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

    private static void renderSoundHighlight(PoseStack poseStack, MultiBufferSource bufferSource, ActiveSoundEntry entry, Vec3 cameraPos, float partialTick) {
        Entity trackedEntity = entry.getTrackedEntity();
        if (trackedEntity instanceof LivingEntity living && tryRenderEntityModel(poseStack, bufferSource, living, cameraPos, entry, partialTick)) {
            return;
        }

        Vec3 soundPos = entry.getCurrentPosition(partialTick);
        int color = entry.getColor();
        float opacity = entry.getOpacity();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
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
        VertexConsumer buf = bufferSource.getBuffer(RenderTypes.debugQuads());
        emitCube(buf, mat, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, r, g, b, a);

        poseStack.popPose();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean tryRenderEntityModel(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity, Vec3 cameraPos, ActiveSoundEntry entry, float partialTick) {
        EntityRenderDispatcher dispatch = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?, ?> renderer = dispatch.getRenderer(entity);
        if (!(renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer)) {
            return false;
        }
        EntityModel<?> model = livingRenderer.getModel();
        ModelPart rootPart = model.root();

        EntityRenderState state;
        try {
            state = dispatch.extractEntity(entity, partialTick);
        } catch (Exception ignored) {
            return false;
        }
        try {
            ((Model) model).setupAnim(state);
        } catch (Exception ignored) {
        }

        int color = entry.getColor();
        float opacity = entry.getOpacity();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (opacity * 180);

        Vec3 entityPos = entity.getPosition(partialTick);
        Vec3 renderPos = entityPos.subtract(cameraPos);

        poseStack.pushPose();
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
        float bodyRot = state instanceof LivingEntityRenderState livingState
                ? livingState.bodyRot
                : Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(180f - bodyRot)));
        poseStack.scale(-1f, -1f, 1f);
        poseStack.translate(0f, EntityModel.MODEL_Y_OFFSET, 0f);

        VertexConsumer buf = bufferSource.getBuffer(RenderTypes.debugQuads());
        rootPart.visit(poseStack, (pose, name, idx, cube) -> {
            Matrix4f mat = pose.pose();
            float cMinX = cube.minX / 16.0f;
            float cMinY = cube.minY / 16.0f;
            float cMinZ = cube.minZ / 16.0f;
            float cMaxX = cube.maxX / 16.0f;
            float cMaxY = cube.maxY / 16.0f;
            float cMaxZ = cube.maxZ / 16.0f;
            emitCube(buf, mat, cMinX, cMinY, cMinZ, cMaxX, cMaxY, cMaxZ, r, g, b, a);
        });

        poseStack.popPose();
        return true;
    }

    private static void emitCube(VertexConsumer buf, org.joml.Matrix4f mat, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int r, int g, int b, int a) {
        float inflate = 0.005f;
        minX -= inflate; minY -= inflate; minZ -= inflate;
        maxX += inflate; maxY += inflate; maxZ += inflate;
        buf.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);

        buf.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);

        buf.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);

        buf.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);

        buf.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);

        buf.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
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
        poseStack.mulPose(camera.rotation());
        poseStack.scale(0.025f, -0.025f, 0.025f);

        int alpha = (int) (opacity * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        float textWidth = font.width(labelText);

        font.drawInBatch(
                labelText,
                -textWidth / 2.0f,
                0f,
                textColor,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                0xF000F0
        );

        poseStack.popPose();
    }

}
