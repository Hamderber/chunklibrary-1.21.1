package com.hamderber.chunklibrary.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.util.LevelHelper;
import com.hamderber.chunklibrary.util.SeedUtil;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

@Mixin(TreeFeature.class)
public class TreeFeatureMixin {
	@Inject(method = "doPlace", at = @At("HEAD"))
	private void injectNewSeed(WorldGenLevel level, RandomSource random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootBlockSetter, BiConsumer<BlockPos, BlockState> trunkBlockSetter, FoliagePlacer.FoliageSetter foliageBlockSetter, TreeConfiguration config, CallbackInfoReturnable<Boolean> cir) {
		ServerLevel serverLevel = level.getLevel();	
		
		if (ConfigAPI.isRandomTreeEnabled(serverLevel) && !ChunkData.get(serverLevel).isFirstTimeGeneration(serverLevel, LevelHelper.chunkPosFromBlockPos(pos))) {
			random.setSeed(SeedUtil.getFeatureSeed(TimeHelper.getWorldAge(), pos, "minecraft:tree"));
		}
	}
}
