package com.cleanroommc.multiblocked.api.recipe;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenRegister
@ZenClass("mods.multiblocked.recipe.Content")
public class Content {
    public transient Object content;
    @ZenProperty
    public float chance;
    @ZenProperty
    public String slotName;

    public Content(Object content, float chance, String slotName) {
        this.content = content;
        this.chance = chance;
        this.slotName = slotName;
    }

    public Object getContent() {
        return content;
    }
    
}
