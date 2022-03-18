package com.cleanroommc.multiblocked.api.tile.part;

/**
 * A TileEntity that defies all hatch machines.
 *
 * I/O part of the multiblock.
 */
public class PartHatchTileEntity extends PartTileEntity {

    @Override
    public boolean isFormed() {
        return false;
    }
}
