package com.cleanroommc.multiblocked.events;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Listeners {

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld());
        }
    }

    @SubscribeEvent
    public static void onWorldUnLoad(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).releaseSearchingThread();
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).getControllerInChunk(event.getChunk().getPos()).forEach(MultiblockState::onChunkLoad);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).getControllerInChunk(event.getChunk().getPos()).forEach(MultiblockState::onChunkUnload);
        }
    }

}
