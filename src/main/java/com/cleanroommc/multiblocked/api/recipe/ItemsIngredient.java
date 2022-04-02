package com.cleanroommc.multiblocked.api.recipe;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemsIngredient extends Ingredient {
    private int amount;
    private String ore;
    private NonNullList<ItemStack> ores;
    private IntList itemIds = null;
    private ItemStack[] array = null;
    private int lastSizeA = -1, lastSizeL = -1;

    public ItemsIngredient(int amount, String ore) {
        super(0);
        this.amount = amount;
        this.ore = ore;
        ores = OreDictionary.getOres(ore);
    }

    public ItemsIngredient(ItemStack... matchingStacks) {
        super(matchingStacks);
        amount = matchingStacks[0].getCount();
    }

    public ItemsIngredient(int amount, ItemStack... matchingStacks) {
        super(matchingStacks);
        this.amount = amount;
    }

    public String getOreDict() {
        return ore;
    }

    public boolean isOre() {
        return ore != null;
    }

    public ItemStack getOutputStack() {
        ItemStack output = matchingStacks[0].copy();
        output.setCount(amount);
        return output;
    }

    @Override
    @Nonnull
    public ItemStack[] getMatchingStacks() {
        if (ores == null) return super.getMatchingStacks();
        if (array == null || this.lastSizeA != ores.size()) {
            NonNullList<ItemStack> lst = NonNullList.create();
            for (ItemStack itemstack : this.ores) {
                if (itemstack.getMetadata() == OreDictionary.WILDCARD_VALUE)
                    itemstack.getItem().getSubItems(CreativeTabs.SEARCH, lst);
                else
                    lst.add(itemstack);
            }
            this.array = lst.toArray(new ItemStack[0]);
            this.lastSizeA = ores.size();
        }
        return this.array;
    }


    @Override
    @Nonnull
    public IntList getValidItemStacksPacked() {
        if (ores == null) return super.getValidItemStacksPacked();
        if (this.itemIds == null || this.lastSizeL != ores.size()) {
            this.itemIds = new IntArrayList(this.ores.size());
            for (ItemStack itemstack : this.ores) {
                if (itemstack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    NonNullList<ItemStack> lst = NonNullList.create();
                    itemstack.getItem().getSubItems(CreativeTabs.SEARCH, lst);
                    for (ItemStack item : lst) {
                        this.itemIds.add(RecipeItemHelper.pack(item));
                    }
                } else {
                    this.itemIds.add(RecipeItemHelper.pack(itemstack));
                }
            }

            this.itemIds.sort(IntComparators.NATURAL_COMPARATOR);
            this.lastSizeL = ores.size();
        }

        return this.itemIds;
    }


    @Override
    public boolean apply(@Nullable ItemStack input) {
        if (ores == null) return super.apply(input);
        if (input == null) {
            return false;
        }
        for (ItemStack target : this.ores) {
            if (OreDictionary.itemMatches(target, input, false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void invalidate() {
        if (ores == null) {
            super.invalidate();
        }
        this.itemIds = null;
        this.array = null;
    }

    @Override
    public boolean isSimple() {
        return ores != null || super.isSimple();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ItemsIngredient copy() {
        return ores == null ? new ItemsIngredient(amount, matchingStacks) : new ItemsIngredient(amount, ore);
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
