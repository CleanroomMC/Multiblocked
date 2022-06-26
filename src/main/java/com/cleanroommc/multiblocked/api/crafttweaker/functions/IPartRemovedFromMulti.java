package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTController;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTPart;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IPartRemovedFromMulti")
@ZenRegister
public interface IPartRemovedFromMulti {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    void apply(ICTPart partTileEntity, ICTController controllerTileEntity);
}
