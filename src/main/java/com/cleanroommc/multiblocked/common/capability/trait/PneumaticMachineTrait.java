package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.google.gson.JsonElement;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/**
 * @author youyihj
 */
@InterfaceUser(IPneumaticMachine.class)
public class PneumaticMachineTrait extends CapabilityTrait implements IPneumaticMachine {
    private IAirHandler airHandler;

    public PneumaticMachineTrait(MultiblockCapability<?> capability) {
        super(capability);
    }

    @Override
    public JsonElement deserialize() {
        return super.deserialize();
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
    }

    @Override
    public IAirHandler getAirHandler(EnumFacing enumFacing) {
        return airHandler;
    }
}
