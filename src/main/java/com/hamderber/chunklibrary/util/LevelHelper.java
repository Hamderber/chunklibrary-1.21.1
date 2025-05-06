package com.hamderber.chunklibrary.util;

import com.hamderber.chunklibrary.config.ConfigAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		
		return currentServer == null ? null : currentServer.overworld();
	}
	
	public static int sampleAirBlocksSafe(ServerLevel level, ChunkAccess chunk, boolean ignoreTPS) {
		return fullAirBlockScan(level, chunk);
	}
	
	public static int sampleAirBlocksUnsafe (ServerLevel level, ChunkPos chunkPos, boolean ignoreTPS) {
	    ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z); // calling getchunk during generation will cause problems
	    // this has the same behavior but this is unsafe so it is intentionally called when there wont be a chunk access problem
	    return sampleAirBlocksSafe(level, chunk, ignoreTPS);
	}
	
	public static int fullAirBlockScan(ServerLevel level, ChunkAccess chunk) {
		/* WARNING:
		 * This method is deceptively efficient, despite scanning all the blocks. Adding logic to scan fewer blocks in a smart way
		 * is actually more expensive than just sending the scan.
		 * 
		 * HOURS WASTED TRYING TO OPTIMIZE THIS METHOD:
		 * 17 (4/18/25)
		 */
	    long startTime = System.nanoTime();

//	    ChunkPos chunkPos = chunk.getPos();
//	    String dimId = LevelHelper.getDimensionID(level);

	    double scanFactor = ConfigAPI.getDimensionScanFactor(level);
	    int minY = chunk.getMinBuildHeight();
	    int maxY = chunk.getMaxBuildHeight();
	    int fullHeight = maxY - minY;
	    int scanHeight = (int) Math.round(fullHeight * scanFactor);

	    int airCount = 0;
	    MutableBlockPos pos = new MutableBlockPos();

	    for (int x = 0; x < 16; x++) {
	        for (int z = 0; z < 16; z++) {
	            for (int y = 0; y < scanHeight; y++) {
	                pos.set(x, minY + y, z);
	                if (chunk.getBlockState(pos).isAir()) {
	                    airCount++;
	                }
	            }
	        }
	    }

	    long endTime = System.nanoTime();
	    long durationMicros = (endTime - startTime) / 1000;

//	    ChunkLibrary.LOGGER.debug("AirScan [{} | {}]: dim='{}', scanned Y=({}, {}), Air={}, Time={}µs",
//	            chunkPos.x, chunkPos.z,
//	            dimId,
//	            minY, minY + scanHeight,
//	            airCount,
//	            durationMicros
//	    );

	    return airCount;
	}
}
