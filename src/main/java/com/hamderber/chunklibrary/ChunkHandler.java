package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.data.ChunkAgeData;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class ChunkHandler {
	@SubscribeEvent
	public void onLoadChunk(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		
		long currentDay = TimeHelper.getCurrentDay(level);
		
//		StringBuilder message = new StringBuilder("Chunk loaded at " + event.getChunk().getPos().toString() +
//				" on day " + currentDay);
		
		ChunkAgeData data = ChunkAgeData.get(level);
		ChunkPos pos = event.getChunk().getPos();
		
		if (event.isNewChunk() || currentDay <= 1) {
//			 message.append(" and it is a new chunk.");
			data.setLastGeneratedDay(level, pos, currentDay);
		}
		
//		 ChunkLibrary.LOGGER.debug(message.toString());
	}
}
