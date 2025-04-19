package com.hamderber.chunklibrary.data;

import java.util.HashMap;
import java.util.Map;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.util.LevelHelper;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.server.level.ServerLevel;

public class ChunkData extends SavedData {
	private static final String NAME = "chunk_regen_data";
	private static final Map<String, Map<Long, Long>> ageMap = new HashMap<>(); // Key: dimensionID, Value: Map<chunkPackedPos, lastGeneratedDay>
	private static final Map<String, Map<Long, Long>> timesGeneratedMap = new HashMap<>(); // Key: dimensionID, Value: Map<chunkPackedPos, numTimesRegenerated>
	private static final Map<String, Map<Long, Integer>> initialAirEstimateMap = new HashMap<>(); // Key: dimensionID, Value: Map<chunkPackedPos, numAir>
	private static final Map<String, Map<Long, Integer>> currentAirEstimateMap = new HashMap<>(); // Key: dimensionID, Value: Map<chunkPackedPos, numAir>
	
	public ChunkData() {}
	
	public static ChunkData create() {
	    return new ChunkData();
	}
	
	public static ChunkData load(CompoundTag tag, Provider lookupProvider) {
		return new ChunkData(tag);
	}

	public ChunkData(CompoundTag tag) { // load
		ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);
		
		for (Tag t : list) {
		    CompoundTag chunkTag = (CompoundTag) t;
		    
		    String dimensionId = chunkTag.getString("dim");
		    long packedPos = chunkTag.getLong("pos");
		    long day = chunkTag.getLong("day");
		    long timesRegened = chunkTag.getLong("timesRegened");
		    int initialAirEstimate = chunkTag.getInt("initialAirEstimate");
		    int currentAirEstimate = chunkTag.getInt("currentAirEstimate");

		    Map<Long, Long> dimensionChunkMap = ageMap.computeIfAbsent(dimensionId, k -> new HashMap<>());
		    dimensionChunkMap.put(packedPos, day);
		    
		    Map<Long, Long> dimensionRegenChunkMap = timesGeneratedMap.computeIfAbsent(dimensionId, k-> new HashMap<>());
		    dimensionRegenChunkMap.put(packedPos, timesRegened);
		    
		    Map<Long, Integer> dimensionInitialAirMap = initialAirEstimateMap.computeIfAbsent(dimensionId, k-> new HashMap<>());
		    dimensionInitialAirMap.put(packedPos, initialAirEstimate); // int because chunks aint that big for long (memory save)
		    
		    Map<Long, Integer> dimensionCurrentAirMap = currentAirEstimateMap.computeIfAbsent(dimensionId, k-> new HashMap<>());
		    dimensionCurrentAirMap.put(packedPos, currentAirEstimate);
		}
	}

	public static ChunkData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(new SavedData.Factory<ChunkData>(ChunkData::create, ChunkData::load), NAME);
	}
	
	@Override
	public CompoundTag save(CompoundTag tag, Provider provider) {
		ListTag list = new ListTag();
		
		for (Map.Entry<String, Map<Long, Long>> dimensionEntry : ageMap.entrySet()) {
		    String dimensionId = dimensionEntry.getKey();
		    
		    for (Map.Entry<Long, Long> chunkEntry : dimensionEntry.getValue().entrySet()) {
		        CompoundTag chunkTag = new CompoundTag();
		        
		        chunkTag.putString("dim", dimensionId);
		        chunkTag.putLong("pos", chunkEntry.getKey());
		        chunkTag.putLong("day", chunkEntry.getValue());
		        
		        int initialAirEstimate = initialAirEstimateMap.getOrDefault(dimensionId, new HashMap<>()).getOrDefault(chunkEntry.getKey(), -1);
		        chunkTag.putInt("initialAirEstimate", initialAirEstimate);
		        
		        int currentAirEstimate = currentAirEstimateMap.getOrDefault(dimensionId, new HashMap<>()).getOrDefault(chunkEntry.getKey(), -1);
		        chunkTag.putInt("currentAirEstimate", currentAirEstimate);
		        
		        long timesRegened = timesGeneratedMap.getOrDefault(dimensionId, new HashMap<>()).getOrDefault(chunkEntry.getKey(), 0L);
                chunkTag.putLong("timesRegened", timesRegened);
                
		        list.add(chunkTag);
		    }
		}
		
		tag.put("chunks", list);
		
		return tag;
	}

	public void setLastGeneratedDay(ServerLevel level, ChunkPos pos, long day) {
		Map<Long, Long> dimMap = ageMap.computeIfAbsent(LevelHelper.getDimensionID(level), k -> new HashMap<>());
		
		dimMap.put(ChunkPos.asLong(pos.x, pos.z), day);
		
		setDirty();
	}
	
	public void setLastGeneratedDayNow(ServerLevel level, ChunkPos pos) {
		setLastGeneratedDay(level, pos, TimeHelper.getWorldAge());
	}

	public long getLastGeneratedDay(ServerLevel level, ChunkPos pos) {
		Map<Long, Long> dimMap = ageMap.get(LevelHelper.getDimensionID(level));
		
		long defaultDay = TimeHelper.getWorldAge();
		
		if (dimMap == null) return defaultDay;
		
		return dimMap.getOrDefault(ChunkPos.asLong(pos.x, pos.z), defaultDay);
	}
	
    public long getTimesGenerated(ServerLevel level, ChunkPos pos) {
        Map<Long, Long> dimMap = timesGeneratedMap.get(LevelHelper.getDimensionID(level));
        
        if (dimMap == null) return 0;
        
        return dimMap.getOrDefault(ChunkPos.asLong(pos.x, pos.z), 0L);
    }
    
    
    
    public void incrementTimesGenerated(ServerLevel level, ChunkPos pos) {
        String dimensionID = LevelHelper.getDimensionID(level);
        
        Map<Long, Long> dimMap = timesGeneratedMap.computeIfAbsent(dimensionID, k -> new HashMap<>());
        
        long chunkPos = ChunkPos.asLong(pos.x, pos.z);
        
        dimMap.put(chunkPos, dimMap.getOrDefault(chunkPos, 0L) + 1L);
        
        setDirty();
    }

    public boolean isFirstTimeGeneration(ServerLevel level, ChunkPos pos) {
        return getTimesGenerated(level, pos) == 0L;
    }
    
    public long getChunkAge(ServerLevel level, ChunkPos pos) {
    	long currentDay = TimeHelper.getWorldAge();
		long lastGeneratedDay = getLastGeneratedDay(level, pos);
		
    	return currentDay - lastGeneratedDay;
    }
    
    public void setChunkAge(ServerLevel level, ChunkPos pos, long age) {
    	// age is based on time since generated
    	setLastGeneratedDay(level, pos, TimeHelper.getWorldAge() - age);
    }
    
    public void resetChunkData(ServerLevel level) {
    	String dimensionId = LevelHelper.getDimensionID(level);

        ageMap.remove(dimensionId);
        timesGeneratedMap.remove(dimensionId);

        setDirty();
    }
    
    public static void resetAllChunkData() {
        for (ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            ChunkData data = ChunkData.get(level);
            String dimensionId = LevelHelper.getDimensionID(level);
            
            ageMap.remove(dimensionId);
            timesGeneratedMap.remove(dimensionId);
            
            data.setDirty();
        }
    }
    
    public void setInitialAirEstimate(ServerLevel level, ChunkPos pos, int airCount) {
        Map<Long, Integer> dimMap = initialAirEstimateMap.computeIfAbsent(LevelHelper.getDimensionID(level), k -> new HashMap<>());
        
        dimMap.put(ChunkPos.asLong(pos.x, pos.z), airCount);
        
        setDirty();
    }
    
    public void setCurrentAirEstimate(ServerLevel level, ChunkPos pos, int airCount) {
        Map<Long, Integer> dimMap = currentAirEstimateMap.computeIfAbsent(LevelHelper.getDimensionID(level), k -> new HashMap<>());
        
        dimMap.put(ChunkPos.asLong(pos.x, pos.z), airCount);
        
        setDirty();
    }

    public int getInitialAirEstimate(ServerLevel level, ChunkPos pos) {
        Map<Long, Integer> dimMap = initialAirEstimateMap.get(LevelHelper.getDimensionID(level));
        
        if (dimMap == null) return -1; // -1 default
        
        return dimMap.getOrDefault(ChunkPos.asLong(pos.x, pos.z), -1);
    }
    
    public int getCurrentAirEstimate(ServerLevel level, ChunkPos pos) {
        Map<Long, Integer> dimMap = currentAirEstimateMap.get(LevelHelper.getDimensionID(level));
        
        if (dimMap == null) return -1; // -1 default
        
        return dimMap.getOrDefault(ChunkPos.asLong(pos.x, pos.z), -1);
    }
    
    public int getAirDelta(ServerLevel level, ChunkPos pos) {
        int initialAir = getInitialAirEstimate(level, pos);
        int currentAir = getCurrentAirEstimate(level, pos);

        return Math.abs(initialAir - currentAir);
    }
    
    public boolean shouldResetChunk(ServerLevel level, ChunkPos pos, int ageLimit) {
    	int airLossThreshold = ConfigAPI.getAirDeltaThreshold(level);
    	long age = getChunkAge(level, pos);
    	
    	// the issue is related to initialair and currentair; one of them is -1 and causing the fallback to just chunk age consistently
    	
//    	ChunkLibrary.LOGGER.debug("Checking chunk at {}: age={}, threshold={}", pos, age, airLossThreshold);

    	if (airLossThreshold > 1) {
    	    int initialAir = getInitialAirEstimate(level, pos);
//    	    ChunkLibrary.LOGGER.debug("Initial air estimate: {}", initialAir);

//    	    if (initialAir <= 0) {
//    	        ChunkLibrary.LOGGER.debug("Skipping air delta check due to invalid initial air (<= 0). Returning false");
//    	        // if this happens, skip. loading the chunk in teh future will correct this
//    	    	return false;
//    	    }

    	    int currentAir = getCurrentAirEstimate(level, pos);
//    	    ChunkLibrary.LOGGER.debug("Current air estimate: {}", currentAir);
    	    
    	    

    	    if (initialAir < 0 || currentAir < 0) { // current air is -1 if invalid or skipped due to low tps
//    	        ChunkLibrary.LOGGER.debug("Air delta check skipped due to TPS or data unavailability");
    	        return false;//age >= ageLimit;
    	    }

    	    int airDelta = getAirDelta(level, pos);
//    	    ChunkLibrary.LOGGER.debug("Air delta: {}", airDelta);
    	    
    	    boolean shouldReset = age >= ageLimit && airDelta >= airLossThreshold;
//    	    ChunkLibrary.LOGGER.debug("Final decision: ageCheck={}, airCheck={}, shouldReset={}",
//    	        age >= ageLimit, airDelta >= airLossThreshold, shouldReset);

    	    return shouldReset;
    	} else {
    	    boolean shouldReset = age >= ageLimit;
//    	    ChunkLibrary.LOGGER.debug("Air loss threshold too low or disabled. Should reset based on age only: {}", shouldReset);
    	    return shouldReset;
    	}
    }
}
