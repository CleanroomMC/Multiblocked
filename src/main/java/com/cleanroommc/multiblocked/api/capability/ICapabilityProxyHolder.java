package com.cleanroommc.multiblocked.api.capability;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.google.common.collect.Table;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.multiblocked.capability.CapabilityHolder")
@ZenRegister
public interface ICapabilityProxyHolder {

    @ZenGetter
    default boolean hasProxies() {
        return getCapabilities() != null && !getCapabilities().isEmpty();
    }

    @ZenMethod
    default boolean hasProxy(IO io, MultiblockCapability<?> capability) {
        return hasProxies() && getCapabilities().contains(io, capability);
    }

    Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> getCapabilities();

    static ICapabilityProxyHolder fromWorldPos(World world, BlockPos pos, MultiblockCapability<?>... capabilities) {
        return new CommonCapabilityProxyHolder(world, pos, capabilities);
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_CT)
    static ICapabilityProxyHolder fromWorldPos(IWorld world, IBlockPos pos, MultiblockCapability<?>... capabilities) {
        return fromWorldPos(CraftTweakerMC.getWorld(world), CraftTweakerMC.getBlockPos(pos), capabilities);
    }
}
