package com.hamderber.chunklibrary.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.hamderber.chunklibrary.ChunkScanner;
import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.util.LevelHelper;
import com.hamderber.chunklibrary.util.SeedUtil;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
	@Redirect(
	    method = "applyBiomeDecoration",
	    at = @At(
	        value = "INVOKE",
	        target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"
	    ) // Redirect instead of inject because I want to preserve the game's randomness for everything that relies on the world seed.
	)     // By injecting here, the world determinism of the original seed is kept outside of ores.
	private boolean redirectFeaturePlacement(PlacedFeature instance, WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		if (instance.feature().value().feature() instanceof OreFeature) {
			ServerLevel serverLevel = level.getLevel();
			
			boolean isFirstLoad = ChunkData.get(serverLevel).isFirstTimeGeneration(serverLevel, LevelHelper.chunkPosFromBlockPos(origin));
			
			if (ConfigAPI.isOreDisabled(serverLevel) && !isFirstLoad) return false; // sending false makes minecraft move to the next generation step
			
			if (ConfigAPI.isRandomOreEnabled(serverLevel) && !isFirstLoad) {
				random.setSeed(SeedUtil.getFeatureSeed(TimeHelper.getWorldAge(), origin, "minecraft:ore"));
			}
		}
		
	    return instance.placeWithBiomeCheck(level, generator, random, origin);
	}
	
	@Inject(method = "applyBiomeDecoration", at = @At("TAIL")) // using ChunkAccess makes block scanning safe
	private void afterBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {
		// This is called following generation of a chunk for the first time, so this is where I want to schedule an
		// immediate chunk scan.
		ChunkScanner.queueChunkForScanImmediate(level.getLevel(), chunk.getPos());
	}
}
