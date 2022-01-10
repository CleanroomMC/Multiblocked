package io.github.cleanroommc.multiblocked.events;

import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Listeners {

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()); // Pre-load
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
