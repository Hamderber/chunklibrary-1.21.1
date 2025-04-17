package com.hamderber.chunklibrary.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

public class StartLoad extends ChunkLibraryEvent {
    public StartLoad(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag) {
        super(level, poiManager, regionStorageInfo, pos, tag);
    }
}