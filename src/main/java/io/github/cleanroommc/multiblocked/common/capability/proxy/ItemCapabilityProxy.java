package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemCapabilityProxy extends CapabilityProxy<ItemsIngredient> {

    public ItemCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IItemHandler getCapability() {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    public ItemsIngredient copyInner(ItemsIngredient content) {
        return content.copy();
    }

    @Override
    protected List<ItemsIngredient> matchingRecipeInner(IO io, Recipe recipe, List<ItemsIngredient> left) {
        IItemHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<ItemsIngredient> iterator = left.iterator();
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                ItemsIngredient ingredient = iterator.next();
                for (int i = 0; i < capability.getSlots(); i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    if (ingredient.match(itemStack)) {
                        ItemStack extracted = capability.extractItem(i, itemStack.getCount(), true);
                        ingredient.setAmount(ingredient.getAmount() - extracted.getCount());
                    }
                }
                if (ingredient.getAmount() <= 0) iterator.remove();
            }
        } else if (io == IO.OUT){
            while (iterator.hasNext()) {
                ItemsIngredient ingredient = iterator.next();
                ItemStack output = ingredient.getOutputStack();
                for (int i = 0; i < capability.getSlots(); i++) {
                    output = capability.insertItem(i, output, true);
                    if (output.isEmpty()) break;
                }
                if (output.isEmpty()) iterator.remove();
                else ingredient.setAmount(output.getCount());
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    protected List<ItemsIngredient> handleRecipeInputInner(IO io, Recipe recipe, List<ItemsIngredient> left) {
        IItemHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<ItemsIngredient> iterator = left.iterator();
        while (iterator.hasNext()) {
            ItemsIngredient ingredient = iterator.next();
            for (int i = 0; i < capability.getSlots(); i++) {
                ItemStack itemStack = capability.getStackInSlot(i);
                if (ingredient.match(itemStack)) {
                    ItemStack extracted = capability.extractItem(i, itemStack.getCount(), false);
                    ingredient.setAmount(ingredient.getAmount() - extracted.getCount());
                }
            }
            if (ingredient.getAmount() <= 0) iterator.remove();
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    protected List<ItemsIngredient> handleRecipeOutputInner(IO io, Recipe recipe, List<ItemsIngredient> left) {
        IItemHandler capability = getCapability();
        if (capability == null) return left;
        Iterator<ItemsIngredient> iterator = left.iterator();
        while (iterator.hasNext()) {
            ItemsIngredient ingredient = iterator.next();
            ItemStack output = ingredient.getOutputStack();
            for (int i = 0; i < capability.getSlots(); i++) {
                output = capability.insertItem(i, output, false);
                if (output.isEmpty()) break;
            }
            if (output.isEmpty()) iterator.remove();
            else ingredient.setAmount(output.getCount());
        }
        return left.isEmpty() ? null : left;
    }

}
