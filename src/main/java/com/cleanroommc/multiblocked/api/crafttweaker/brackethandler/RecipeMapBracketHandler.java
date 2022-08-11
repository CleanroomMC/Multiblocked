package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;

@BracketHandler
@ZenRegister
public class RecipeMapBracketHandler extends MultiblockedBracketHandler {
    public RecipeMapBracketHandler() {
        super(CraftTweakerAPI.getJavaMethod(RecipeMapBracketHandler.class, "get", String.class), "recipe_map", RecipeMap.class);
    }

    public static RecipeMap get(String member) {
        return CTRegistry.getRecipeMap(member);
    }
}
