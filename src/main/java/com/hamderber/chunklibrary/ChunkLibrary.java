package com.hamderber.chunklibrary;

import com.hamderber.chunklibrary.enums.AirEstimate;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import com.hamderber.chunklibrary.config.ConfigAPI;
import com.hamderber.chunklibrary.data.ChunkData;
import com.hamderber.chunklibrary.data.TimeTrackerData;
import com.hamderber.chunklibrary.util.LevelHelper;
import com.hamderber.chunklibrary.util.TimeHelper;

@Mod(ChunkLibrary.MODID)
public class ChunkLibrary
{
    public static final String MODID = "chunklibrary";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public ChunkLibrary(IEventBus modEventBus, ModContainer modContainer)
    {
    	LOGGER.info(MODID + " loaded!");
    	
    	NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    	NeoForge.EVENT_BUS.register(new ChunkHandler());
    	NeoForge.EVENT_BUS.register(new TimeHelper());
    	NeoForge.EVENT_BUS.register(new ChunkScanner());
    	NeoForge.EVENT_BUS.register(new SuffocationFixer());
    }
    
    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        //TODO: Make a command factory because this is ugly as shit
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("regen_chunk_bypass_checks")
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                            .executes(context -> {
                                ServerLevel level = DimensionArgument.getDimension(context, "dimension");
                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "position");

                                int chunkX = pos.getX() >> 4;
                                int chunkZ = pos.getZ() >> 4;

                                String dimId = LevelHelper.getDimensionID(level);
                                
                                ChunkRegenerator.regenerateChunk(dimId, chunkX, chunkZ);

                                context.getSource().sendSuccess(() ->
                                    Component.literal("Scheduled regeneration for chunk at " + chunkX + ", " + chunkZ 
                                        + " in dimension " + dimId + ". Unload it to for regneration to occur (compatibility)."),
                                    true
                                );

                                return 1;
                            })))));
        
        dispatcher.register(Commands.literal("chunklibrary")
    	    .requires(source -> source.hasPermission(2))
        	    .then(Commands.literal("set_world_age")
        	        .then(Commands.argument("days", LongArgumentType.longArg())
        	            .executes(context -> {
        	                long days = LongArgumentType.getLong(context, "days");
        	                long beforeAge = TimeHelper.getWorldAge();
        	                
        	                TimeTrackerData.get().setTotalDays(days);
        	                
        	                long age = TimeHelper.getWorldAge();

        	                context.getSource().sendSuccess(() ->
        	                    Component.literal("Set world age to " + days + " days. It is now " + beforeAge + " -> " + age + " days old."),
        	                    true
        	                );

        	                return 1;
        	            }))));
        
        dispatcher.register(Commands.literal("chunklibrary")
    	    .requires(source -> source.hasPermission(2))
    	    	.then(Commands.literal("get_world_age")
    	            .executes(context -> {
	                	long days = TimeHelper.getWorldAge();
	                	
    	                context.getSource().sendSuccess(() ->
    	                    Component.literal("The world is " + days + " day" + (days == 1 ? "" : "s") + " old (since mod installation)"),
    	                    true
    	                );

    	                return 1;
    	            })));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get_chunk_info")
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                            .executes(context -> {
                                ServerLevel level = DimensionArgument.getDimension(context, "dimension");
                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "position");
                                
                                ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                                ChunkData data = ChunkData.get(level);
                                
                                long age = data.getChunkAge(level, chunkPos);
                                long numRegen = data.getTimesGenerated(level, chunkPos);

                                int initialAirEstimate = data.getInitialAirEstimate(level, chunkPos);
                                int currentAirEstimate = data.getCurrentAirEstimate(level, chunkPos);
                                int airDelta = data.getAirDelta(level, chunkPos);

                                String dimensionID = LevelHelper.getDimensionID(level);

                                boolean shouldResetChunk = data.shouldResetChunk(level, chunkPos, ConfigAPI.getRegenPeriod(dimensionID));

                                context.getSource().sendSuccess(() ->
                                    Component.literal("Chunk at " + chunkPos + " in " + dimensionID +
                                		", since mod installation, is " + age + " days old and has been generated " + numRegen +
                                		" time(s). " + (currentAirEstimate == AirEstimate.DEFAULT.getValue() ?
                                				"The chunk has not yet been scanned for air. " :
                                				"Last scan had " + currentAirEstimate + " near-surface air blocks. The chunk was first scanned with " + initialAirEstimate + " air blocks. Since then, +/- " + airDelta +" air block" + (airDelta == 1 ? "" : "s") + " have changed. ") +
                                		"This chunk " + ( shouldResetChunk ? "is" : "is not") + " eligible to regenerate."),
                                    true
                                );

                                return 1;
                            })))));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set_chunk_age")
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                	        .then(Commands.argument("days", LongArgumentType.longArg())
                                .executes(context -> {
                                    ServerLevel level = DimensionArgument.getDimension(context, "dimension");
                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "position");
                                    
                                    ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                                    ChunkData data = ChunkData.get(level);
                                    
                                    long days = LongArgumentType.getLong(context, "days");
                                    long beforeAge = data.getChunkAge(level, chunkPos);
                                    
                	                data.setChunkAge(level, chunkPos, LongArgumentType.getLong(context, "days"));
                	                
                	                long age = data.getChunkAge(level, chunkPos);

                	                context.getSource().sendSuccess(() ->
                	                    Component.literal("Set chunk age to " + days + " at " + chunkPos + " in " +
                	                    		LevelHelper.getDimensionID(level) + ". It is now " + beforeAge + " -> " + age + " days old."),
                	                    true
                                    );

                                    return 1;
                                }))))));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("air_scan_chunk")
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                            .executes(context -> {
                                ServerLevel level = DimensionArgument.getDimension(context, "dimension");
                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "position");
                                
                                ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                                ChunkData data = ChunkData.get(level);
                                
                                int airBefore = data.getCurrentAirEstimate(level, chunkPos);
                                int airEstimate = LevelHelper.sampleAirBlocksUnsafe(level, chunkPos, true);
                                data.setCurrentAirEstimate(level, chunkPos, airEstimate);
                                int currentAir = data.getCurrentAirEstimate(level, chunkPos);

            	                context.getSource().sendSuccess(() ->
            	                    Component.literal("Chunk at " + chunkPos + " in " + LevelHelper.getDimensionID(level) +
            	                    		" previously had ~" + airBefore + " relavant air blocks. New estimate ~" + airEstimate + 
            	                    		" (~" + airBefore + " -> ~" + currentAir + ")"),
            	                    true
                                );

                                return 1;
                            })))));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset_dimension_chunk_data")
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .executes(context -> {
                            ServerLevel level = DimensionArgument.getDimension(context, "dimension");
                            
                            ChunkData data = ChunkData.get(level);
                            data.resetChunkData(level);

                            context.getSource().sendSuccess(() ->
                                Component.literal(LevelHelper.getDimensionID(level) + " chunk data has been reset."),
                                true
                            );

                            return 1;
                        }))));
    
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset_all_chunk_age_data")
                    .executes(context -> {
                        ChunkData.resetAllChunkData();

                        context.getSource().sendSuccess(() ->
                            Component.literal("Chunk age data has been reset."),
                            true
                        );

                        return 1;
                    })));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset_world_age")
                    .executes(context -> {
                        TimeTrackerData.get().resetTotalDays();
                        
                        long age = TimeHelper.getWorldAge();
                        
                        context.getSource().sendSuccess(() ->
                            Component.literal("The world is now " + age + " days old."),
                            true
                        );

                        return 1;
                    })));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug_dump_library_config")
                    .executes(context -> {
                        context.getSource().sendSuccess(() ->
                            Component.literal(ConfigAPI.dumpConfigSettings()),
                            true
                        );

                        return 1;
                    })));
        
        dispatcher.register(Commands.literal("chunklibrary")
            .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug_dump_dimensions")
                    .executes(context -> {
                    	
                    	StringBuilder dimensionIds = new StringBuilder();

                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                        if (server != null) {
                            for (ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                                dimensionIds.append(LevelHelper.getDimensionID(level)).append(" ");
                            }
                        }

                        context.getSource().sendSuccess(() ->
                            Component.literal(dimensionIds.toString()),
                            true
                        );

                        return 1;
                    })));
        }
}
