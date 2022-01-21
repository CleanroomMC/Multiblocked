package io.github.cleanroommc.multiblocked.core.mixins;

import io.github.cleanroommc.multiblocked.jei.JeiPlugin;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipesGui.class)
public class RecipesGuiMixin {
    @Shadow @Final private List<RecipeLayout> recipeLayouts;

    @Inject(method = "func_146274_d", at = @At(value = "HEAD"), cancellable = true)
    private void injectRenderBlockDamage(CallbackInfo ci) {
        RecipesGui recipesGui = ((RecipesGui) (Object) this);
        if (recipesGui.mc == null) ci.cancel();
        else {
            boolean find = false;
            for (RecipeLayout layout : recipeLayouts) {
                IRecipeWrapper wrapper = JeiPlugin.getWrapper(layout);
                if (wrapper instanceof MultiblockInfoRecipeWrapper) {
                    ((MultiblockInfoRecipeWrapper)wrapper).handleMouseInput();
                    find = true;
                }
            }
            if (find) {
                final int x = Mouse.getEventX() * recipesGui.width / recipesGui.mc.displayWidth;
                final int y = recipesGui.height - Mouse.getEventY() * recipesGui.height / recipesGui.mc.displayHeight - 1;
                if (recipesGui.isMouseOver(x, y)) {
                    if (Mouse.getEventDWheel() != 0) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
