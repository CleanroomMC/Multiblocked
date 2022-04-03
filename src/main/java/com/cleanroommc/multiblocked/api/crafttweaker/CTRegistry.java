package com.cleanroommc.multiblocked.api.crafttweaker;


import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.multiblocked.MBDRegistry")
@ZenRegister
public class CTRegistry {

    @ZenMethod
    public static ComponentDefinition getDefinition(String name) {
        return MultiblockComponents.DEFINITION_REGISTRY.get(new ResourceLocation(name));
    }

}
