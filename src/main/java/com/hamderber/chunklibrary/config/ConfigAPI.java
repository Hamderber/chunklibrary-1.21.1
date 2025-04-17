package com.hamderber.chunklibrary.config;

import java.util.HashMap;
import java.util.Map;

import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ConfigAPI {
	public static final Map<String, IntValue> FEATURE_REGEN_PERIODS = new HashMap<>();
	public static final Map<String, BooleanValue> ORE_DISABLED = new HashMap<>();
	public static final Map<String, BooleanValue> RANDOM_ORE_ENABLED = new HashMap<>();
	public static final Map<String, BooleanValue> RANDOM_TREE_ENABLED = new HashMap<>();
	public static final Map<String, BooleanValue> RANDOM_MOB_ENABLED = new HashMap<>();
    
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
}
