package com.hamderber.chunklibrary;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ChunkScanner {
    public record ChunkKey(String dimId, long posLong) {}

    // using both of these allows for preventing duplicate entries while allowing for a separate container for walking the set
    // dedupe lookup
    private static final Set<ChunkKey> pendingScanSet = ConcurrentHashMap.newKeySet();
    // ordered queue
    private static final Queue<ChunkKey> pendingScanQueue = new ConcurrentLinkedQueue<>();

    private static int tickCount = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCount++;
        if (tickCount < ConfigAPI.getTicksBetweenChunkScanBatch()) return;
        tickCount = 0;
        
        int chunkCount = 0;

        for (int i = 0; i < ConfigAPI.getMaxChunkScansPerBatch(); i++) {
            ChunkKey key = pendingScanQueue.poll();
            if (key == null) break;

            pendingScanSet.remove(key);
            
            ServerLevel level = LevelHelper.getServerLevel(key.dimId());
            if (level == null) continue;

            int chunkPosX = ChunkPos.getX(key.posLong());
            int chunkPosZ = ChunkPos.getZ(key.posLong());

            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPosX, chunkPosZ); // only get chunk if its loaded
            if (chunk == null) continue;
            
            int airSample = LevelHelper.sampleAirBlocksSafe(level, chunk, false);
            if (airSample == -1) {
                // not scanned so requeue
                queueChunkForScan(level, new ChunkPos(chunkPosX, chunkPosZ));
            }

            chunkCount++;

            ChunkLibrary.LOGGER.debug("Chunk at [" + chunkPosX + ", " + chunkPosZ + "] scanned. "
                + "Air Sample: " + airSample + " "
                + "Queue size: " + pendingScanQueue.size() + " "
                + "Chunk # in tick: " + chunkCount);
        }
    }

    public static void queueChunkForScan(ServerLevel level, ChunkPos pos) {
        queueChunkForScan(LevelHelper.getDimensionID(level), pos);
    }

    public static void queueChunkForScan(String dimensionID, ChunkPos pos) {
        ChunkKey key = new ChunkKey(dimensionID, ChunkPos.asLong(pos.x, pos.z));
        
        if (pendingScanSet.add(key)) { // only add if new
            pendingScanQueue.add(key);
        }
    }
}
