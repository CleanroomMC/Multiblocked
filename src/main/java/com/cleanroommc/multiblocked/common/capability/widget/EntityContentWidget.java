package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.EntityIngredient;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemStackHandler;

public class EntityContentWidget extends ContentWidget<EntityIngredient> {
    protected ItemStackHandler itemHandler;

    @Override
    protected void onContentUpdate() {
        if (itemHandler == null) {
            itemHandler = new ItemStackHandler();
            addWidget(new SlotWidget(itemHandler, 0, 1, 1, false, false).setDrawOverlay(false).setOnAddedTooltips((s, l) -> {
                if (chance < 1) {
                    l.add(chance == 0 ? LocalizationUtils.format("multiblocked.gui.content.chance_0") : LocalizationUtils.format("multiblocked.gui.content.chance_1", String.format("%.1f", chance * 100)));
                }
                if (perTick) {
                    l.add(LocalizationUtils.format("multiblocked.gui.content.per_tick"));
                }
            }));
        }
        if (content.isEntityItem()) {
            itemHandler.setStackInSlot(0, content.getEntityItem());
        } else {
            EntityList.EntityEggInfo egg = content.type.getEgg();
            ItemStack itemStack;
            if (egg == null) {
                itemStack = ItemStack.EMPTY;
            } else {
                itemStack = new ItemStack(Items.SPAWN_EGG);
                ItemMonsterPlacer.applyEntityIdToItemStack(itemStack, egg.spawnedID);
            }
            itemHandler.setStackInSlot(0, itemStack);
        }
    }

    @Override
    public EntityIngredient getJEIContent(Object content) {
        EntityIngredient ingredient = new EntityIngredient();
        if (content instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) content;
            if (itemStack.getItem() instanceof ItemMonsterPlacer) {
                ResourceLocation id = ItemMonsterPlacer.getNamedIdFrom(itemStack);
                ingredient.type = ForgeRegistries.ENTITIES.getValue(id);
            } else {
                ingredient.type = EntityIngredient.ITEM;
                ingredient.tag = new NBTTagCompound();
                ingredient.tag.setTag("Item", itemStack.writeToNBT(new NBTTagCompound()));
            }
        }
        return ingredient;
    }

    @Override
    public Object getJEIIngredient(EntityIngredient content) {
        ItemStack item = itemHandler.getStackInSlot(0);
        return item.isEmpty() ? null : item;
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int y = 25;
        TextFieldWidget type, tag;
        dialog.addWidget(new LabelWidget(5, y + 3, "multiblocked.gui.label.entity_type"));
        dialog.addWidget(type = new TextFieldWidget(125 - 60, y, 60, 15,  null, string -> {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(string));
            content.type = entry == null ? content.type : entry;
            onContentUpdate();
        }));
        dialog.addWidget(new LabelWidget(5, y + 23, "multiblocked.gui.label.entity_tag"));
        dialog.addWidget(tag = new TextFieldWidget(125 - 60, y + 20, 60, 15,  null, string -> {
            try {
                content.tag = JsonToNBT.getTagFromJson(string);
                onContentUpdate();
            } catch (NBTException ignored) {
            }
        }));
        if (content != null) {
            type.setCurrentString(content.type.getRegistryName().toString());
            if (content.tag != null) {
                tag.setCurrentString(content.tag.toString());
            }
        }
    }
}
