package com.oidioomstudio.olib.util.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Common particle and sound playback utilities.
 */
public final class OEffectHelper {
    private OEffectHelper() {}

    public static void playSoundAt(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (level != null && sound != null && pos != null) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, source, volume, pitch);
        }
    }

    public static void spawnParticlesAt(Level level, ParticleOptions particle, BlockPos pos, int count, double spread) {
        if (level instanceof ServerLevel serverLevel && particle != null && pos != null) {
            serverLevel.sendParticles(particle,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                count, spread, spread, spread, 0.05);
        }
    }
}
