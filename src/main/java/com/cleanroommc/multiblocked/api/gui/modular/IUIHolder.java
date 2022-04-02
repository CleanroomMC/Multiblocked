package com.cleanroommc.multiblocked.api.gui.modular;


import net.minecraft.entity.player.EntityPlayer;

public interface IUIHolder {
    IUIHolder EMPTY = new IUIHolder() {
        @Override
        public ModularUI createUI(EntityPlayer entityPlayer) {
            return null;
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public boolean isRemote() {
            return false;
        }

        @Override
        public void markAsDirty() {

        }
    };

    ModularUI createUI(EntityPlayer entityPlayer);

    boolean isInvalid();

    boolean isRemote();

    void markAsDirty();

}
