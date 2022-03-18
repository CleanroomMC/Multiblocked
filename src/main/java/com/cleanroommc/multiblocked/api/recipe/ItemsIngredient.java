package com.cleanroommc.multiblocked.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class ItemsIngredient extends Ingredient {
    private int amount;

    public ItemsIngredient(ItemStack... matchingStacks) {
        super(matchingStacks);
        amount = matchingStacks[0].getCount();
    }

    public ItemsIngredient(int amount, ItemStack... matchingStacks) {
        super(matchingStacks);
        this.amount = amount;
    }

    public ItemStack getOutputStack() {
        ItemStack output = matchingStacks[0].copy();
        output.setCount(amount);
        return output;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean match(ItemStack itemStack) {
        for (ItemStack matchingStack : getMatchingStacks()) {
            if (matchingStack.isItemEqual(itemStack)) return true;
        }
        return false;
    }

    public ItemsIngredient copy() {
        return new ItemsIngredient(amount, getMatchingStacks());
    }

    @Override
    public int hashCode() {
        int hash = amount;
        for (ItemStack stack : getMatchingStacks()) {
            ResourceLocation name = stack.getItem().getRegistryName();
            hash += name == null ? 0 : name.hashCode();
        }
        return hash;
    }
}
