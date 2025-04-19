package com.hamderber.chunklibrary.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public class WorldRegenData extends SavedData {
    private static final String NAME = "world_regen_data";
    private final Set<ChunkRecord> regenList = new HashSet<>();

    public WorldRegenData() {}

    public static WorldRegenData create() {
        return new WorldRegenData();
    }

    public static WorldRegenData load(CompoundTag tag, Provider registries) {
        return new WorldRegenData(tag);
    }

    public WorldRegenData(CompoundTag tag) {
        ListTag list = tag.getList("regen_chunks", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag chunkTag = (CompoundTag) t;
            String dim = chunkTag.getString("regen_dim");
            int posX = chunkTag.getInt("regen_posX");
            int posZ = chunkTag.getInt("regen_posZ");
            ChunkRecord record = new ChunkRecord(dim, posX, posZ);
            regenList.add(record);
            ChunkLibrary.LOGGER.debug("Loaded: " + record);
        }
    }

    public static WorldRegenData get() {
        ServerLevel overworld = LevelHelper.getOverworld();

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not loaded yet");
        }

        // Retrieve the WorldRegenData for the entire world (this is global data, not chunk-specific)
        return overworld.getDataStorage().computeIfAbsent(
            new Factory<>(WorldRegenData::create, WorldRegenData::load),
            NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider registries) {
        ListTag list = new ListTag();
        for (ChunkRecord record : regenList) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putString("regen_dim", record.dim());
            chunkTag.putInt("regen_posX", record.x());
            chunkTag.putInt("regen_posZ", record.z());
            list.add(chunkTag);
        }
        tag.put("regen_chunks", list);
        return tag;
    }

    public void addChunk(ServerLevel level, ChunkPos pos) {
        String dimId = LevelHelper.getDimensionID(level);
        ChunkRecord record = new ChunkRecord(dimId, pos.x, pos.z);

        // Only add the record if it does not already exist
        if (regenList.add(record)) {
            ChunkLibrary.LOGGER.debug("Added: " + record);
            setDirty();
        } else {
            ChunkLibrary.LOGGER.debug("Chunk already exists: " + record);
        }
    }

    public boolean isMarked(ServerLevel level, ChunkPos pos) {
        String dimId = LevelHelper.getDimensionID(level);
        return regenList.contains(new ChunkRecord(dimId, pos.x, pos.z));
    }

    public void clearChunk(ServerLevel level, ChunkPos pos) {
        String dimId = LevelHelper.getDimensionID(level);
        ChunkRecord record = new ChunkRecord(dimId, pos.x, pos.z);
        
        ChunkLibrary.LOGGER.debug("Trying to remove: \"" + record.dim() + "\" pos=" + record.x() + "|" + record.z());
        for (ChunkRecord r : regenList) {
            ChunkLibrary.LOGGER.debug("In list: \"" + r.dim() + "\" pos=" + r.x() + "|" + r.z());
        }
        
        for (ChunkRecord r : regenList) {
            if (r.dim().equals(dimId) && r.x() == pos.x && r.z() == pos.z) {
                ChunkLibrary.LOGGER.debug("Match found manually: " + r);
            }
        }
        
        if (regenList.remove(record)) {
            ChunkLibrary.LOGGER.debug("Removed: " + record);
            setDirty();
        }
    }

    public Set<ChunkRecord> getChunks() {
        return Collections.unmodifiableSet(regenList);
    }
}
