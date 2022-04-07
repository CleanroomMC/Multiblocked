package com.cleanroommc.multiblocked.api.crafttweaker.functions;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IFacing;
import com.cleanroommc.multiblocked.Multiblocked;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

@FunctionalInterface
@ZenClass("mods.multiblocked.functions.IRightClick")
@ZenRegister
public interface IRightClick {
    @Optional.Method(modid = Multiblocked.MODID_CT)
    boolean apply(ICTComponent componentTileEntity, IPlayer player, IFacing facing, float hitX, float hitY, float hitZ);
}
