package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.trait.MultiCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.FluidMultiblockCapability;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
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
import java.util.stream.Collectors;

public class FluidCapabilityTrait extends MultiCapabilityTrait {
    private static final String EMPTY_TEX = "multiblocked:textures/void.png";
    private FluidTankList handler;
    private int[] tankCapability;
    private FluidStack[][] validFluids;
    protected int[] width;
    protected int[] height;
    protected String[] texture;
    protected ProgressTexture.FillDirection[] fillDirection;

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
        width = new int[size];
        height = new int[size];
        texture = new String[size];
        fillDirection = new ProgressTexture.FillDirection[size];
        validFluids =  new FluidStack[size][];
        int i = 0;
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            tankCapability[i] = JsonUtils.getInt(jsonObject, "tC", 1000);
            width[i] = JsonUtils.getInt(jsonObject, "w", 18);
            height[i] = JsonUtils.getInt(jsonObject, "h", 18);
            texture[i] = JsonUtils.getString(jsonObject, "tex", EMPTY_TEX);
            fillDirection[i] = JsonUtil.getEnumOr(jsonObject, "fillDir", ProgressTexture.FillDirection.class, ProgressTexture.FillDirection.ALWAYS_FULL);
            if (jsonObject.has("valid")) {
                validFluids[i] = new FluidStack[0];
                for (JsonElement fluid : jsonObject.get("valid").getAsJsonArray()) {
                    validFluids[i] = ArrayUtils.add(validFluids[i], Multiblocked.GSON.fromJson(fluid.getAsString(), FluidStack.class));
                }
            }
            i++;
        }
        FluidTank[] fluidTanks = new FluidTank[tankCapability.length];

        for (int j = 0; j < tankCapability.length; j++) {
            final FluidStack[] fluids = validFluids[j];
            fluidTanks[j] = new FluidTank(tankCapability[j]) {
                @Override
                public boolean canFillFluidType(FluidStack fluid) {
                    if (fluids != null) {
                        for (FluidStack fluidStack : fluids) {
                            if (fluidStack.isFluidEqual(fluid)) return super.canFillFluidType(fluid);
                        }
                        return false;
                    }
                    return super.canFillFluidType(fluid);
                }

                @Override
                public boolean canDrainFluidType(@Nullable FluidStack fluid) {
                    if (fluids != null) {
                        for (FluidStack fluidStack : fluids) {
                            if (fluidStack.isFluidEqual(fluid)) return super.canDrainFluidType(fluid);
                        }
                        return false;
                    }
                    return super.canDrainFluidType(fluid);
                }
            };


        }
        handler = new FluidTankList(capabilityIO, Arrays.asList(fluidTanks), this.slotName, null);
    }

    @Override
    public JsonElement deserialize() {
        JsonArray jsonArray = super.deserialize().getAsJsonArray();
        for (int i = 0; i < capabilityIO.length; i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            jsonObject.addProperty("tC", tankCapability[i]);
            jsonObject.addProperty("w", width[i]);
            jsonObject.addProperty("h", height[i]);
            if (!texture[i].equals(EMPTY_TEX)) jsonObject.addProperty("tex", texture[i]);
            jsonObject.addProperty("fillDir", fillDirection[i].name());
            if (validFluids[i] != null) {
                JsonArray fluids = new JsonArray();
                for (FluidStack fluid : validFluids[i]) {
                    fluids.add(Multiblocked.GSON.toJson(fluid));
                }
                jsonObject.add("valid", fluids);
            }
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
    protected void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        for (int i = 0; i < guiIO.length; i++) {
            int finalI = i;
            ButtonWidget setting = (ButtonWidget) new ButtonWidget(width[finalI] - 8, 0, 8, 8, new ResourceTexture("multiblocked:textures/gui/option.png"), null).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings");
            ImageWidget imageWidget = new ImageWidget(0, 0,  width[finalI], height[finalI], new GuiTextureGroup(createAutoProgressTexture(finalI), new ColorBorderTexture(1, getColorByIO(capabilityIO[finalI]))));
            setting.setVisible(false);
            DraggableWidgetGroup slot = new DraggableWidgetGroup(x[finalI], y[finalI], width[finalI], height[finalI]);
            slot.setOnSelected(w -> setting.setVisible(true));
            slot.setOnUnSelected(w -> setting.setVisible(false));
            slot.addWidget(imageWidget);
            slot.addWidget(setting);
            slot.setOnEndDrag(b -> {
                x[finalI] = b.getSelfPosition().x;
                y[finalI] = b.getSelfPosition().y;
            });
            dragGroup.addWidget(slot);

            setting.setOnPressCallback(cd2 -> {
                DialogWidget dialog = new DialogWidget(dragGroup, true);
                dialog.addWidget(new ImageWidget(0, 0, 176, 256, new ColorRectTexture(0xaf000000)));
                dialog.addWidget(new ButtonWidget(5, 5, 85, 20, new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.trait.remove_slot")), cd3 -> {
                    removeSlot(finalI);
                    refreshSlots(dragGroup);
                    dialog.close();
                }).setHoverBorderTexture(1, -1));
                initSettingDialog(dialog, slot, finalI);
            });
        }
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot, final int index) {
        super.initSettingDialog(dialog, slot, index);
        dialog.addWidget(new LabelWidget(5, 60, "multiblocked.gui.label.tank_capability"));
        dialog.addWidget(new TextFieldWidget(5, 70, 100, 15, true, null, s -> tankCapability[index] = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(tankCapability[index] + ""));
        WidgetGroup widget = new WidgetGroup(5, 103, 200, 200);
        dialog.addWidget(widget);
        dialog.addWidget(GuiUtils.createBoolSwitch(5, 90, "Fluid Filter", "", validFluids[index] != null, result->{
            if (result) {
                validFluids[index] = new FluidStack[0];
                widget.addWidget(GuiUtils.createFluidStackSelector(dialog, 0,0, "Valid Fluids", Arrays.stream(validFluids[index]).collect(Collectors.toList()), list -> validFluids[index] = list.toArray(new FluidStack[0])));
            } else {
                widget.clearAllWidgets();
                validFluids[index] = null;
            }
        }));
        if (validFluids[index] != null) {
            widget.addWidget(GuiUtils.createFluidStackSelector(dialog, 0,0, "Valid Fluids", Arrays.stream(validFluids[index]).collect(Collectors.toList()), list -> validFluids[index] = list.toArray(new FluidStack[0])));
        }


        // progress bar
        WidgetGroup group = new WidgetGroup(0, 180, 50, 50);
        dialog.addWidget(group);
        ImageWidget imageWidget = (ImageWidget) slot.widgets.get(0);
        ButtonWidget setting = (ButtonWidget) slot.widgets.get(1);
        ButtonWidget imageSelector = (ButtonWidget) new ButtonWidget(60, 45, width[index] , height[index] , new GuiTextureGroup(new ColorBorderTexture(1, -1), createAutoProgressTexture(index)), null)
                .setHoverTooltip("multiblocked.gui.tips.select_image");
        group.addWidget(new TextFieldWidget(5, 25, 50, 15, null, s -> {
            width[index] = Integer.parseInt(s);
            Size size = new Size(width[index], height[index]);
            slot.setSize(size);
            imageWidget.setSize(size);
            imageSelector.setSize(size);
            setting.setSelfPosition(new Position(width[index] - 8, 0));
        }).setCurrentString(width[index] + "").setNumbersOnly(1, 180).setHoverTooltip("multiblocked.gui.trait.set_width"));
        group.addWidget(new TextFieldWidget(5, 45, 50, 15, null, s -> {
            height[index]  = Integer.parseInt(s);
            Size size = new Size(width[index], height[index]);
            slot.setSize(size);
            imageWidget.setSize(size);
            imageSelector.setSize(size);
            setting.setSelfPosition(new Position(width[index] - 8, 0));
        }).setCurrentString(height[index] + "").setNumbersOnly(1, 180).setHoverTooltip("multiblocked.gui.trait.set_height"));

        group.addWidget(imageSelector);
        group.addWidget(new SelectorWidget(60, 25, 90, 15, Arrays.stream(ProgressTexture.FillDirection.values()).map(Enum::name).collect(Collectors.toList()), -1)
                .setIsUp(true)
                .setValue(fillDirection[index].name())
                .setOnChanged(io -> {
                    fillDirection[index] = ProgressTexture.FillDirection.valueOf(io);
                    ResourceTexture autoProgressTexture = createAutoProgressTexture(index);
                    imageSelector.setButtonTexture(new GuiTextureGroup(new ColorBorderTexture(1, -1), autoProgressTexture));
                    imageWidget.setImage(new GuiTextureGroup(autoProgressTexture, new ColorBorderTexture(1, getColorByIO(capabilityIO[index]))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("multiblocked.gui.trait.fill_direction"));
        imageSelector.setOnPressCallback(cd -> new ResourceTextureWidget((WidgetGroup) dialog.parent.getGui().guiWidgets.get(0), texture1 -> {
            if (texture1 != null) {
                texture[index] = texture1.imageLocation.toString();
                ResourceTexture autoProgressTexture = createAutoProgressTexture(index);
                imageSelector.setButtonTexture(new GuiTextureGroup(new ColorBorderTexture(1, -1), autoProgressTexture));
                imageWidget.setImage(new GuiTextureGroup(autoProgressTexture, new ColorBorderTexture(1, getColorByIO(capabilityIO[index]))));
            }
        }));
    }

    @Override
    protected void updateImageWidget(ImageWidget imageWidget, int index) {
        imageWidget.setImage(new GuiTextureGroup(createAutoProgressTexture(index), new ColorBorderTexture(1, getColorByIO(capabilityIO[index]))));
    }

    private ResourceTexture createAutoProgressTexture(int index) {
        return new ResourceTexture(this.texture[index]);
    }

    @Override
    protected void addSlot() {
        super.addSlot();
        tankCapability = ArrayUtils.add(tankCapability, 1000);
        validFluids = ArrayUtils.add(validFluids, null);
        width = ArrayUtils.add(width, 18);
        height = ArrayUtils.add(height, 18);
        texture = ArrayUtils.add(texture, EMPTY_TEX);
        fillDirection = ArrayUtils.add(fillDirection, ProgressTexture.FillDirection.ALWAYS_FULL);
    }

    @Override
    protected void removeSlot(int index) {
        super.removeSlot(index);
        tankCapability = ArrayUtils.remove(tankCapability, index);
        validFluids = ArrayUtils.remove(validFluids, index);
        width = ArrayUtils.remove(width, index);
        height = ArrayUtils.remove(height, index);
        texture = ArrayUtils.remove(texture, index);
        fillDirection = ArrayUtils.remove(fillDirection, index);
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
                group.addWidget(new TankWidget(new ProxyFluidHandler(handler.getTankAt(i), guiIO[i]), x[i], y[i], width[i], height[i], true, true).setOverlay(new ResourceTexture(texture[i])).setFillDirection(fillDirection[i]).setShowTips(fillDirection[i] == ProgressTexture.FillDirection.ALWAYS_FULL));
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
    public <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing, @Nullable String slotName) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidTankList(getRealMbdIO(), handler.fluidTanks, this.slotName, slotName)) : null;
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
        public String[] slotNames;
        public String slotName;

        private FluidTankList(IO[] cIOs, final List<FluidTank> fluidTanks, String[] slotNames, @Nullable String slotName) {
            this.cIOs = cIOs;
            this.fluidTanks = fluidTanks;
            this.slotNames = slotNames;
            this.slotName = slotName;
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
                if (io == IO.OUT) {
                    continue;
                }
                if (slotName != null && !slotNames[i].equals(slotName)) {
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
                if (io == IO.OUT) {
                    continue;
                }
                if (slotName != null && !slotNames[i].equals(slotName)) {
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
                if (io == IO.IN) {
                    continue;
                }
                if (slotName != null && !slotNames[i].equals(slotName)) {
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
                if (io == IO.IN) {
                    continue;
                }
                if (slotName != null && !slotNames[i].equals(slotName)) {
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
