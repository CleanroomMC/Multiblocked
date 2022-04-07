package com.cleanroommc.multiblocked.api.recipe;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.Tuple;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenRegister
@ZenClass("mods.multiblocked.recipe.Content")
public class Content {
    public final Object content;
    @ZenProperty
    public final float chance;
    
    public Content(Tuple<Object, Float> tuple) {
        content = tuple.getFirst();
        chance = tuple.getSecond();
    }
    
}
