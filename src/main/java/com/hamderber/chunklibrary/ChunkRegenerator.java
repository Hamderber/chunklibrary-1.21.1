package com.hamderber.chunklibrary;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class ChunkRegenerator {
	public static Set<Pair<String, Long>> regenList = new HashSet<>(64);
	
	public static void regenerateChunk(ServerLevel level, ChunkPos pos) {
		regenList.add(Pair.of(LevelHelper.getDimensionID(level), ChunkPos.asLong(pos.x, pos.z)));
		ChunkLibrary.LOGGER.debug("Chunk at " + pos.toString() + " scheduled to regenerate.");
	}
	
	public static void regenerateChunk(String dimensionID, int x, int z) {
		ServerLevel level = LevelHelper.getServerLevel(dimensionID);
	    if (level == null) {
	        ChunkLibrary.LOGGER.warn("Failed to find ServerLevel for dimension ID: " + dimensionID);
	        return;
	    }
	    
	    ChunkPos chunkPos = new ChunkPos(x, z);

	    regenerateChunk(level, chunkPos);
	}
}
