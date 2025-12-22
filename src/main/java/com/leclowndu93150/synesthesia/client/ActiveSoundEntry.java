package com.leclowndu93150.synesthesia.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.ref.WeakReference;

public class ActiveSoundEntry {
    private static final long FADE_DURATION_MS = 3000;

    private final ResourceLocation soundId;
    private final long startTimeMs;
    private final int color;
    private final double x, y, z;
    @Nullable
    private final WeakReference<Entity> trackedEntity;
    @Nullable
    private final BlockPos trackedBlock;

    public ActiveSoundEntry(ResourceLocation soundId, double x, double y, double z,
                            @Nullable Entity entity, @Nullable BlockPos block) {
        this.soundId = soundId;
        this.startTimeMs = System.currentTimeMillis();
        this.color = computeColorFromLocation(soundId);
        this.x = x;
        this.y = y;
        this.z = z;
        this.trackedEntity = entity != null ? new WeakReference<>(entity) : null;
        this.trackedBlock = block;
    }

    public Vec3 getCurrentPosition() {
        if (trackedEntity != null) {
            Entity entity = trackedEntity.get();
            if (entity != null && entity.isAlive()) {
                return entity.getEyePosition();
            }
        }
        if (trackedBlock != null) {
            return Vec3.atCenterOf(trackedBlock);
        }
        return new Vec3(x, y, z);
    }

    public float getEntityHeight() {
        if (trackedEntity != null) {
            Entity entity = trackedEntity.get();
            if (entity != null && entity.isAlive()) {
                return entity.getBbHeight();
            }
        }
        return 0f;
    }

    public float getOpacity() {
        long age = System.currentTimeMillis() - startTimeMs;
        if (age >= FADE_DURATION_MS) {
            return 0.0f;
        }
        return 1.0f - (float) age / FADE_DURATION_MS;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTimeMs >= FADE_DURATION_MS;
    }

    public int getColor() {
        return color;
    }

    public int getColorWithOpacity() {
        float opacity = getOpacity();
        int alpha = (int) (opacity * 255);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public String getSoundIdString() {
        return soundId.toString();
    }

    public ResourceLocation getSoundId() {
        return soundId;
    }

    public boolean hasTrackedEntity() {
        return trackedEntity != null && trackedEntity.get() != null && trackedEntity.get().isAlive();
    }

    public boolean hasTrackedBlock() {
        return trackedBlock != null;
    }

    @Nullable
    public BlockPos getTrackedBlock() {
        return trackedBlock;
    }

    public static int computeColorFromLocation(ResourceLocation loc) {
        int hash = loc.toString().hashCode();
        float hue = (hash & 0xFF) / 255.0f;
        float saturation = 0.7f + ((hash >> 8) & 0x3F) / 255.0f * 0.3f;
        float brightness = 0.8f + ((hash >> 14) & 0x3F) / 255.0f * 0.2f;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }
}
