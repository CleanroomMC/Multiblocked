package io.github.cleanroommc.multiblocked.jei;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoWrapper;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class MultiblockInfoRecipeFocusShower implements IShowsRecipeFocuses {
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        Object hover = MultiblockInfoWrapper.getFocus();
        if (hover != null) {
            if (hover instanceof ItemStack && ((ItemStack) hover).getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) ((ItemStack) hover).getItem()).getBlock();
                if (block instanceof BlockComponent) {
                    if (((BlockComponent) block).definition.baseRenderer instanceof CycleBlockStateRenderer) {
                        CycleBlockStateRenderer renderer = ((CycleBlockStateRenderer) ((BlockComponent) block).definition.baseRenderer);
                        IBlockState blockState = renderer.states[Math.abs(renderer.index) % renderer.states.length];
                        hover = new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState));
                    }
                }
            }
            return ClickedIngredient.create(hover, null);
        }
        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
