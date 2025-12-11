package com.leclowndu93150.extreme_sound_visualizer.mixin;

import com.leclowndu93150.extreme_sound_visualizer.client.SoundVisualizerData;
import com.leclowndu93150.extreme_sound_visualizer.client.VisualizerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Unique
    private static final double ESV_MAX_SOUND_DISTANCE = 16.0;

    @Inject(method = "play", at = @At("HEAD"))
    private void esv_onSoundPlay(SoundInstance sound, CallbackInfo ci) {
        if (!VisualizerState.isEnabled()) {
            return;
        }

        if (sound.isRelative()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (sound.getSource() == net.minecraft.sounds.SoundSource.PLAYERS) {
            return;
        }

        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();

        Vec3 soundPos = new Vec3(x, y, z);
        double distance = mc.player.position().distanceTo(soundPos);

        float volume = 1.0f;
        try {
            if (sound.getSound() != null) {
                volume = sound.getSound().getVolume().sample(mc.level.random);
            }
        } catch (Exception ignored) {
        }
        double maxDistance = ESV_MAX_SOUND_DISTANCE * volume;
        if (distance > maxDistance) {
            return;
        }

        Entity trackedEntity = esv_findNearbyEntity(x, y, z);
        BlockPos trackedBlock = null;

        if (trackedEntity == null) {
            trackedBlock = esv_findSoundBlock(x, y, z);
        }

        SoundVisualizerData.addSound(sound.getLocation(), x, y, z, trackedEntity, trackedBlock);
    }

    @Unique
    private Entity esv_findNearbyEntity(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return null;
        }

        Vec3 soundPos = new Vec3(x, y, z);
        AABB searchBox = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);

        List<Entity> nearbyEntities = level.getEntitiesOfClass(
                Entity.class,
                searchBox,
                entity -> !entity.isSpectator()
        );

        if (!nearbyEntities.isEmpty()) {
            return nearbyEntities.stream()
                    .min(Comparator.comparingDouble(e -> e.position().distanceToSqr(soundPos)))
                    .orElse(null);
        }
        return null;
    }

    @Unique
    private BlockPos esv_findSoundBlock(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return null;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = level.getBlockState(pos);

        if (!state.isAir()) {
            return pos;
        }
        return null;
    }
}
