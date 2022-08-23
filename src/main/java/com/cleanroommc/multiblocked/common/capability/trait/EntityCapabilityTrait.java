package com.cleanroommc.multiblocked.common.capability.trait;


import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.EntityMultiblockCapability;

/**
 * @author KilaBash
 * @date 2022/8/3
 * @implNote EntityCapabilityTrait
 */
public class EntityCapabilityTrait extends CapabilityTrait {

    public EntityCapabilityTrait() {
        super(EntityMultiblockCapability.CAP);
    }

}
