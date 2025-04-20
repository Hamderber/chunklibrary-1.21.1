package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.data.WorldRegenData;
import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class ChunkRegenerator {
	public static void regenerateChunk(ServerLevel level, ChunkPos pos) {
        WorldRegenData.get().addChunk(level, pos);
    }

    public static void regenerateChunk(String dimensionID, int x, int z) {
        ServerLevel level = LevelHelper.getServerLevel(dimensionID);
        if (level == null) {
            ChunkLibrary.LOGGER.warn("Failed to find ServerLevel for dimension ID: " + dimensionID);
            return;
        }
        
        regenerateChunk(level, new ChunkPos(x, z));
    }

    public static boolean isMarked(ServerLevel level, ChunkPos pos) {
        return WorldRegenData.get().isMarked(level, pos);
    }
}
