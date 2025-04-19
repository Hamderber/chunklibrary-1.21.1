package com.hamderber.chunklibrary.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.hamderber.chunklibrary.ChunkLibrary;
import com.hamderber.chunklibrary.ChunkRegenerator;
import com.hamderber.chunklibrary.SuffocationFixer;
import com.hamderber.chunklibrary.data.WorldRegenData;
import com.hamderber.chunklibrary.events.EndLoad;
import com.hamderber.chunklibrary.events.StartLoad;
import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Crackiness.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ProtoChunkTicks;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    @Inject(method = "read", at = @At("HEAD"), cancellable = true)
    private static void onReadStart(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
    	net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new StartLoad(level, poiManager, regionStorageInfo, pos, tag));
    	
    	String dimensionID = LevelHelper.getDimensionID(level);
    	
    	if (WorldRegenData.get().isMarked(level, pos)) {
    		ProtoChunk dummy = new ProtoChunk(
	            pos,
	            UpgradeData.EMPTY,
	            new LevelChunkSection[level.getSectionsCount()],
	            new ProtoChunkTicks<>(),
	            new ProtoChunkTicks<>(),
	            level,
	            level.registryAccess().registryOrThrow(Registries.BIOME),
	            null);
    		
	        dummy.setPersistedStatus(ChunkStatus.EMPTY);
		    SuffocationFixer.addChunkToSuffocationCheck(dimensionID, pos.x, pos.z);
			cir.setReturnValue(dummy);
    	}
    }
    
    @Inject(method = "read", at = @At("TAIL"), cancellable = true)
    private static void onReadEnd(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
    	net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new EndLoad(level, poiManager, regionStorageInfo, pos, tag));
    	
    }
}