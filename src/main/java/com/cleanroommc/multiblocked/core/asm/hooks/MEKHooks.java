package com.cleanroommc.multiblocked.core.asm.hooks;

import com.cleanroommc.multiblocked.util.world.DummyWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MEKHooks {

    public static boolean sendUpdatePacket(TileEntity tile) {
        if (tile.getWorld() instanceof DummyWorld || tile.getWorld() == null) {
            return true;
        }
        return false;
    }

    public static boolean sendToDimension(int dimensionId) {
        if (dimensionId == Integer.MAX_VALUE - 1024) {
            return true;
        }
        return false;
    }

}
