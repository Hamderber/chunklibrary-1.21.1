package com.hamderber.chunklibrary.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.util.SeedUtil;
import com.hamderber.chunklibrary.util.TimeHelper;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@Inject(method = "spawnMobsForChunkGeneration", at = @At("HEAD")) // This causes WHAT mob that spawns to be random, NOT the decision to spawn it.
    private static void spawnRandomMobsForChunkGeneration(ServerLevelAccessor levelAccessor, Holder<Biome> biome, ChunkPos chunkPos, RandomSource random, CallbackInfo ci) {
        ServerLevel level = levelAccessor.getLevel();
        
        if (ConfigAPI.isRandomPassiveMobEnabled(level) && !ChunkData.get(level).isFirstTimeGeneration(level, chunkPos)) {
            long seed = SeedUtil.getFeatureSeed(TimeHelper.getWorldAge(), new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()), "minecraft:mobspawn");
            random.setSeed(seed);
        }
    }
}