package com.hamderber.chunklibrary;

import org.slf4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.hamderber.chunklibrary.util.LevelHelper;

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
    }
    
    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("chunklibrary")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("regenchunk")
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
    }
}
