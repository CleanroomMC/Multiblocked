package io.github.cleanroommc.multiblocked.api.gui.factory;

import io.github.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityUIFactory extends UIFactory<TileEntity>{
    public static final TileEntityUIFactory INSTANCE  = new TileEntityUIFactory();

    private TileEntityUIFactory() {
        super();
    }

    @Override
    protected ModularUI createUITemplate(TileEntity holder, EntityPlayer entityPlayer) {
        if (holder instanceof IUIHolder) {
            return ((IUIHolder) holder).createUI(entityPlayer);
        }
        return null;
    }

    @Override
    protected TileEntity readHolderFromSyncData(PacketBuffer syncData) {
        return Minecraft.getMinecraft().world.getTileEntity(syncData.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, TileEntity holder) {
        syncData.writeBlockPos(holder.getPos());
    }
}
