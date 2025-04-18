package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.config.ConfigAPI;
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
		
		long currentDay = TimeHelper.getWorldAge();
		
		ChunkData data = ChunkData.get(level);
		ChunkPos pos = event.getChunk().getPos();
		
		if (event.isNewChunk()) {
				data.setLastGeneratedDay(level, pos, currentDay);
				data.incrementTimesGenerated(level, pos);
		}
		else if (data.getInitialAirEstimate(level, pos) <= 0 || data.getCurrentAirEstimate(level, pos) <= 0 ) {//|| Math.abs(Long.hashCode(pos.toLong())) % 1/*ConfigAPI.getChunkScanFrequency()*/ == 0) { // reduce scan frequency (performance)
			int regenPeriod = ConfigAPI.getRegenPeriod(level);
			if (regenPeriod == -1) return;
			
			final double PERCENT_OF_AGE_TO_SCAN = 0.9;// only scan when a chunk is close to being old enough to regen to save on performance
			
			if (data.getChunkAge(level, pos) >= (int)(regenPeriod * PERCENT_OF_AGE_TO_SCAN) || true) {//bypass check for testing
				ChunkScanner.queueChunkForScan(level, pos);
			}
		}
	}
}
