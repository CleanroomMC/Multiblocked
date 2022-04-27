package com.cleanroommc.multiblocked.core.asm.hooks;

import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import com.cleanroommc.multiblocked.jei.JeiPlugin;
import com.cleanroommc.multiblocked.jei.multipage.MultiblockInfoWrapper;
import com.cleanroommc.multiblocked.jei.recipeppage.RecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

import java.util.Objects;

public class JEIHooks {
    public static boolean handleMouseInput(RecipesGui recipesGui) {
        if (recipesGui.mc == null) return true;
        else {
            boolean find = false;
            for (RecipeLayout layout : Objects.requireNonNull(JeiPlugin.getRecipeLayouts(recipesGui))) {
                IRecipeWrapper wrapper = JeiPlugin.getWrapper(layout);
                if (wrapper instanceof MultiblockInfoWrapper) {
                    ((MultiblockInfoWrapper)wrapper).handleMouseInput();
                    find = true;
                } else if (wrapper instanceof RecipeWrapper){
                    RecipeWidget recipeWidget = (RecipeWidget) ((RecipeWrapper) wrapper).getWidget();
                    int width = recipeWidget.getGui().getScreenWidth();
                    int height = recipeWidget.getGui().getScreenHeight();
                    int x = Mouse.getEventX() * width / Minecraft.getMinecraft().displayWidth;
                    int y = height - Mouse.getEventY() * height / Minecraft.getMinecraft().displayHeight - 1;
                    if (recipeWidget.inputs.isMouseOverElement(x, y) || recipeWidget.outputs.isMouseOverElement(x, y)) {
                        ((RecipeWrapper)wrapper).handleMouseInput();
                        find = true;
                    }
                }
            }
            if (find) {
                final int x = Mouse.getEventX() * recipesGui.width / recipesGui.mc.displayWidth;
                final int y = recipesGui.height - Mouse.getEventY() * recipesGui.height / recipesGui.mc.displayHeight - 1;
                if (recipesGui.isMouseOver(x, y)) {
                    if (Mouse.getEventDWheel() != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
