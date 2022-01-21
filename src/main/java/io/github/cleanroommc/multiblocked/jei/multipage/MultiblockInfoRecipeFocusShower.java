package io.github.cleanroommc.multiblocked.jei.multipage;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class MultiblockInfoRecipeFocusShower implements IShowsRecipeFocuses {
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        ItemStack hover = MultiblockInfoRecipeWrapper.getHoveredItemStack();
        if (hover != null) {
            if (hover.getItem() instanceof ItemBlock) {
                if (((ItemBlock) hover.getItem()).getBlock() instanceof BlockComponent) {
                    BlockComponent block = (BlockComponent) ((ItemBlock) hover.getItem()).getBlock();
                    if (block.definition.baseRenderer instanceof CycleBlockStateRenderer) {
                        CycleBlockStateRenderer renderer = ((CycleBlockStateRenderer) block.definition.baseRenderer);
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
