package io.github.cleanroommc.multiblocked.jei;

import io.github.cleanroommc.multiblocked.api.gui.ingredient.IIngredientSlot;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiContainer;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;

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
            Object hover = JEIModularUIGuiContainer.getFocus();
            if (hover != null) {
                return ClickedIngredient.create(hover, null);
            }
        }
        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
