package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IReadInitialData")
@ZenRegister
public interface IReadInitialData {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    void apply(ICTComponent componentTileEntity, IData data);
}
