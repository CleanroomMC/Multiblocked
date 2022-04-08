package com.cleanroommc.multiblocked.api.crafttweaker;


import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.multiblocked.MBDRegistry")
@ZenRegister
public class CTRegistry {

    @ZenMethod
    public static ComponentDefinition getDefinition(String name) {
        return MbdComponents.DEFINITION_REGISTRY.get(new ResourceLocation(name));
    }

    @ZenMethod
    public static RecipeMap getRecipeMap(String name) {
        return RecipeMap.RECIPE_MAP_REGISTRY.get(name);
    }

    @ZenMethod
    public static MultiblockCapability<?> getCapability(String name) {
        return MbdCapabilities.get(name);
    }

}
