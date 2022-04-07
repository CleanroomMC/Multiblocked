package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IDynamicRenderer")
@ZenRegister
public interface IDynamicRenderer {
    IRenderer apply(ICTComponent componentTileEntity);
}
