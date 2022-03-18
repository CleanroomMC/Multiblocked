package com.cleanroommc.multiblocked.client.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModelSupplier {

    @SideOnly(Side.CLIENT)
    void onModelRegister();

}
