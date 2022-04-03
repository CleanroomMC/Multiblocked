package com.cleanroommc.multiblocked.core.asm.hooks;

import com.cleanroommc.multiblocked.util.world.DummyWorld;
import net.minecraft.tileentity.TileEntity;

public class MEKHooks {

    public static boolean sendUpdatePacket(TileEntity tile) {
        if (tile.getWorld() instanceof DummyWorld || tile.getWorld() == null) {
            return true;
        }
        return false;
    }

}
