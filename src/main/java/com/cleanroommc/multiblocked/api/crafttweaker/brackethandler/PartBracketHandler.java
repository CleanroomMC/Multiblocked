package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;

/**
 * @author youyihj
 */
@ZenRegister
@BracketHandler
public class PartBracketHandler extends MultiblockedBracketHandler {
    public PartBracketHandler() {
        super(CraftTweakerAPI.getJavaMethod(PartBracketHandler.class, "get", String.class), "part", PartDefinition.class);
    }

    public static PartDefinition get(String member) {
        ComponentDefinition definition = CTRegistry.getDefinition(member);
        return definition instanceof PartDefinition ? ((PartDefinition) definition) : null;
    }
}
