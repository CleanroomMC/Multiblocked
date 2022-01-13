package io.github.cleanroommc.multiblocked.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class ItemsIngredient extends Ingredient {
    private int amount = 0;

    public ItemsIngredient(ItemStack... matchingStacks) {
        super(matchingStacks);
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
            if (matchingStack.isItemEqual(matchingStack)) return true;
        }
        return false;
    }

    public ItemsIngredient copy() {
        return new ItemsIngredient(amount, getMatchingStacks());
    }

}
