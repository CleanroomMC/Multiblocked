package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IPredicateFunction")
@ZenRegister
public interface IPredicateFunction {

    boolean test(RecipeLogic recipeLogic, Recipe recipe);

}
