package com.cleanroommc.multiblocked.core.asm.hooks;

import com.cleanroommc.multiblocked.util.world.DummyWorld;
import net.minecraft.tileentity.TileEntity;

public class MEKHooks {

    public static boolean sendUpdatePacket(TileEntity tile) {
        return tile.getWorld() instanceof DummyWorld || tile.getWorld() == null;
    }

    public static boolean sendToDimension(int dimensionId) {
        return dimensionId == Integer.MAX_VALUE - 1024;
    }

}
