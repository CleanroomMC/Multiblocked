package io.github.cleanroommc.multiblocked.gui.modular;


import net.minecraft.entity.player.EntityPlayer;

public interface IUIHolder {

    ModularUI createUI(EntityPlayer entityPlayer);

    boolean isInvalid();

    boolean isRemote();

    void markDirty();

}
