package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.tile.part.PartDynamicTileEntity;
import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import net.minecraft.util.EnumFacing;
import vazkii.botania.api.mana.IManaPool;

import java.util.Map;
import java.util.function.BiConsumer;


/**
 * @author youyihj
 */
public class TestComponent extends PartDynamicTileEntity<TestComponent> implements IPneumaticMachine {
    private static final Map<String, BiConsumer<TestComponent, CapabilityTrait>> TRAIT_SETTERS = ImmutableMap.<String, BiConsumer<TestComponent, CapabilityTrait>>builder().put("IPneumaticMachine", (te, trait) -> te.machine = ((IPneumaticMachine) trait)).put("IManaPool", (te, trait) -> te.pool = ((IManaPool) trait)).build();

    public IPneumaticMachine machine;
    public IManaPool pool;

    @Override
    public IAirHandler getAirHandler(EnumFacing enumFacing) {
        return machine.getAirHandler(enumFacing);
    }

    @Override
    protected Map<String, BiConsumer<TestComponent, CapabilityTrait>> getTraitSetters() {
        return TRAIT_SETTERS;
    }
}
