package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class ChunkHandler {
	@SubscribeEvent
	public void onLoadChunk(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		
		long currentDay = TimeHelper.getTrueOverworldDay();
		
		ChunkData data = ChunkData.get(level);
		ChunkPos pos = event.getChunk().getPos();
		
		if (event.isNewChunk()) {
				data.setLastGeneratedDay(level, pos, currentDay);
				data.incrementTimesGenerated(level, pos);
		}
	}
}
