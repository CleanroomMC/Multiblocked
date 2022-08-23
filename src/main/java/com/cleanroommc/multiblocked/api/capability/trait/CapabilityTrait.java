package com.cleanroommc.multiblocked.api.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IInnerCapabilityProvider;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
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
        return new JsonObject();
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

    public boolean receiveClientEvent(int id, int type) {
        return false;
    }

    public void validate() {}

    public void invalidate() {}

    public void onLoad() {}

    public void onChunkUnload() {}

    public void onNeighborChanged() {}

    public final void writeCustomData(int id, Consumer<PacketBuffer> writer) {
        this.component.writeTraitData(this, id, writer);
    }

    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {

    }

    public void openConfigurator(WidgetGroup dialog) {
        
    }

    public void onDrops(NonNullList<ItemStack> drops, EntityPlayer player) {

    }
}
