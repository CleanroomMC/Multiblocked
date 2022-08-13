package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenRegister
@ZenClass("mods.multiblocked.functions.ICalcRecipeDuration")
public interface ICalcRecipeDuration {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    int apply(RecipeLogic logic, Recipe recipe, int oldDuration);
}
