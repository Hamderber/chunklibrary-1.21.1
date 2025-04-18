package com.hamderber.chunklibrary;


import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class SuffocationFixer {
	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		// This fires before the ChunkLibrary.EndLoad event, so the regen list will still have chunks that HAVE been regened in it at this slice of time
		if (!(event.getLevel() instanceof ServerLevel level)) return;
	    if (!(event.getChunk() instanceof LevelChunk chunk)) return;

	    ChunkPos chunkPos = chunk.getPos();
	    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	    Pair<String, Long> pair = Pair.of(LevelHelper.getDimensionID(level), ChunkPos.asLong(chunkPos.x, chunkPos.z));
    	
    	if (ChunkRegenerator.regenList.contains(pair)) {
		    // Manually create bounding box
		    int minX = chunkPos.getMinBlockX();
		    int minZ = chunkPos.getMinBlockZ();
		    int maxX = chunkPos.getMaxBlockX() + 1;
		    int maxZ = chunkPos.getMaxBlockZ() + 1;
	
		    int minY = level.getMinBuildHeight();
		    int maxY = level.getMaxBuildHeight();
	
		    AABB chunkBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		    List<Entity> entities = level.getEntities(null, chunkBox);
	
		    for (Entity entity : entities) {
		        BlockPos pos = entity.blockPosition();
		        BlockState state = level.getBlockState(pos);
	
		        if (state.isSuffocating(level, pos)) {
		            // Search upward for safe air space
		            int safeY = pos.getY();
	
		            for (int y = pos.getY(); y < maxY; y++) {
		                mutablePos.set(pos.getX(), y, pos.getZ());
		                if (!level.getBlockState(mutablePos).isSuffocating(level, mutablePos)) {
		                    safeY = y;
		                    break;
		                }
		            }
	
		            entity.setPos(entity.getX(), safeY + 0.5, entity.getZ());
		            entity.setDeltaMovement(Vec3.ZERO);
		            entity.fallDistance = 0.0F;
	
		            ChunkLibrary.LOGGER.debug("Teleported {} to Y={} to prevent suffocation in chunk {}",
		                    entity.getName().getString(), safeY, chunkPos);
		        }
		    }
    	}
	}
}
