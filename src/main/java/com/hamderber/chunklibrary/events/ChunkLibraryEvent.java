package com.hamderber.chunklibrary.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.neoforged.bus.api.Event;

public class ChunkLibraryEvent extends Event {
	public final ServerLevel level;
	public final PoiManager poiManager;
	public final RegionStorageInfo regionStorageInfo;
	public final ChunkPos pos;
	public final CompoundTag tag;
	
	// Don't call level.getChunk() from this event! You will cause infinite recursion from chunk loading.
	
    public ChunkLibraryEvent(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag) {
        this.level = level;
        this.poiManager = poiManager;
        this.regionStorageInfo = regionStorageInfo;
        this.pos = pos;
        this.tag = tag;
    }
}
