package com.hamderber.chunklibrary.util;

import com.hamderber.chunklibrary.config.ExternalConfig;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class SeedUtil {
	public static long seedForDay(long day) {
        long baseSeed = getOverworldSeed();
        
        return day < 2 ? baseSeed ^ day : baseSeed; //xor the base seed by day
    }
	
	public static long getOverworldSeed() {
		return ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD).getSeed();
	}
	
	public static long hash(long left, long right) {
		return HashCommon.mix(left ^ right);
	}
	
	public static long getFeatureGenSeed(ServerLevel level) {
		long baseSeed = getOverworldSeed();
		
		if (ExternalConfig.FEATURE_REGEN_PERIODS.isEmpty()) {
			return baseSeed;
		}
		
		IntValue regenPeriodicityIV = ExternalConfig.FEATURE_REGEN_PERIODS.get(LevelHelper.getDimensionID(level));
		int regenPeriodicity = regenPeriodicityIV != null ? regenPeriodicityIV.get() : 0;
		
		// Because regenPeriodicity is an int, rounding contributes to keeping the same seed for multiple days
		return regenPeriodicity == 0 ? baseSeed : hash(baseSeed, TimeHelper.getOverworldDay() / regenPeriodicity);
	}
}
