package io.github.cleanroommc.multiblocked.jei.multipage;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.gui.modular.IUIHolder;
import io.github.cleanroommc.multiblocked.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.util.ModularUIBuilder;
import io.github.cleanroommc.multiblocked.gui.widget.imp.controller.structure.PatternWidget;
import io.github.cleanroommc.multiblocked.jei.JEIModularUIGuiContainer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.io.IOException;

public class MultiblockInfoRecipeWrapper implements IRecipeWrapper {
    private final PatternWidget patternWidget;
    private final ControllerDefinition definition;
    private final JEIModularUIGuiContainer guiContainer;
    private static long lastRender;
    private static ItemStack tooltipBlockStack;
    private RecipeLayout layout;

    public MultiblockInfoRecipeWrapper(ControllerDefinition definition) {
        patternWidget = PatternWidget.getPatternWidget(definition);
        this.definition = definition;
        ModularUI gui = new ModularUIBuilder(IGuiTexture.EMPTY, 176, 256)
                .widget(patternWidget)
                .build(IUIHolder.EMPTY, Minecraft.getMinecraft().player);
        gui.initWidgets();
        guiContainer = new JEIModularUIGuiContainer(gui);
    }

    public void setRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper) {
        this.layout = layout;
        guiContainer.setRecipeLayout(layout);
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, patternWidget.allItemStackInputs);
        ingredients.setOutput(VanillaTypes.ITEM, definition.getStackForm());
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        lastRender = System.currentTimeMillis();
        guiContainer.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
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

    public void handleMouseInput() {
        try {
            guiContainer.handleMouseInput();
        } catch (IOException e) {
            Multiblocked.LOGGER.error(e);
        }
    }
}
