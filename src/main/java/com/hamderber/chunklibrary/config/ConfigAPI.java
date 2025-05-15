package com.hamderber.chunklibrary.config;

import java.util.HashMap;
import java.util.Map;

import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ConfigAPI {
	public static final Map<String, IntValue> FEATURE_REGEN_PERIODS = new HashMap<>();
	public static final Map<String, IntValue> AIR_DELTA_ALLOWED = new HashMap<>();
	public static final Map<String, BooleanValue> ORE_DISABLED = new HashMap<>();
	public static final Map<String, BooleanValue> RANDOM_ORE_ENABLED = new HashMap<>();
	public static final Map<String, BooleanValue> RANDOM_TREE_ENABLED = new HashMap<>();
	public static final Map<String, BooleanValue> RANDOM_MOB_ENABLED = new HashMap<>();
	public static final Map<String, DoubleValue> DIMENSION_SCAN_FACTORS = new HashMap<>();
	public static IntValue CHUNK_SCAN_FLAGGING_CHANCE_MODULO;
	public static DoubleValue SKIP_CHUNK_SCAN_BELOW_TPS;
	public static IntValue TICKS_BETWEEN_CHUNK_SCAN_BATCH;
	public static IntValue MAX_CHUNK_SCANS_PER_BATCH;
    
	public static String dumpConfigSettings() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("=== Global Settings ===\n");
	    sb.append("Chunk Scan Flagging Modulo: ").append(getChunkScanFrequency()).append("\n");
	    sb.append("Skip Scan Below TPS: ").append(getSkipSkipChunkScanBelowTPS()).append("\n");
	    sb.append("Ticks Between Chunk Scan Batch: ").append(getTicksBetweenChunkScanBatch()).append("\n");
	    sb.append("Max Chunk Scans Per Batch: ").append(getMaxChunkScansPerBatch()).append("\n\n");

	    sb.append("=== Per-Dimension Settings ===\n");

	    for (String dimension : ConfigAPI.FEATURE_REGEN_PERIODS.keySet()) {
	        sb.append("[").append(dimension).append("]\n");
	        sb.append("  Days Between Regen: ").append(ConfigAPI.FEATURE_REGEN_PERIODS.get(dimension).get()).append("\n");
	        sb.append("  Air Delta Allowed: ").append(ConfigAPI.AIR_DELTA_ALLOWED.get(dimension).get()).append("\n");
	        sb.append("  Percent of Chunk to Scan: ").append(ConfigAPI.DIMENSION_SCAN_FACTORS.get(dimension).get()).append("\n");
	        sb.append("  Ore Disabled: ").append(ConfigAPI.ORE_DISABLED.get(dimension).get()).append("\n");
	        sb.append("  Random Ore Enabled: ").append(ConfigAPI.RANDOM_ORE_ENABLED.get(dimension).get()).append("\n");
	        sb.append("  Random Tree Enabled: ").append(ConfigAPI.RANDOM_TREE_ENABLED.get(dimension).get()).append("\n");
	        sb.append("  Random Mob Enabled: ").append(ConfigAPI.RANDOM_MOB_ENABLED.get(dimension).get()).append("\n");
	        sb.append("\n");
	    }

	    return sb.toString();
	}
	
	public static double getDimensionScanFactor(ServerLevel level) {
		return getDimensionScanFactor(LevelHelper.getDimensionID(level));
	}
	
	public static double getDimensionScanFactor(String dimensionID) {
		DoubleValue value = DIMENSION_SCAN_FACTORS.get(dimensionID);
		return value != null ? value.get() : 0.35;
	}
	
	public static int getMaxChunkScansPerBatch() {
		return MAX_CHUNK_SCANS_PER_BATCH != null ? MAX_CHUNK_SCANS_PER_BATCH.get() : 3;
	}
	
	public static int getTicksBetweenChunkScanBatch() {
		return TICKS_BETWEEN_CHUNK_SCAN_BATCH != null ? TICKS_BETWEEN_CHUNK_SCAN_BATCH.get() : 100;
	}
	
	public static int getRegenPeriod(ServerLevel level) {
		return getRegenPeriod(LevelHelper.getDimensionID(level));
	}
	
	public static int getRegenPeriod(String dimensionID) {
		IntValue value = FEATURE_REGEN_PERIODS.get(dimensionID);
		return value != null ? value.get() : -1;
	}
	
	public static int getAirDeltaThreshold(ServerLevel level) {
		return getAirDeltaThreshold(LevelHelper.getDimensionID(level));
	}
	
	public static int getAirDeltaThreshold(String dimensionID) {
    	IntValue value = AIR_DELTA_ALLOWED.get(dimensionID);
    	return value != null ? value.get() : -1;
	}
	
    public static boolean isOreDisabled(ServerLevel level) {
    	return isOreDisabled(LevelHelper.getDimensionID(level));
    }
    
    public static boolean isOreDisabled(String dimensionID) {
    	BooleanValue value = ORE_DISABLED.get(dimensionID);
    	return value != null ? value.get() : false;
    }
    
	public static boolean isRandomOreEnabled(ServerLevel level) {
		return isRandomOreEnabled(LevelHelper.getDimensionID(level));
    }

	public static boolean isRandomOreEnabled(String dimensionID) {
    	BooleanValue value = RANDOM_ORE_ENABLED.get(dimensionID);
    	return value != null ? value.get() : false;
    }
	
    public static boolean isRandomTreeEnabled(ServerLevel level) {
    	return isRandomTreeEnabled(LevelHelper.getDimensionID(level));
    }
    
    public static boolean isRandomTreeEnabled(String dimensionID) {
    	BooleanValue value = RANDOM_TREE_ENABLED.get(dimensionID);
    	return value != null ? value.get() : false;
    }
    
    public static boolean isRandomPassiveMobEnabled(ServerLevel level) {
    	return isRandomPassiveMobEnabled(LevelHelper.getDimensionID(level));
    }
    
    public static boolean isRandomPassiveMobEnabled(String dimensionID) {
    	BooleanValue value = RANDOM_MOB_ENABLED.get(dimensionID);
    	return value != null ? value.get() : false;
    }
    
    public static int getChunkScanFrequency() {
    	return CHUNK_SCAN_FLAGGING_CHANCE_MODULO != null ? CHUNK_SCAN_FLAGGING_CHANCE_MODULO.get() : 7;
    }

	public static double getSkipSkipChunkScanBelowTPS() {
		return SKIP_CHUNK_SCAN_BELOW_TPS != null ? SKIP_CHUNK_SCAN_BELOW_TPS.get() : 15.0;
	}
}
