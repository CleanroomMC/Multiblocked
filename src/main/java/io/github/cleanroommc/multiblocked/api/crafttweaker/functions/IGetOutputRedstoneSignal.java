package io.github.cleanroommc.multiblocked.api.crafttweaker.functions;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.world.IFacing;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.function.IGetOutputRedstoneSignal")
@ZenRegister
public interface IGetOutputRedstoneSignal {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    int apply(ComponentTileEntity<?> controllerTileEntity, IFacing facing);
}
