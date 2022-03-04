package io.github.cleanroommc.multiblocked.core.asm.hooks;

import io.github.cleanroommc.multiblocked.jei.JeiPlugin;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
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
