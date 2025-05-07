package com.hamderber.chunklibrary.util;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.data.TimeTrackerData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class TimeHelper {
	private static final int TIME_DESYNC_THESHOLD = 2;
	private static boolean DID_WARN_TIME_DESYNC = false;
	
	@SubscribeEvent
    public void onLevelExit(LevelEvent.Unload event) {
		// Skip if client side. This should be server-side only.
		if (event.getLevel().isClientSide()) return;
		
		// allow for warning of time desyncs whever a world is unloaded. this is lazier than counting ticks or something else
		DID_WARN_TIME_DESYNC = false;
		
    	TimeTrackerData.get().setDirty();
    }
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		long realDay = TimeTrackerData.get().getTotalDays();
		long fakeDay = getGameOverworldDay();
		
		if (!DID_WARN_TIME_DESYNC && Math.abs(realDay - fakeDay) > TIME_DESYNC_THESHOLD) {
			// Warn of time desync, but not to a point where the log is flooded with warnings.
			DID_WARN_TIME_DESYNC = true;
			ChunkLibrary.LOGGER.warn("Time desync detected: trackedDay={}, gameDay={}! Was /time used?", realDay, fakeDay);
		}
		
		TimeTrackerData.get().tick(1);
	}
	
	public static long getCurrentDay(ServerLevel level) {
		return level != null ? level.getServer().getLevel(Level.OVERWORLD).getDayTime() / Level.TICKS_PER_DAY : 0;
	}
	
	public static long getGameOverworldDay() {
		// mod interactions or things like /time set day can cause the game day to reset to 0 or be off
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		
		if (currentServer == null) {
			ChunkLibrary.LOGGER.warn("Server not found! Unable to get the game's Overworld day.");
			return 0;
		}
		else {
			return getCurrentDay(currentServer.getLevel(Level.OVERWORLD));
		}
	}
	
	public static long getWorldAge() {
		// tracking the day by counting ticks mitigates issues with time dilation
		return TimeTrackerData.get().getTotalDays();
	}
	
	public static double getAverageTPS() {
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		
		if (currentServer == null) {
			ChunkLibrary.LOGGER.warn("Server not found! Unable to get the server's average TPS.");
			return 0;
		}
		else {
			double tickTime = currentServer.getAverageTickTimeNanos();
		    return 1_000_000_000.0 / tickTime;
		}
	}
}
