package io.github.cleanroommc.multiblocked.api.tile;

/**
 * A TileEntity that defies all hatch machines.
 *
 * I/O part of the multiblock.
 */
public class HatchTileEntity extends ComponentTileEntity{

    @Override
    public boolean isFormed() {
        return false;
    }
}
