package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author youyihj
 */
@SuppressWarnings("unused")
public abstract class ControllerDynamicTileEntity<T extends ControllerDynamicTileEntity<?>> extends ControllerTileEntity {
    protected abstract Map<String, BiConsumer<T, CapabilityTrait>> getTraitSetters();

    @Override
    @SuppressWarnings("unchecked")
    public void setDefinition(ComponentDefinition definition) {
        super.setDefinition(definition);
        Map<String, BiConsumer<T, CapabilityTrait>> traitSetters = getTraitSetters();
        for (CapabilityTrait trait : traits.values()) {
            Class<? extends CapabilityTrait> traitClass = trait.getClass();
            if (traitClass.isAnnotationPresent(InterfaceUser.class)) {
                traitSetters.get(traitClass.getAnnotation(InterfaceUser.class).value().getSimpleName()).accept((T) this, trait);
            }
        }
    }
}
