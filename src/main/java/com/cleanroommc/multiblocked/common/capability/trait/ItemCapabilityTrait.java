package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.cleanroommc.multiblocked.util.Position;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemCapabilityTrait extends CapabilityTrait {
    private ItemStackHandler handler;
    private IO[] capabilityIO;
    private IO[] guiIO;
    private int[] x;
    private int[] y;

    public ItemCapabilityTrait() {
        super(ItemMultiblockCapability.CAP);
    }

    @Override
    public void init(JsonObject jsonObject) {
        super.init(jsonObject);
        int size = JsonUtils.getInt(jsonObject, "size", 0);
        handler = new ItemStackHandler(size);
        capabilityIO = JsonUtil.getEnumArray(jsonObject.get("capabilityIO"), IO.class);
        guiIO = JsonUtil.getEnumArray(jsonObject.get("guiIO"), IO.class);
        x = JsonUtil.getIntArray(jsonObject.get("x"));
        y = JsonUtil.getIntArray(jsonObject.get("y"));
    }

    @Override
    public Function<JsonObject, JsonObject> getConfigurator(JsonObject original, WidgetGroup parentDialog) {
        Function<JsonObject, JsonObject> parent = super.getConfigurator(original, parentDialog);
        WidgetGroup group = new WidgetGroup((384 - 176) / 2, 0, 176, 256);
        parentDialog.addWidget(group);
        DraggableScrollableWidgetGroup dragGroup = new DraggableScrollableWidgetGroup(0, 0, 176, 256);
        group.addWidget(dragGroup);
        List<DraggableWidgetGroup> buttons = new ArrayList<>();
        Map<DraggableWidgetGroup, IO> capabilityIO = new HashMap<>();
        Map<DraggableWidgetGroup, IO> guiIO = new HashMap<>();

        Supplier<DraggableWidgetGroup> addSlot = () -> {
            ButtonWidget setting = new ButtonWidget(18, 4, 10, 10, new ResourceTexture("multiblocked:textures/gui/option.png"), null);
            setting.setVisible(false);
            DraggableWidgetGroup button = new DraggableWidgetGroup(5, 5, 18, 18);
            button.setOnSelected(w -> setting.setVisible(true));
            button.setOnUnSelected(w -> setting.setVisible(false));
            button.addWidget(setting);
            button.addWidget(new ImageWidget(0, 0, 18, 18, new GuiTextureGroup(new ColorRectTexture(0x4f000000), new ItemStackTexture(Items.PAPER, Items.IRON_INGOT, Items.SKULL))));
            buttons.add(button);
            capabilityIO.put(button, IO.BOTH);
            guiIO.put(button, IO.BOTH);
            dragGroup.addWidget(button);
            setting.setOnPressCallback(cd2 -> {
                DialogWidget dialog = new DialogWidget(group, true);
                dialog.addWidget(new ImageWidget(0, 0, 176, 256, new ColorRectTexture(0xaf000000)));
                dialog.addWidget(new ButtonWidget(5, 5, 100, 20, new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("remove slot")), cd3 -> {
                    buttons.remove(button);
                    capabilityIO.remove(button);
                    guiIO.remove(button);
                    dragGroup.waitToRemoved(button);
                    dialog.close();
                }));
                dialog.addWidget(new SelectorWidget(5, 30, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                        .setValue(capabilityIO.get(button).name())
                        .setOnChanged(io-> capabilityIO.put(button, IO.valueOf(io)))
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(new ColorRectTexture(0xffaaaaaa))
                        .setHoverTooltip("Capability IO"));
                dialog.addWidget(new SelectorWidget(50, 30, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                        .setValue(guiIO.get(button).name())
                        .setOnChanged(io-> guiIO.put(button, IO.valueOf(io)))
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(new ColorRectTexture(0xffaaaaaa))
                        .setHoverTooltip("Gui IO"));
            });
            return button;
        };

        if (original != null) {
            init(original);
            for (int i = 0; i < handler.getSlots(); i++) {
                DraggableWidgetGroup button = addSlot.get();
                button.setSelfPosition(new Position(this.x[i], this.y[i]));
                capabilityIO.put(button, this.capabilityIO[i]);
                guiIO.put(button, this.guiIO[i]);
            }
        }

        group.addWidget(new ButtonWidget(-20,35, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd->addSlot.get()));
        return (jsonObject) -> {
            parent.apply(jsonObject);
            jsonObject.addProperty("size", buttons.size());
            jsonObject.add("capabilityIO", JsonUtil.setEnumArray(buttons.stream().map(capabilityIO::get).toArray(IO[]::new), IO.class));
            jsonObject.add("guiIO", JsonUtil.setEnumArray(buttons.stream().map(guiIO::get).toArray(IO[]::new), IO.class));
            jsonObject.add("x", JsonUtil.setIntArray(buttons.stream().mapToInt(button->button.getSelfPosition().x).toArray()));
            jsonObject.add("y", JsonUtil.setIntArray(buttons.stream().mapToInt(button->button.getSelfPosition().y).toArray()));
            return jsonObject;
        };
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
    public void createUI(WidgetGroup group, EntityPlayer player) {
        super.createUI(group, player);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                group.addWidget(new SlotWidget(new ProxyItemHandler(handler, guiIO), i, x[i], y[i], guiIO[i] == IO.BOTH || guiIO[i] == IO.OUT, guiIO[i] == IO.BOTH || guiIO[i] == IO.IN));
            }
        }
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ProxyItemHandler(handler, capabilityIO)) : null;
    }

    static class ProxyItemHandler implements IItemHandler, IItemHandlerModifiable {
        public ItemStackHandler proxy;
        public IO[] ios;

        public ProxyItemHandler(ItemStackHandler proxy, IO[] ios) {
            this.proxy = proxy;
            this.ios = ios;
        }

        @Override
        public int getSlots() {
            return proxy.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return proxy.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            IO io = ios[slot];
            if (io == IO.BOTH || io == IO.IN) {
                return proxy.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IO io = ios[slot];
            if (io == IO.BOTH || io == IO.OUT) {
                return proxy.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return proxy.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return proxy.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            proxy.setStackInSlot(slot, stack);
        }
    }

}
