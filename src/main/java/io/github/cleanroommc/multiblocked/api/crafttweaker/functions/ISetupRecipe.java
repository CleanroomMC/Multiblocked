package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.ISetupRecipe")
@ZenRegister
public interface ISetupRecipe {
    /**
     * @return true - block the original logic
     */
    boolean apply(RecipeLogic recipeLogic, Recipe recipe);
}
