package com.hamderber.chunklibrary.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.util.SeedUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
	@Redirect(
	    method = "applyBiomeDecoration",
	    at = @At(
	        value = "INVOKE",
	        target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"
	    )
	)
	private boolean redirectFeaturePlacement(PlacedFeature instance, WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		if (instance.feature().value().feature() instanceof OreFeature) {
		    long newSeed = SeedUtil.getFeatureGenSeed(level.getLevel());
		    random.setSeed(newSeed);
		    ChunkLibrary.LOGGER.debug("Placing feature: minecraft:ore " + newSeed);
		}
		
	    return instance.placeWithBiomeCheck(level, generator, random, origin);
	}
}
