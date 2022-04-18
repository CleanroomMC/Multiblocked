package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.trait.MultiCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TankWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FluidCapabilityTrait extends MultiCapabilityTrait {
    private FluidTankList handler;
    private int[] tankCapability;

    public FluidCapabilityTrait() {
        super(FluidMultiblockCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonArray();
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int size = jsonArray.size();
        tankCapability = new int[size];
        int i = 0;
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            tankCapability[i] = JsonUtils.getInt(jsonObject, "tC", 1000);
            i++;
        }
        handler = new FluidTankList(capabilityIO, Arrays.stream(tankCapability).mapToObj(FluidTank::new).toArray(FluidTank[]::new));
    }

    @Override
    public JsonElement deserialize() {
        JsonArray jsonArray = super.deserialize().getAsJsonArray();
        for (int i = 0; i < capabilityIO.length; i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            jsonObject.addProperty("tC", tankCapability[i]);
        }
        return jsonArray;
    }

    @Override
    public boolean hasUpdate() {
        return ArrayUtils.contains(autoIO, true);
    }

    @Override
    public void update() {
        for (int i = 0; i < autoIO.length; i++) {
            if (autoIO[i]) {
                if (capabilityIO[i] == IO.IN) {
                    FluidTank already = this.handler.getTankAt(i);
                    FluidStack fluidStack = already.getFluid();
                    int need = already.getCapacity() - already.getFluidAmount();
                    if (need > 0) {
                        for (EnumFacing facing : getIOFacing()) {
                            TileEntity te = component.getWorld().getTileEntity(component.getPos().offset(facing));
                            if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                                if (handler != null) {
                                    if (fluidStack != null) {
                                        if (already.fill(handler.drain(new FluidStack(fluidStack.getFluid(), need), true), true) > 0) {
                                            return;
                                        }
                                    } else {
                                        if (already.fill(handler.drain(need, true), true) > 0) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (capabilityIO[i] == IO.OUT){
                    FluidTank already = this.handler.getTankAt(i);
                    FluidStack fluidStack = already.getFluid();
                    if (fluidStack != null && already.getFluidAmount() > 0) {
                        for (EnumFacing facing : getIOFacing()) {
                            TileEntity te = component.getWorld().getTileEntity(component.getPos().offset(facing));
                            if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                                if (handler != null) {
                                    if (already.drain(handler.fill(fluidStack.copy(), true), true) != null) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        super.update();
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot, int index) {
        super.initSettingDialog(dialog, slot, index);
        dialog.addWidget(new LabelWidget(5, 60, "tank capability (L): "));
        dialog.addWidget(new TextFieldWidget(5, 70, 100, 15, true, null, s -> tankCapability[index] = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(tankCapability[index] + ""));
    }

    @Override
    protected void addSlot() {
        super.addSlot();
        tankCapability = ArrayUtils.add(tankCapability, 1000);
    }

    @Override
    protected void removeSlot(int index) {
        super.removeSlot(index);
        tankCapability = ArrayUtils.remove(tankCapability, index);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        handler.deserializeNBT(compound.getCompoundTag("_"));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("_", handler.serializeNBT());
    }

    @Override
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        super.createUI(component, group, player);
        if (handler != null) {
            for (int i = 0; i < guiIO.length; i++) {
                group.addWidget(new TankWidget(new ProxyFluidHandler(handler.getTankAt(i), guiIO[i]), x[i], y[i], true, true));
            }
        }
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler) : null;
    }

    @Nullable
    @Override
    public <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler.inner()) : null;
    }

    private class ProxyFluidHandler implements IFluidTank, IFluidHandler {
        public FluidTank proxy;
        public IO io;

        public ProxyFluidHandler(FluidTank proxy, IO io) {
            this.proxy = proxy;
            this.io = io;
        }
        @Nullable
        @Override
        public FluidStack getFluid() {
            return proxy.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return proxy.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return proxy.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return proxy.getInfo();
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return proxy.getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (io == IO.OUT) {
                return 0;
            }
            markAsDirty();
            return proxy.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return proxy.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (io == IO.IN) {
                return null;
            }
            markAsDirty();
            return proxy.drain(maxDrain, doDrain);
        }
    }

    public class FluidTankList implements IFluidHandler, INBTSerializable<NBTTagCompound> {
        public IO[] cIOs;
        protected final List<FluidTank> fluidTanks;
        private IFluidTankProperties[] fluidTankProperties;
        private boolean inner;

        public FluidTankList(IO[] cIOs, FluidTank... fluidTanks) {
            this.fluidTanks = Arrays.asList(fluidTanks);
            this.cIOs = cIOs;
        }

        private FluidTankList(IO[] cIOs, final List<FluidTank> fluidTanks, IFluidTankProperties[] fluidTankProperties, boolean inner) {
            this.cIOs = cIOs;
            this.fluidTanks = fluidTanks;
            this.fluidTankProperties = fluidTankProperties;
            this.inner = inner;
        }

        public FluidTankList inner(){
            return new FluidTankList(cIOs, fluidTanks, fluidTankProperties, true);
        }

        public List<FluidTank> getFluidTanks() {
            return Collections.unmodifiableList(fluidTanks);
        }

        @Nonnull
        public Iterator<FluidTank> iterator() {
            return getFluidTanks().iterator();
        }

        public int getTanks() {
            return fluidTanks.size();
        }

        public FluidTank getTankAt(int index) {
            return fluidTanks.get(index);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            if (fluidTankProperties == null) {
                ArrayList<IFluidTankProperties> propertiesList = new ArrayList<>();
                for (IFluidTank fluidTank : fluidTanks) {
                    if (fluidTank instanceof IFluidHandler) {
                        IFluidHandler fluidHandler = (IFluidHandler) fluidTank;
                        propertiesList.addAll(Arrays.asList(fluidHandler.getTankProperties()));
                    }
                }
                this.fluidTankProperties = propertiesList.toArray(new IFluidTankProperties[0]);
            }
            return fluidTankProperties;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) {
                return 0;
            }
            if (doFill) markAsDirty();
            return fillTanksImpl(resource.copy(), doFill);
        }

        private int fillTanksImpl(FluidStack resource, boolean doFill) {
            int totalFilled = 0;
            for (int i = 0; i < fluidTanks.size(); i++) {
                IO io = cIOs[i];
                if ((inner ? io == IO.IN : io == IO.OUT)) {
                    continue;
                }
                FluidTank handler = fluidTanks.get(i);
                if (resource.isFluidEqual(handler.getFluid())) {
                    int filledAmount = handler.fill(resource, doFill);
                    totalFilled += filledAmount;
                    resource.amount -= filledAmount;
                    if (resource.amount == 0)
                        return totalFilled;
                }
            }
            for (int i = 0; i < fluidTanks.size(); i++) {
                IO io = cIOs[i];
                if ((inner ? io == IO.IN : io == IO.OUT)) {
                    continue;
                }
                FluidTank handler = fluidTanks.get(i);
                if (handler.getFluidAmount() == 0) {
                    int filledAmount = handler.fill(resource, doFill);
                    totalFilled += filledAmount;
                    resource.amount -= filledAmount;
                    if (resource.amount == 0)
                        return totalFilled;
                }
            }
            return totalFilled;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            if (doDrain) markAsDirty();
            resource = resource.copy();
            FluidStack totalDrained = null;
            for (int i = 0; i < fluidTanks.size(); i++) {
                IO io = cIOs[i];
                if ((inner ? io == IO.OUT : io == IO.IN)) {
                    continue;
                }
                FluidTank handler = fluidTanks.get(i);
                if (!resource.isFluidEqual(handler.getFluid())) {
                    continue;
                }
                FluidStack drain = handler.drain(resource.amount, doDrain);
                if (drain == null) {
                    continue;
                }
                if (totalDrained == null) {
                    totalDrained = drain;
                } else totalDrained.amount += drain.amount;

                resource.amount -= drain.amount;
                if (resource.amount == 0) break;
            }
            return totalDrained;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain == 0) {
                return null;
            }
            if (doDrain) markAsDirty();
            FluidStack totalDrained = null;
            for (int i = 0; i < fluidTanks.size(); i++) {
                IO io = cIOs[i];
                if ((inner ? io == IO.OUT : io == IO.IN)) {
                    continue;
                }
                FluidTank handler = fluidTanks.get(i);
                if (totalDrained == null) {
                    totalDrained = handler.drain(maxDrain, doDrain);
                    if (totalDrained != null)
                        maxDrain -= totalDrained.amount;
                } else {
                    FluidStack copy = totalDrained.copy();
                    copy.amount = maxDrain;
                    if (!copy.isFluidEqual(handler.getFluid())) continue;
                    FluidStack drain = handler.drain(copy.amount, doDrain);
                    if (drain != null) {
                        totalDrained.amount += drain.amount;
                        maxDrain -= drain.amount;
                    }
                }
                if (maxDrain <= 0) break;
            }
            return totalDrained;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound fluidInventory = new NBTTagCompound();
            fluidInventory.setInteger("TankAmount", this.getTanks());

            NBTTagList tanks = new NBTTagList();
            for (int i = 0; i < this.getTanks(); i++) {
                NBTBase writeTag;
                FluidTank fluidTank = fluidTanks.get(i);
                if (fluidTank != null) {
                    writeTag = fluidTank.writeToNBT(new NBTTagCompound());
                } else writeTag = new NBTTagCompound();

                tanks.appendTag(writeTag);
            }
            fluidInventory.setTag("Tanks", tanks);
            return fluidInventory;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            NBTTagList tanks = nbt.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < Math.min(fluidTanks.size(), nbt.getInteger("TankAmount")); i++) {
                NBTBase nbtTag = tanks.get(i);
                FluidTank fluidTank = fluidTanks.get(i);
                if (fluidTank != null) {
                    fluidTank.readFromNBT((NBTTagCompound) nbtTag);
                }
            }
        }

    }

}
