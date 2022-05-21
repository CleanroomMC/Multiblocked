package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IDynamicRenderer")
@ZenRegister
public interface IDynamicRenderer {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    IRenderer apply(ICTComponent componentTileEntity);
}
