package com.hamderber.chunklibrary.mixin;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.ChunkRegenerator;
import com.hamderber.chunklibrary.events.ChunkEvent;
import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.ticks.ProtoChunkTicks;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    @Inject(method = "read", at = @At("HEAD"), cancellable = true)
    private static void onReadStart(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
    	net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new ChunkEvent.StartLoad(level, poiManager, regionStorageInfo, pos, tag));
    	
//    	ChunkLibrary.LOGGER.debug(ChunkRegenerator.regenList.size() + " chunks are pending regeneration.");
    	
    	Pair<String, Long> pair = Pair.of(LevelHelper.getDimensionID(level), ChunkPos.asLong(pos.x, pos.z));
    	if (ChunkRegenerator.regenList.contains(pair)) {
//    		ChunkRegenerator.regenList.remove(pair);
    		
    		ProtoChunk dummy = new ProtoChunk(
		            pos,
		            UpgradeData.EMPTY,
		            new LevelChunkSection[level.getSectionsCount()],
		            new ProtoChunkTicks<>(),
		            new ProtoChunkTicks<>(),
		            level,
		            level.registryAccess().registryOrThrow(Registries.BIOME),
		            null
		        );
		        dummy.setPersistedStatus(ChunkStatus.EMPTY);
			
			cir.setReturnValue(dummy);
			
//			ChunkLibrary.LOGGER.debug("Chunk at " + pos.toString() + " in " + LevelHelper.getDimensionID(level) + " regenerated.");
    	}
    }
    
    @Inject(method = "read", at = @At("TAIL"), cancellable = true)
    private static void onReadEnd(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
    	net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new ChunkEvent.EndLoad(level, poiManager, regionStorageInfo, pos, tag));
    	
    	Pair<String, Long> pair = Pair.of(LevelHelper.getDimensionID(level), ChunkPos.asLong(pos.x, pos.z));
    	if (ChunkRegenerator.regenList.contains(pair)) {
    		ChunkRegenerator.regenList.remove(pair);
//    		ChunkLibrary.LOGGER.debug("Chunk at " + pos.toString() + " in " + LevelHelper.getDimensionID(level) + " regenerated.");
    	}
    }
}