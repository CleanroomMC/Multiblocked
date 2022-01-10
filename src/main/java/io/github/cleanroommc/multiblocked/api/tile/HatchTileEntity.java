package io.github.cleanroommc.multiblocked.api.tile;


public class HatchTileEntity extends ComponentTileEntity{

    @Override
    public ComponentTileEntity createNewTileEntity() {
        HatchTileEntity tileEntity = new HatchTileEntity();
        tileEntity.location = this.location;
        return tileEntity;
    }
}
