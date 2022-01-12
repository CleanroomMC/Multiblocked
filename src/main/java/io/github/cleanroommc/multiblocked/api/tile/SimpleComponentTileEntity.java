package io.github.cleanroommc.multiblocked.api.tile;

import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import net.minecraft.util.EnumFacing;

/**
 * A TileEntity that defies all controller machines.
 *
 * Simple component of the multiblock.
 */
public class SimpleComponentTileEntity extends ComponentTileEntity<ComponentDefinition>{
    @Override
    public boolean isFormed() {
        return false;
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return false;
    }
}
