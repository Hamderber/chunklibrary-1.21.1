package com.hamderber.chunklibrary;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.enums.AirEstimate;
import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ChunkScanner {
    public record ChunkKey(String dimId, long posLong) {}

    // using both of these allows for preventing duplicate entries while allowing for a separate container for walking the set
    // separate set for immediate to handle brand-new chunks
    private static final Set<ChunkKey> pendingScanSet = ConcurrentHashMap.newKeySet();
    private static final Queue<ChunkKey> pendingScanQueue = new ConcurrentLinkedQueue<>();
    private static final Set<ChunkKey> pendingScanSetImmediate = ConcurrentHashMap.newKeySet();
    private static final Queue<ChunkKey> pendingScanQueueImmediate = new ConcurrentLinkedQueue<>();

    private static int tickCount = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Handle immediate (first-time) scans
        immediateScan();

        // Handle batched scans
        batchedScan();
    }

    private static void immediateScan() {
        while(!pendingScanQueueImmediate.isEmpty()) {
            ChunkKey key = pendingScanQueueImmediate.poll();

            // This shouldn't happen, but just in case
            if (key == null) break;

            pendingScanSetImmediate.remove(key);

            ServerLevel level = LevelHelper.getServerLevel(key.dimId());
            if (level == null) {
                logWarnNullLevel(key);
                continue;
            }

            ChunkPos chunkPos = new ChunkPos(ChunkPos.getX(key.posLong()), ChunkPos.getZ(key.posLong()));

            // only get chunk if its loaded
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk == null) {
                // Not necessary to log that a chunk was skipped because it is no longer loaded.
                continue;
            }

            ChunkData data = ChunkData.get(level);
            scanForInitialAirEstimate(level, chunk, data);
        }
    }

    private static void batchedScan() {
        tickCount++;
        // skip ticks until the timer is met
        if (tickCount < ConfigAPI.getTicksBetweenChunkScanBatch()) return;
        tickCount = 0;

        for (int i = 0; i < ConfigAPI.getMaxChunkScansPerBatch(); i++) {
            ChunkKey key = pendingScanQueue.poll();
            if (key == null) break;

            pendingScanSet.remove(key);

            ServerLevel level = LevelHelper.getServerLevel(key.dimId());
            if (level == null) {
                logWarnNullLevel(key);
                continue;
            }

            ChunkPos chunkPos = new ChunkPos(ChunkPos.getX(key.posLong()), ChunkPos.getZ(key.posLong()));

            // only get chunk if its loaded
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk == null) {
                // Not necessary to log that a chunk was skipped because it is no longer loaded.
                continue;
            }

            ChunkData data = ChunkData.get(level);
            if (data.getInitialAirEstimate(level, chunkPos) == AirEstimate.DEFAULT.getValue() ||
                data.getCurrentAirEstimate(level, chunkPos) == AirEstimate.DEFAULT.getValue()) {
                    // If the chunk has the default for current or initial air estimate, pass handling to the immediate scan
                    scanForInitialAirEstimate(level, chunk, data);
            }
            else {
                scanForCurrentAirEstimate(level, chunk, data);
            }
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

    public static void queueChunkForScanImmediate(ServerLevel level, ChunkPos pos) {
        queueChunkForScanImmediate(LevelHelper.getDimensionID(level), pos);
    }

    public static void queueChunkForScanImmediate(String dimensionID, ChunkPos pos) {
        ChunkKey key = new ChunkKey(dimensionID, ChunkPos.asLong(pos.x, pos.z));

        if (pendingScanSetImmediate.add(key)) {
            pendingScanQueueImmediate.add(key);
        }
    }

    private static void scanForInitialAirEstimate(ServerLevel level, LevelChunk chunk, ChunkData data) {
        // bypass tps consideration for first-time scans
        int airCount = LevelHelper.sampleAirBlocksSafe(level, chunk, true);
        data.setInitialAirEstimate(level, chunk.getPos(), airCount);
        data.setCurrentAirEstimate(level, chunk.getPos(), airCount);
    }

    private static void scanForCurrentAirEstimate(ServerLevel level, LevelChunk chunk, ChunkData data) {
        int airCount = LevelHelper.sampleAirBlocksSafe(level, chunk, false);
        data.setCurrentAirEstimate(level, chunk.getPos(), airCount);
    }

    private static void logWarnNullLevel(ChunkKey key) {
        ChunkLibrary.LOGGER.warn("Unable to get ServerLevel associated with {}! Skipping.", key);
    }
}
