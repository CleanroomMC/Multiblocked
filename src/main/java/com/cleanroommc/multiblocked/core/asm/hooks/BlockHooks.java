package com.cleanroommc.multiblocked.core.asm.hooks;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockHooks {

    public static Boolean doesSideBlockRendering(IBlockAccess world, BlockPos pos) {
        if (MultiblockWorldSavedData.isModelDisabled(pos)) {
            return false;
        }
        return null;
    }

    public static Boolean isFullCube(IBlockAccess world, BlockPos pos) {
        if (MultiblockWorldSavedData.isModelDisabled(pos)) {
            return false;
        }
        return null;
    }
}
