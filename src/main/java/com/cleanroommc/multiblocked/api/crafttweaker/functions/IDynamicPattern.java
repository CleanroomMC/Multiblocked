package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTController;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.api.pattern.BlockPattern;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IDynamicPattern")
@ZenRegister
public interface IDynamicPattern {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    BlockPattern apply(ICTController controllerTileEntity);
}
