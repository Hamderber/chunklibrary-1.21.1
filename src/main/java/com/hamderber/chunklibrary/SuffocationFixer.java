package com.hamderber.chunklibrary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.hamderber.chunklibrary.util.LevelHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class SuffocationFixer {
	private static Map<Pair<String, Long>, Byte> checkForSuffocation = new HashMap<>();
	
	@SubscribeEvent
    public void onSuffocationDamage(LivingDamageEvent.Pre event) {
		LivingEntity entity = event.getEntity();
		ServerLevel level = (ServerLevel) entity.level();
		BlockPos blockPos = entity.blockPosition();
		ChunkPos chunkPos = new ChunkPos(blockPos);
		
//		ChunkLibrary.LOGGER.debug("Damage for: " + entity.toString()  + " " + event.getSource().getMsgId());
		
		Pair<String, Long> entry = Pair.of(LevelHelper.getDimensionID(level), ChunkPos.asLong(chunkPos.x, chunkPos.z));
		
    	if (checkForSuffocation.containsKey(entry)) {
//        	ChunkLibrary.LOGGER.debug(chunkPos.toString() + " in suffocation check");
	        DamageType type = event.getSource().type();
	
	        if (type.msgId().contains("inWall")) {
	            BlockPos safe = findSafePositionAbove(entity.blockPosition(), level);

//	    		ChunkLibrary.LOGGER.debug("Suffocation damage for: " + entity.toString());
	            if (!safe.equals(entity.blockPosition())) {
	            	entity.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
	                event.setNewDamage(0);
	                
	                if (entity instanceof ServerPlayer player) {
	                	player.sendSystemMessage(Component.literal("You were protected by the suffocation damage from a regenerated chunk.")
	                			.withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)));
	                }
	                
//	                ChunkLibrary.LOGGER.debug("Moved " + entity.getName().getString() + " to safe Y: " + safe.getY());
	            }
	        }
    	}
    }
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		final byte EXPIRATION = 100;
	    Iterator<Map.Entry<Pair<String, Long>, Byte>> iterator = checkForSuffocation.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Map.Entry<Pair<String, Long>, Byte> entry = iterator.next();
	        byte newTimer = (byte)(entry.getValue() + 1);
//	        ChunkLibrary.LOGGER.debug("Timer " + (int) newTimer);
	        if (newTimer >= EXPIRATION) {
	            iterator.remove();
	        } else {
	            entry.setValue(newTimer);
	        }
	    }
	}

    private static BlockPos findSafePositionAbove(BlockPos start, Level level) {
        for (int y = start.getY(); y < level.getMaxBuildHeight(); y++) {
            BlockPos check = new BlockPos(start.getX(), y, start.getZ());
            if (level.getBlockState(check).isAir()) {
                return check;
            }
        }
        return start;
    }
    
    public static void addChunkToSuffocationCheck(String dimensionID, int x, int z) {
    	ChunkPos chunkPos = new ChunkPos(x, z);
    	long chunkPosLong = chunkPos.toLong();
    	checkForSuffocation.put(Pair.of(dimensionID, chunkPosLong), (byte) 0);
//    	ChunkLibrary.LOGGER.debug(chunkPos.toString() + " added to suffocation check");
    }
}
