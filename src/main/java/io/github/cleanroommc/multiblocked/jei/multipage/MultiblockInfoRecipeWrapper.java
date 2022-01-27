package io.github.cleanroommc.multiblocked.jei.multipage;

import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.controller.structure.PatternWidget;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MultiblockInfoRecipeWrapper extends ModularWrapper {
    public final ControllerDefinition definition;
    private static long lastRender;
    private static ItemStack tooltipBlockStack;

    public MultiblockInfoRecipeWrapper(ControllerDefinition definition) {
        super(PatternWidget.getPatternWidget(definition), 176, 256);
        this.definition = definition;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, ((PatternWidget)widget).allItemStackInputs);
        ingredients.setOutput(VanillaTypes.ITEM, definition.getStackForm());
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        lastRender = System.currentTimeMillis();
        guiContainer.drawInfo(minecraft, mouseX, mouseY);
    }

    public static void setTooltipBlockStack(ItemStack tooltipBlockStack) {
        MultiblockInfoRecipeWrapper.tooltipBlockStack = tooltipBlockStack;
    }

    public static ItemStack getHoveredItemStack() {
        if(lastRender > System.currentTimeMillis() - 100) {
            return tooltipBlockStack;
        }
        return null;
    }

}
