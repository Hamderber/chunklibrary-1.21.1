package com.hamderber.chunklibrary.data;

import java.util.HashMap;
import java.util.Map;

import com.hamderber.chunklibrary.util.LevelHelper;
import com.hamderber.chunklibrary.util.TimeHelper;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class ChunkData extends SavedData {
	private static final String NAME = "chunk_regen_data";
	private static final Map<String, Map<Long, Long>> ageMap = new HashMap<>(); // Key: dimensionID, Value: Map<chunkPackedPos, lastGeneratedDay>
	private static final Map<String, Map<Long, Long>> timesGeneratedMap = new HashMap<>();// Key: dimensionID, Value: Map<chunkPackedPos, numTimesRegenerated>
	
	public ChunkData() {}
	
	public static ChunkData create() {
	    return new ChunkData();
	}
	
	public static ChunkData load(CompoundTag tag, Provider lookupProvider) {
		return new ChunkData(tag);
	}

	public ChunkData(CompoundTag tag) {
		ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);
		
		for (Tag t : list) {
		    CompoundTag chunkTag = (CompoundTag) t;
		    
		    String dimensionId = chunkTag.getString("dim");
		    long packedPos = chunkTag.getLong("pos");
		    long day = chunkTag.getLong("day");
		    long timesRegened = chunkTag.getLong("timesRegened");

		    Map<Long, Long> dimensionChunkMap = ageMap.computeIfAbsent(dimensionId, k -> new HashMap<>());
		    dimensionChunkMap.put(packedPos, day);
		    
		    Map<Long, Long> dimensionRegenChunkMap = timesGeneratedMap.computeIfAbsent(dimensionId, k-> new HashMap<>());
		    dimensionRegenChunkMap.put(packedPos, timesRegened);
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
		setLastGeneratedDay(level, pos, TimeHelper.getTrueOverworldDay());
	}

	public long getLastGeneratedDay(ServerLevel level, ChunkPos pos) {
		Map<Long, Long> dimMap = ageMap.get(LevelHelper.getDimensionID(level));
		
		long defaultDay = TimeHelper.getTrueOverworldDay();
		
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
    	long currentDay = TimeHelper.getTrueOverworldDay();
		long lastGeneratedDay = getLastGeneratedDay(level, pos);
		
    	return currentDay - lastGeneratedDay;
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
}
