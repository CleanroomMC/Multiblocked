package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.trait.ProgressCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.common.capability.EnergyGTCECapability;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyCapabilityTrait extends ProgressCapabilityTrait implements ITickable {
    private EnergyContainerHandler handler;
    protected int capacity;

    public EnergyCapabilityTrait() {
        super(EnergyGTCECapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capacity = JsonUtils.getInt(jsonObject, "capacity", 10000);
        handler = new EnergyContainerHandler(capacity);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        return jsonObject;
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        return String.format("EU Stored: %d / %d", (int)(handler.getEnergyCapacity() * progress), handler.getEnergyCapacity());
    }

    @Override
    protected double getProgress() {
        return handler.getEnergyStored() * 1f / handler.getEnergyCapacity();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("_")) {
            handler.deserializeNBT(compound.getCompoundTag("_"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("_", handler.serializeNBT());
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        super.initSettingDialog(dialog, slot);
        dialog.addWidget(new TextFieldWidget(60, 5, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(capacity + "")
                .setHoverTooltip("capability (EU)"));
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER ? GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(new ProxyEnergyStorage(handler, capabilityIO, false)) : null;
    }

    @Nullable
    @Override
    public <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER ? GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(new ProxyEnergyStorage(handler, capabilityIO, true)) : null;
    }

    @Override
    public void update() {
        handler.update();
    }

    private static class ProxyEnergyStorage implements IEnergyContainer {
        public EnergyContainerHandler proxy;
        public IO io;
        public boolean inner;

        public ProxyEnergyStorage(EnergyContainerHandler proxy, IO io, boolean inner) {
            this.proxy = proxy;
            this.io = io;
            this.inner = inner;
        }

        @Override
        public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
            if (io == IO.BOTH || (inner ? io == IO.OUT : io == IO.IN)) {
                return proxy.acceptEnergyFromNetwork(enumFacing, l, l1);
            }
            return 0;
        }

        @Override
        public boolean inputsEnergy(EnumFacing enumFacing) {
            if (io == IO.BOTH || (inner ? io == IO.OUT : io == IO.IN)) {
                return proxy.inputsEnergy(enumFacing);
            }
            return false;
        }

        @Override
        public boolean outputsEnergy(EnumFacing side) {
            if (io == IO.BOTH || (inner ? io == IO.IN : io == IO.OUT)) {
                return proxy.outputsEnergy(side);
            }
            return false;
        }

        @Override
        public long changeEnergy(long l) {
            if (l > 0) {
                if (io == IO.BOTH || (inner ? io == IO.OUT : io == IO.IN)) {
                    return proxy.changeEnergy(l);
                }
            } else {
                if (io == IO.BOTH || (inner ? io == IO.IN : io == IO.OUT)) {
                    return proxy.changeEnergy(l);
                }
            }
            return 0;
        }

        @Override
        public long getEnergyStored() {
            return proxy.getEnergyStored();
        }

        @Override
        public long getEnergyCapacity() {
            return proxy.getEnergyCapacity();
        }

        @Override
        public long getInputAmperage() {
            return proxy.getInputAmperage();
        }

        @Override
        public long getInputVoltage() {
            return proxy.getInputVoltage();
        }

    }

    public class EnergyContainerHandler implements IEnergyContainer {

        protected final long maxCapacity;
        protected long energyStored;
        protected long amps = 0;

        public EnergyContainerHandler(long maxCapacity) {
            this.maxCapacity = maxCapacity;
        }


        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong("EnergyStored", energyStored);
            return compound;
        }

        public void deserializeNBT(NBTTagCompound compound) {
            this.energyStored = compound.getLong("EnergyStored");
        }

        @Override
        public long getEnergyStored() {
            return this.energyStored;
        }

        public void setEnergyStored(long energyStored) {
            this.energyStored = energyStored;
            markAsDirty();
        }

        public void update() {
            long energyUsed = 0;
            amps = 0;
            if (component.getWorld().isRemote) return;

            for (EnumFacing side : EnumFacing.VALUES) {
                if (!outputsEnergy(side)) continue;
                TileEntity tileEntity = component.getWorld().getTileEntity(component.getPos().offset(side));
                EnumFacing oppositeSide = side.getOpposite();
                if (tileEntity != null && tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide)) {
                    IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide);
                    if (energyContainer != null && energyContainer.inputsEnergy(oppositeSide)) {
                        long outputV = energyContainer.getInputVoltage();
                        if (outputV > energyUsed) continue;
                        energyUsed += energyContainer.acceptEnergyFromNetwork(oppositeSide, outputV, 1) * energyContainer.getInputVoltage();
                    }
                    if (energyUsed >= getEnergyStored()) break;
                }
            }

            if (energyUsed > 0) {
                setEnergyStored(getEnergyStored() - energyUsed);
            }
        }

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            if (amps >= getInputAmperage()) return 0;
            long canAccept = getEnergyCapacity() - getEnergyStored();
            if (voltage > 0L && (side == null || inputsEnergy(side))) {
                if (canAccept >= voltage) {
                    long amperesAccepted = Math.min(canAccept / voltage, amperage);
                    if (amperesAccepted > 0) {
                        setEnergyStored(getEnergyStored() + voltage * amperesAccepted);
                        amps += amperesAccepted;
                        return amperesAccepted;
                    }
                }
            }
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            return this.maxCapacity;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return true;
        }

        @Override
        public boolean outputsEnergy(EnumFacing side) {
            return component.getFrontFacing() == side || !component.getDefinition().allowRotate;
        }

        @Override
        public long changeEnergy(long energyToAdd) {
            long oldEnergyStored = getEnergyStored();
            long newEnergyStored = (maxCapacity - oldEnergyStored < energyToAdd) ? maxCapacity : (oldEnergyStored + energyToAdd);
            if (newEnergyStored < 0)
                newEnergyStored = 0;
            setEnergyStored(newEnergyStored);
            return newEnergyStored - oldEnergyStored;
        }

        @Override
        public long getOutputVoltage() {
            return 0;
        }

        @Override
        public long getOutputAmperage() {
            return 0;
        }

        @Override
        public long getInputAmperage() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getInputVoltage() {
            return Long.MAX_VALUE;
        }

    }
}
