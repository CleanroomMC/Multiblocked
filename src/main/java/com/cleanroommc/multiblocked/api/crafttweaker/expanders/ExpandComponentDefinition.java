package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;

@ZenRegister
@ZenExpansion("mods.multiblocked.definition.ComponentDefinition")
public class ExpandComponentDefinition {

    @ZenCaster
    public static ControllerDefinition asController(ComponentDefinition definition) {
        return definition instanceof ControllerDefinition ? (ControllerDefinition) definition : null;
    }

    @ZenCaster
    public static PartDefinition asPart(ComponentDefinition definition) {
        return definition instanceof PartDefinition ? (PartDefinition) definition : null;
    }
}
