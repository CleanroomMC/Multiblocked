package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;

@BracketHandler
@ZenRegister
public class CapabilityBracketHandler extends MultiblockedBracketHandler {
    public CapabilityBracketHandler() {
        super(CraftTweakerAPI.getJavaMethod(CapabilityBracketHandler.class, "get", String.class), "cap", MultiblockCapability.class);
    }

    public static MultiblockCapability<?> get(String member) {
        return CTRegistry.getCapability(member);
    }
}
