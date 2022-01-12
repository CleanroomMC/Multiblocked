package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.INeighborChanged")
@ZenRegister
public interface INeighborChanged {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    void apply(ComponentTileEntity<?> controllerTileEntity);
}
