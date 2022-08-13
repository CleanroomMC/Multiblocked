package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.recipe.Content;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;

/**
 * @author youyihj
 */
@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IApplyContentModifier")
@ZenRegister
public interface IApplyContentModifier {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    @Nullable
    ContentModifier apply(RecipeLogic logic, Recipe recipe, Content content, MultiblockCapability<?> capability, IO io, boolean isTickIO);
}
