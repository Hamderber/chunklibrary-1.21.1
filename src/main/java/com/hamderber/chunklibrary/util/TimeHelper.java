package com.hamderber.chunklibrary.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class TimeHelper {
	public static long getCurrentDay(ServerLevel level) {
		if (level == null) {
			return 0;
		}
		else {
			return level.getServer().getLevel(Level.OVERWORLD).getDayTime() / Level.TICKS_PER_DAY;
		}
	}
	
	public static long getOverworldDay() {
		return getCurrentDay(ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD));
	}
}
