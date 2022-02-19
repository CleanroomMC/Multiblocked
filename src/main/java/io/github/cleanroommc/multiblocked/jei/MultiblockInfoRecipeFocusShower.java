package io.github.cleanroommc.multiblocked.jei;

import io.github.cleanroommc.multiblocked.api.gui.ingredient.IIngredientSlot;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiContainer;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;

import java.util.List;

public class MultiblockInfoRecipeFocusShower implements IShowsRecipeFocuses {
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        if (Minecraft.getMinecraft().currentScreen instanceof ModularUIGuiContainer) {
            for (Widget widget : ((ModularUIGuiContainer) Minecraft.getMinecraft().currentScreen).modularUI.guiWidgets.values()) {
                if (widget instanceof IIngredientSlot && widget.isVisible()) {
                    Object result = ((IIngredientSlot) widget).getIngredientOverMouse(mouseX, mouseY);
                    if (result != null) {
                        return ClickedIngredient.create(result, null);
                    }
                }
            }
        } else if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui){
            List<RecipeLayout> recipeLayouts = JeiPlugin.getRecipeLayouts((RecipesGui) Minecraft.getMinecraft().currentScreen);
            if (recipeLayouts != null) {
                for (RecipeLayout recipeLayout : recipeLayouts) {
                    if (recipeLayout.isMouseOver(mouseX, mouseY)) {
                        IRecipeWrapper wrapper = JeiPlugin.getWrapper(recipeLayout);
                        if (wrapper instanceof ModularWrapper) {
                            for (Widget widget : ((ModularWrapper) wrapper).guiContainer.modularUI.guiWidgets.values()) {
                                if (widget instanceof IIngredientSlot && widget.isVisible()) {
                                    Object result = ((IIngredientSlot) widget).getIngredientOverMouse(mouseX, mouseY);
                                    if (result != null) {
                                        return ClickedIngredient.create(result, null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
