package com.hamderber.chunklibrary.data;

import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class TimeTrackerData extends SavedData {
    private static final String NAME = "time_tracker_data";

    private long totalTicks = 0;
    private long totalDays = 0;

    public TimeTrackerData() {}
    
    public static TimeTrackerData create() {
    	return new TimeTrackerData();
    }
    
    public static TimeTrackerData load(CompoundTag tag, Provider lookupProvider) {
        return new TimeTrackerData(tag);
    }
    
    public TimeTrackerData(CompoundTag tag) {
    	totalTicks = tag.getLong("totalTicks");
    	totalDays = tag.getLong("totalDays");
    }
    
    public static TimeTrackerData get() {
        ServerLevel overworld = LevelHelper.getOverworld();
        
        if (overworld == null) {
            throw new IllegalStateException("Overworld is not loaded yet");
        }
        
        return overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(TimeTrackerData::create, TimeTrackerData::load), NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider provider) {
        tag.putLong("totalTicks", totalTicks);
        tag.putLong("totalDays", totalDays);
        
        return tag;
    }

    public void tick(long ticksThisFrame) {
        totalTicks += ticksThisFrame;

        long expectedDay = totalTicks / Level.TICKS_PER_DAY;
        
        if (expectedDay > totalDays) {
            totalDays = expectedDay; // lazy increment. protects against lag causing skipped ticks

            setDirty();
        }
    }

    public long getTotalDays() {
        return totalDays;
    }

    public long getTotalTicks() {
        return totalTicks;
    }
    
    public void resetTotalDays() {
    	totalDays = 0;
    	totalTicks = 0;
    	setDirty();
    }
}
