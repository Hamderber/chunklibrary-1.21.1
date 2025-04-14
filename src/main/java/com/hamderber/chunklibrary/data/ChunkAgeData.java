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
import net.minecraft.server.level.ServerLevel;

public class ChunkAgeData extends SavedData {
	private static final String NAME = "chunk_regen_data";
	private final Map<String, Map<Long, Long>> ageMap = new HashMap<>(); // Key: dimensionID, Value: Map<chunkPackedPos, lastGeneratedDay>
	
	public ChunkAgeData() {
		
	}
	
	public static ChunkAgeData create() {
	    return new ChunkAgeData();
	}

	public ChunkAgeData(CompoundTag tag) {
		ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);
		for (Tag t : list) {
		    CompoundTag chunkTag = (CompoundTag) t;
		    String dimensionId = chunkTag.getString("dim");
		    long packedPos = chunkTag.getLong("pos");
		    long day = chunkTag.getLong("day");

		    Map<Long, Long> dimensionChunkMap = ageMap.computeIfAbsent(dimensionId, k -> new HashMap<>());
		    dimensionChunkMap.put(packedPos, day);
		}
	}
	
	public static ChunkAgeData load(CompoundTag tag, Provider lookupProvider) {
		return new ChunkAgeData(tag);
	}

	public static ChunkAgeData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
			    new SavedData.Factory<ChunkAgeData>(ChunkAgeData::create, ChunkAgeData::load),NAME);
	}
	
	@Override
	public CompoundTag save(CompoundTag tag, Provider registries) {
		ListTag list = new ListTag();
		for (Map.Entry<String, Map<Long, Long>> dimensionEntry : ageMap.entrySet()) {
		    String dimensionId = dimensionEntry.getKey();
		    for (Map.Entry<Long, Long> chunkEntry : dimensionEntry.getValue().entrySet()) {
		        CompoundTag chunkTag = new CompoundTag();
		        chunkTag.putString("dim", dimensionId);
		        chunkTag.putLong("pos", chunkEntry.getKey());
		        chunkTag.putLong("day", chunkEntry.getValue());
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

	public long getLastGeneratedDay(ServerLevel level, ChunkPos pos) {
		Map<Long, Long> dimMap = ageMap.get(LevelHelper.getDimensionID(level));
		long defaultDay = TimeHelper.getCurrentDay(level);
		if (dimMap == null) return defaultDay;
		return dimMap.getOrDefault(ChunkPos.asLong(pos.x, pos.z), defaultDay);
	}
	
	public void removeChunkAgeData(ServerLevel level, ChunkPos pos) {
		String dimensionID = LevelHelper.getDimensionID(level);
		Map<Long, Long> dimMap = ageMap.get(dimensionID);
		if (dimMap != null) {
			dimMap.remove(ChunkPos.asLong(pos.x, pos.z));
			if (dimMap.isEmpty()) {
				ageMap.remove(dimensionID);
			}
			setDirty();
		}
	}
}
