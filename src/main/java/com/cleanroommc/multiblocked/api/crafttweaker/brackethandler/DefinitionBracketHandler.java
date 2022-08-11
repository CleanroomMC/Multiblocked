package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;

@BracketHandler
@ZenRegister
public class DefinitionBracketHandler extends MultiblockedBracketHandler {

    public DefinitionBracketHandler() {
        super(CraftTweakerAPI.getJavaMethod(DefinitionBracketHandler.class, "get", String.class), "definition", ComponentDefinition.class);
    }

    public static ComponentDefinition get(String member) {
        return CTRegistry.getDefinition(member);
    }
}
