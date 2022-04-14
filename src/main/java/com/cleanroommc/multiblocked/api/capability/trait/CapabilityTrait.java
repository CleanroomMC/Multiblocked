package com.cleanroommc.multiblocked.api.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IInnerCapabilityProvider;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.google.gson.JsonElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class CapabilityTrait implements IInnerCapabilityProvider {
    public final MultiblockCapability<?> capability;
    protected ComponentTileEntity<?> component;

    protected CapabilityTrait(MultiblockCapability<?> capability) {
        this.capability = capability;
    }

    public void serialize(@Nullable JsonElement jsonElement){

    }

    public JsonElement deserialize(){
        return null;
    }

    public void setComponent(ComponentTileEntity<?> component) {
        this.component = component;
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }

    public boolean hasUpdate() {
        return false;
    }
    
    public void update() {
        
    }

    public void markAsDirty() {
        if (component != null) {
            component.markAsDirty();
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
    }

    public void writeToNBT(NBTTagCompound compound) {
    }

    public void receiveCustomData(int id, PacketBuffer buffer) {
    }

    public final void writeCustomData(int id, Consumer<PacketBuffer> writer) {
        this.component.writeTraitData(this, id, writer);
    }

    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                group.addWidget(new SlotWidget(player.inventory, col + (row + 1) * 9, 7 + col * 18, 173 + row * 18)
                        .setLocationInfo(true, false));
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            group.addWidget(new SlotWidget(player.inventory, slot, 7 + slot * 18, 231)
                    .setLocationInfo(true, true));
        }
    }

    public void openConfigurator(WidgetGroup dialog) {
        
    }

}
