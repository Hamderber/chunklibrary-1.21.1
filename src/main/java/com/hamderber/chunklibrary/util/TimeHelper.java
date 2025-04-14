package com.hamderber.chunklibrary.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class TimeHelper {
	public static long getCurrentDay(ServerLevel level) {
		if (level == null) {
			return 0;
		}
		else {
			return level.getServer().getLevel(Level.OVERWORLD).getDayTime() / Level.TICKS_PER_DAY;
		}
	}
}
