package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTController;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.api.pattern.BlockPattern;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IDynamicPattern")
@ZenRegister
public interface IDynamicPattern {
    BlockPattern apply(ICTController controllerTileEntity);
}
