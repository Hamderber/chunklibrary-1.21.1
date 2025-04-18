package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.util.LevelHelper;
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
		else if (data.getInitialAirEstimate(level, pos) <= 0 || data.getCurrentAirEstimate(level, pos) <= 0) {
			int airSample = LevelHelper.sampleAirBlocksSafe(level, event.getChunk(), true);
			
			if (data.getInitialAirEstimate(level, pos) <= 0) {
				// The initial air estimate may be <= 0 if it is skipped for some reason
				data.setInitialAirEstimate(level, pos, airSample);
				ChunkLibrary.LOGGER.debug("Set initial air estimate: {}", airSample);
			}
			
			data.setCurrentAirEstimate(level, pos, airSample);
			ChunkLibrary.LOGGER.debug("Set current air estimate: {}", airSample);
		}
		else if (Math.abs(Long.hashCode(pos.toLong())) % 1/*ConfigAPI.getChunkScanFrequency()*/ == 0) { // reduce scan frequency (performance)
			int airSample = LevelHelper.sampleAirBlocksSafe(level, event.getChunk(), false);
			
			if (airSample < 0) return; // dont set to -1 if skipped for tps concerns
			
			data.setCurrentAirEstimate(level, pos, airSample);
			ChunkLibrary.LOGGER.debug("Routine scan set current air estimate: {}", airSample);
		}
	}
}
