package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemCapabilityProxy extends CapabilityProxy<ItemsIngredient> {

    public ItemCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IItemHandler getCapability() {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    protected ItemsIngredient matchingRecipeInner(IO io, Recipe recipe, ItemsIngredient left) {
        IItemHandler capability = getCapability();
        if (capability == null) return left;
        if (io == IO.IN) {
            for (int i = 0; i < capability.getSlots(); i++) {
                ItemStack itemStack = capability.getStackInSlot(i);
                if (left.match(itemStack)) {
                    int amount = left.getAmount();
                    ItemStack extracted = capability.extractItem(i, itemStack.getCount(), true);
                    left = left.copy();
                    left.setAmount(amount - extracted.getCount());
                }
                if (left.getAmount() == 0) return null;
            }
            return left;
        } else if (io == IO.OUT){
            ItemStack output = left.getOutputStack();
            for (int i = 0; i < capability.getSlots(); i++) {
                output = capability.insertItem(i, output, true);
                if (output.isEmpty()) return null;
            }
            return new ItemsIngredient(output.getCount(), output);
        }
        return left;
    }

    @Override
    protected ItemsIngredient handleRecipeInputInner(IO io, Recipe recipe, ItemsIngredient left) {
        IItemHandler capability = getCapability();
        if (capability == null) return left;
        for (int i = 0; i < capability.getSlots(); i++) {
            ItemStack itemStack = capability.getStackInSlot(i);
            if (left.match(itemStack)) {
                int amount = left.getAmount();
                ItemStack extracted = capability.extractItem(i, itemStack.getCount(), false);
                left = left.copy();
                left.setAmount(amount - extracted.getCount());
            }
            if (left.getAmount() == 0) return null;
        }
        return left;
    }

    @Override
    protected ItemsIngredient handleRecipeOutputInner(IO io, Recipe recipe, ItemsIngredient left) {
        IItemHandler capability = getCapability();
        if (capability == null) return left;
        ItemStack output = left.getOutputStack();
        for (int i = 0; i < capability.getSlots(); i++) {
            output = capability.insertItem(i, output, false);
            if (output.isEmpty()) return null;
        }
        return new ItemsIngredient(output.getCount(), output);
    }


}
