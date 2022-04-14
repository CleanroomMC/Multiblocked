package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.SingleCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.FEMultiblockCapability;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FECapabilityTrait extends SingleCapabilityTrait {
    private EnergyStorage handler;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;
    protected int width;
    protected int height;
    protected String texture;

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
        width = JsonUtils.getInt(jsonObject, "width", 60);
        height = JsonUtils.getInt(jsonObject, "height", 18);
        texture = JsonUtils.getString(jsonObject, "texture", "multiblocked:textures/gui/energy_bar.png");
        handler = new EnergyStorage(capacity, maxReceive, maxExtract);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        jsonObject.addProperty("maxReceive", maxReceive);
        jsonObject.addProperty("maxExtract", maxExtract);
        jsonObject.addProperty("width", width);
        jsonObject.addProperty("height", height);
        jsonObject.addProperty("texture", texture);
        return jsonObject;
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
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        super.createUI(component, group, player);
        group.addWidget(new ProgressWidget(
                () -> handler.getEnergyStored() * 1f / handler.getMaxEnergyStored(),
                x, y, width, height,
                new ResourceTexture(texture))
                .setDynamicHoverTips(p -> String.format("FE Stored: %d / %d", (int)(handler.getMaxEnergyStored() * p), handler.getMaxEnergyStored())));
    }

    protected void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        ButtonWidget setting = (ButtonWidget) new ButtonWidget(width - 8, 0, 8, 8, new ResourceTexture("multiblocked:textures/gui/option.png"), null).setHoverBorderTexture(1, -1).setHoverTooltip("settings");
        ImageWidget imageWidget = new ImageWidget(0, 0, width, height, new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO)), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
        setting.setVisible(false);
        DraggableWidgetGroup slot = new DraggableWidgetGroup(x, y, width, height);
        slot.setOnSelected(w -> setting.setVisible(true));
        slot.setOnUnSelected(w -> setting.setVisible(false));
        slot.addWidget(imageWidget);
        slot.addWidget(setting);
        slot.setOnEndDrag(b -> {
            x = b.getSelfPosition().x;
            y = b.getSelfPosition().y;
        });
        dragGroup.addWidget(slot);

        setting.setOnPressCallback(cd2 -> {
            DialogWidget dialog = new DialogWidget(dragGroup, true);
            dialog.addWidget(new ImageWidget(0, 0, 176, 256, new ColorRectTexture(0xaf000000)));
            initSettingDialog(dialog, slot);
        });
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        ImageWidget imageWidget = (ImageWidget) slot.widgets.get(0);
        ButtonWidget setting = (ButtonWidget) slot.widgets.get(1);
        ButtonWidget imageSelector = (ButtonWidget) new ButtonWidget(5, 105, width, height, new GuiTextureGroup(new ColorBorderTexture(1, -1), new ResourceTexture(texture)), null)
                .setHoverTooltip("select a image");
        dialog.addWidget(new SelectorWidget(5, 5, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(
                Collectors.toList()), -1)
                .setValue(capabilityIO.name())
                .setOnChanged(io-> {
                    capabilityIO = IO.valueOf(io);
                    imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO)), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("Capability IO (e.g., pipe interaction)"));
        dialog.addWidget(new TextFieldWidget(5, 25, 50, 15, true, null, s -> {
            width = Integer.parseInt(s);
            Size size = new Size(width, height);
            slot.setSize(size);
            imageWidget.setSize(size);
            imageSelector.setSize(size);
            setting.setSelfPosition(new Position(width - 8, 0));
        }).setCurrentString(width + "").setNumbersOnly(1, 180).setHoverTooltip("set width"));
        dialog.addWidget(new TextFieldWidget(60, 25, 50, 15, true, null, s -> {
            height = Integer.parseInt(s);
            Size size = new Size(width, height);
            slot.setSize(size);
            imageWidget.setSize(size);
            imageSelector.setSize(size);
            setting.setSelfPosition(new Position(width - 8, 0));
        }).setCurrentString(height + "").setNumbersOnly(1, 180).setHoverTooltip("set height"));

        dialog.addWidget(new TextFieldWidget(5, 45, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(capacity + "")
                .setHoverTooltip("capability (RF)"));

        dialog.addWidget(new TextFieldWidget(5, 65, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(maxReceive + "")
                .setHoverTooltip("maxReceive (RF/packet)"));

        dialog.addWidget(new TextFieldWidget(5, 85, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(maxExtract + "")
                .setHoverTooltip("maxExtract (RF/packet)"));

        dialog.addWidget(imageSelector);
        imageSelector.setOnPressCallback(cd -> new ResourceTextureWidget((WidgetGroup) dialog.parent.getGui().guiWidgets.get(0), texture1 -> {
            imageSelector.setButtonTexture(new GuiTextureGroup(new ColorBorderTexture(1, -1), texture1));
            texture = texture1.imageLocation.toString();
        }));
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

    private static class ProxyEnergyStorage implements IEnergyStorage {
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
                return proxy.receiveEnergy(maxReceive, simulate);
            }
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (io == IO.BOTH || (inner ? io == IO.IN : io == IO.OUT)) {
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
