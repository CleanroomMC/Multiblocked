package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTController;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IShouldCheckPattern")
@ZenRegister
public interface IShouldCheckPattern {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    boolean apply(ICTController controllerTileEntity);
}
