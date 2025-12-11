package com.leclowndu93150.extreme_sound_visualizer.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoundVisualizerData {
    private static final List<ActiveSoundEntry> activeSounds = Collections.synchronizedList(new ArrayList<>());

    public static void addSound(ResourceLocation soundId, double x, double y, double z,
                                @Nullable Entity entity, @Nullable BlockPos block) {
        activeSounds.add(new ActiveSoundEntry(soundId, x, y, z, entity, block));
    }

    public static List<ActiveSoundEntry> getActiveSounds() {
        return new ArrayList<>(activeSounds);
    }

    public static void tick() {
        activeSounds.removeIf(ActiveSoundEntry::isExpired);
    }

    public static void clear() {
        activeSounds.clear();
    }

    public static int getActiveCount() {
        return activeSounds.size();
    }
}
