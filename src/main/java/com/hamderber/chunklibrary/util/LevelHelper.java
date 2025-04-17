package com.hamderber.chunklibrary.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class LevelHelper {
	public static String getDimensionID(Level level) {
		return level.dimension().location().toString();
	}
	
	public static ServerLevel getServerLevel(String dimensionID) {
	    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
	    if (server == null) return null;

	    for (ServerLevel level : server.getAllLevels()) {
	        if (level.dimension().location().toString().equals(dimensionID)) {
	            return level;
	        }
	    }

	    return null;
	}
	
	public static ResourceKey<Level> getDimensionKey(String dimensionID) {
	    ResourceLocation location = ResourceLocation.tryParse(dimensionID);
	    if (location == null) return null;

	    return ResourceKey.create(Registries.DIMENSION, location);
	}
	
	public static ChunkPos chunkPosFromBlockPos(BlockPos pos) {
		return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
	}
	
	public static ServerLevel getOverworld() {
		return ServerLifecycleHooks.getCurrentServer().overworld();
	}
}
