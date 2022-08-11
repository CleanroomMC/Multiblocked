package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;

@BracketHandler
@ZenRegister
public class ControllerBracketHandler extends MultiblockedBracketHandler {

    public ControllerBracketHandler() {
        super(CraftTweakerAPI.getJavaMethod(ControllerBracketHandler.class, "get", String.class), "controller", ControllerDefinition.class);
    }

    public static ControllerDefinition get(String member) {
        ComponentDefinition definition = CTRegistry.getDefinition(member);
        return definition instanceof ControllerDefinition ? ((ControllerDefinition) definition) : null;
    }
}
