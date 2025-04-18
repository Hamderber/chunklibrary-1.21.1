package com.hamderber.chunklibrary.util;

import java.util.Random;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.config.ConfigAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class LevelHelper {
	public static String getDimensionID(Level level) {
		return level.dimension().location().toString();
	}
	
	public static ServerLevel getServerLevel(String dimensionID) {
	    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
	    if (server == null) return null;

	    for (ServerLevel level : server.getAllLevels()) {
	        if (level.dimension().location().toString().equals(dimensionID)) {
	            return level;
	        }
	    }

	    return null;
	}
	
	public static ResourceKey<Level> getDimensionKey(String dimensionID) {
	    ResourceLocation location = ResourceLocation.tryParse(dimensionID);
	    if (location == null) return null;

	    return ResourceKey.create(Registries.DIMENSION, location);
	}
	
	public static ChunkPos chunkPosFromBlockPos(BlockPos pos) {
		return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
	}
	
	public static ServerLevel getOverworld() {
		return ServerLifecycleHooks.getCurrentServer().overworld();
	}
	
	public static int sampleAirBlocksSafe(ServerLevel level, ChunkAccess chunk, boolean ignoreTPS) {
		if (!ignoreTPS && TimeHelper.getAverageTPS() < ConfigAPI.SKIP_CHUNK_SCAN_BELOW_TPS.get()) return -1;

		int airSampleCount = 0;
		ChunkPos chunkPos = chunk.getPos();
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		ChunkLibrary.LOGGER.debug("Safe sample at " + chunkPos.toString());
		
		Random random = new Random(ChunkPos.asLong(chunkPos.x, chunkPos.z));

		int minY = level.getMinBuildHeight();
		int maxY = level.getMaxBuildHeight();
		int totalHeight = maxY - minY;

		// do a fullscan at sea level, thats where most activity is
		final int seaLevel = level.getSeaLevel();
		final int seaBandStart = Math.max(seaLevel - 4, minY);
		final int seaBandEnd = Math.min(seaLevel + 4, maxY - 1);

		for (int y = seaBandStart; y <= seaBandEnd; y++) {
			for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
				for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
					mutablePos.set(x, y, z);
					if (chunk.getBlockState(mutablePos).isAir()) airSampleCount++;
				}
			}
		}
		
		// [startY %, endY %, sampleWeight, boxRadius]
		// boxRadius: 1 = 3x3x3, 0 = center only, 2 = 5x5x5, etc.
		final float[][] regions = {
			{0.85f, 1.0f, 1.2f, 2f},   // surface (aggressive scan, large boxes)
			{0.65f, 0.85f, 1.0f, 1.5f},// upper
			{0.35f, 0.65f, 0.9f, 1f},  // mid (deepslate)
			{0.10f, 0.35f, 0.7f, 1f},  // lower
			{0.0f, 0.10f, 0.5f, 0f}    // bedrock (center only)
		};

		final int baseSamples = 64; // scale reference

		for (float[] region : regions) {
			int yStart = minY + Math.round(region[0] * totalHeight);
			int yEnd = minY + Math.round(region[1] * totalHeight);
			int regionHeight = Math.max(1, yEnd - yStart);

			int sampleCount = Math.round(baseSamples * region[2] * (regionHeight / 64.0f));
			int boxRadius = Math.round(region[3]);

			for (int i = 0; i < sampleCount; i++) {
				int x = chunkPos.getMinBlockX() + random.nextInt(16);
				int z = chunkPos.getMinBlockZ() + random.nextInt(16);
				int y = yStart + random.nextInt(regionHeight);

				for (int dx = -boxRadius; dx <= boxRadius; dx++) {
					for (int dy = -boxRadius; dy <= boxRadius; dy++) {
						for (int dz = -boxRadius; dz <= boxRadius; dz++) {
							int cx = Mth.clamp(x + dx, chunkPos.getMinBlockX(), chunkPos.getMaxBlockX());
							int cy = Mth.clamp(y + dy, minY, maxY - 1);
							int cz = Mth.clamp(z + dz, chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ());

							mutablePos.set(cx, cy, cz);
							if (chunk.getBlockState(mutablePos).isAir()) airSampleCount++;
						}
					}
				}
			}
		}

		return airSampleCount;
	}
	
	public static int sampleAirBlocksUnsafe (ServerLevel level, ChunkPos chunkPos, boolean ignoreTPS) {
	    ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z); // calling getchunk during generation will cause problems
	    
	    return sampleAirBlocksSafe(level, chunk, ignoreTPS);
	}
}
