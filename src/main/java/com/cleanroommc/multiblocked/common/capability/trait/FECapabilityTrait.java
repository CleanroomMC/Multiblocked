package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.trait.ProgressCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FECapabilityTrait extends ProgressCapabilityTrait {
    private EnergyStorage handler;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public FECapabilityTrait() {
        super(FEMultiblockCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capacity = JsonUtils.getInt(jsonObject, "capacity", 10000);
        maxReceive = JsonUtils.getInt(jsonObject, "maxReceive", 500);
        maxExtract = JsonUtils.getInt(jsonObject, "maxExtract", 500);
        handler = new EnergyStorage(capacity, maxReceive, maxExtract);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        jsonObject.addProperty("maxReceive", maxReceive);
        jsonObject.addProperty("maxExtract", maxExtract);
        return jsonObject;
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        return String.format("FE Stored: %d / %d", (int)(handler.getMaxEnergyStored() * progress), handler.getMaxEnergyStored());
    }

    @Override
    protected double getProgress() {
        return handler.getEnergyStored() * 1f / handler.getMaxEnergyStored();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("_")) {
            handler = new EnergyStorage(capacity, maxReceive, maxExtract, Math.min(compound.getInteger("_"), capacity));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("_", handler.getEnergyStored());
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        dialog.addWidget(new TextFieldWidget(60, 5, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(capacity + "")
                .setHoverTooltip("capability (RF)"));

        dialog.addWidget(new TextFieldWidget(60, 25, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(maxReceive + "")
                .setHoverTooltip("maxReceive (RF/packet)"));

        dialog.addWidget(new TextFieldWidget(60, 45, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(maxExtract + "")
                .setHoverTooltip("maxExtract (RF/packet)"));
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(new ProxyEnergyStorage(handler, capabilityIO, false)) : null;
    }

    @Nullable
    @Override
    public <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(new ProxyEnergyStorage(handler, capabilityIO, true)) : null;
    }

    private class ProxyEnergyStorage implements IEnergyStorage {
        public EnergyStorage proxy;
        public IO io;
        public boolean inner;

        public ProxyEnergyStorage(EnergyStorage proxy, IO io, boolean inner) {
            this.proxy = proxy;
            this.io = io;
            this.inner = inner;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (io == IO.BOTH || (inner ? io == IO.OUT : io == IO.IN)) {
                if (!simulate) markAsDirty();
                return proxy.receiveEnergy(maxReceive, simulate);
            }
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (io == IO.BOTH || (inner ? io == IO.IN : io == IO.OUT)) {
                if (!simulate) markAsDirty();
                return proxy.extractEnergy(maxExtract, simulate);
            }
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return proxy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return proxy.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            if (io == IO.BOTH || (inner ? io == IO.IN : io == IO.OUT)) {
                return proxy.canExtract();
            }
            return false;
        }

        @Override
        public boolean canReceive() {
            if (io == IO.BOTH || (inner ? io == IO.OUT : io == IO.IN)) {
                return proxy.canReceive();
            }
            return false;
        }
    }

}
