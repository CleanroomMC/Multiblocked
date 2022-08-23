package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Consumer;

public class ItemStackSelectorWidget extends WidgetGroup {
    private Consumer<ItemStack> onItemStackUpdate;
    private final IItemHandlerModifiable handler;
    private final TextFieldWidget itemField;
    private ItemStack item = ItemStack.EMPTY;

    public ItemStackSelectorWidget(WidgetGroup parent, int x, int y, int width) {
        super(x, y, width, 20);
        setClientSideWidget();
        itemField = (TextFieldWidget) new TextFieldWidget(22, 0, width - 46, 20, null, s -> {
            if (s != null && !s.isEmpty()) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
                if (item == null) {
                    item = ItemStack.EMPTY.getItem();
                }
                if (!ItemStack.areItemsEqual(item.getDefaultInstance(), this.item) || !ItemStack.areItemStackTagsEqual(item.getDefaultInstance(), this.item)) {
                    this.item = item.getDefaultInstance();
                    onUpdate();
                }
            }
        }).setHoverTooltip("multiblocked.gui.tips.item_selector");

        addWidget(new PhantomSlotWidget(handler = new ItemStackHandler(1), 0, 1, 1)
                .setClearSlotOnRightClick(true)
                .setChangeListener(() -> {
                    setItemStack(handler.getStackInSlot(0));
                    onUpdate();
                }).setBackgroundTexture(new ColorBorderTexture(1, -1)));
        addWidget(itemField);

        addWidget(new ButtonWidget(width - 21, 0, 20, 20, null, cd -> {
            if (item.isEmpty()) return;
            TextFieldWidget nbtField;
            new DialogWidget(parent, isClientSideWidget)
                    .setOnClosed(this::onUpdate)
                    .addWidget(nbtField = new TextFieldWidget(10, 10, parent.getSize().width - 50, 20, null, s -> {
                        try {
                            item.setTagCompound(JsonToNBT.getTagFromJson(s));
                            onUpdate();
                        } catch (NBTException ignored) {

                        }
                    }));
            if (item.hasTagCompound()) {
                nbtField.setCurrentString(item.getTagCompound().toString());
            }
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("NBT", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.item_tag"));
    }

    public ItemStack getItemStack() {
        return item;
    }

    public ItemStackSelectorWidget setItemStack(ItemStack itemStack) {
        item = itemStack == null ? ItemStack.EMPTY : itemStack;
        item = item.copy();
        handler.setStackInSlot(0, item);
        itemField.setCurrentString(item.getItem().getRegistryName().toString());
        return this;
    }

    public ItemStackSelectorWidget setOnItemStackUpdate(Consumer<ItemStack> onItemStackUpdate) {
        this.onItemStackUpdate = onItemStackUpdate;
        return this;
    }

    private void onUpdate() {
        handler.setStackInSlot(0, item);
        if (onItemStackUpdate != null) {
            onItemStackUpdate.accept(item);
        }
    }
}
