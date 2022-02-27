package io.github.cleanroommc.multiblocked.util;


import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class CycleItemStackHandler implements IItemHandler {
    private final List<List<ItemStack>> stacks;
    private final int[] indexes;


    public CycleItemStackHandler(List<List<ItemStack>> stacks) {
        this.stacks = stacks;
        this.indexes = new int[stacks.size()];
    }

    public void update() {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = Math.abs(indexes[i] + 1) % stacks.get(i).size();
        }
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        List<ItemStack> stackList = stacks.get(i);
        return stackList == null || stackList.isEmpty() ? ItemStack.EMPTY : stackList.get(indexes[i]);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b) {
        return itemStack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }
}
