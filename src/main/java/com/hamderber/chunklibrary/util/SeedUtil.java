package com.hamderber.chunklibrary.util;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class SeedUtil {
	public static long seedForDay(long day) {
        long baseSeed = getOverworldSeed();
        
        return day < 2 ? baseSeed ^ day : baseSeed; //xor the base seed by day
    }
	
	public static long getFeatureSeed(long day, BlockPos origin, String featureID) {
	    long seed = getOverworldSeed();
	    
	    seed ^= HashCommon.mix(seed ^ day);
	    seed ^= HashCommon.mix(seed ^ origin.asLong());
	    seed ^= HashCommon.mix(seed ^ featureID.hashCode());
	    
	    return HashCommon.mix(seed);
	}
	
    public static long getOverworldSeed() {
        return ServerLifecycleHooks.getCurrentServer().overworld().getSeed();
    }
}
