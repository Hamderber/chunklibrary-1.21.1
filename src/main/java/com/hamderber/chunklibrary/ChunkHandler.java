package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.data.WorldRegenData;
import com.hamderber.chunklibrary.enums.AirEstimate;
import com.hamderber.chunklibrary.enums.RegenPeriod;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class ChunkHandler {
	@SubscribeEvent
	public void onLoadChunk(ChunkEvent.Load event) {
		// don't crash the client. Only care about this server-side
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		
		long currentDay = TimeHelper.getWorldAge();
		
		ChunkData data = ChunkData.get(level);
		ChunkPos pos = event.getChunk().getPos();

		int initialAirEstimate = data.getInitialAirEstimate(level, pos);
		int currentAirEstimate = data.getCurrentAirEstimate(level, pos);

		if (event.isNewChunk() || initialAirEstimate == AirEstimate.DEFAULT.getValue() ||
			currentAirEstimate == AirEstimate.DEFAULT.getValue()) {
				data.setLastGeneratedDay(level, pos, currentDay);
				data.incrementTimesGenerated(level, pos);
				ChunkScanner.queueChunkForScanImmediate(level, pos);
		}
		// reduce scan frequency (performance)
		else if (Math.abs(Long.hashCode(pos.toLong())) % ConfigAPI.getChunkScanFrequency() == 0) {
			int regenPeriod = ConfigAPI.getRegenPeriod(level);
			if (regenPeriod == RegenPeriod.DISABLED.getValue()) return;

			// only scan when a chunk is close to being old enough to regen to save on performance
			final double PERCENT_OF_AGE_TO_SCAN = 0.9;
			
			if (data.getChunkAge(level, pos) >= (int)(regenPeriod * PERCENT_OF_AGE_TO_SCAN)) {
				ChunkScanner.queueChunkForScan(level, pos);
			}
		}
		
		// fires after chunk has reloaded
		try {
			WorldRegenData worldRegenData = WorldRegenData.get();
			if (worldRegenData.isMarked(level, pos)) {
	        	WorldRegenData.get().clearChunk(level, pos);
	    	}
		}
		catch (IllegalStateException ex) {
			// No-op if getting chunk data failed.
		}
	}
}
