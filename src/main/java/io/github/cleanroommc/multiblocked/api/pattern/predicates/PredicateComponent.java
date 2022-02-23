package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import net.minecraft.util.ResourceLocation;

public class PredicateComponent extends SimplePredicate {
    public ResourceLocation location;

    public PredicateComponent() {}

    public PredicateComponent(ComponentDefinition definition) {
        this(definition.location);
    }

    public PredicateComponent(ResourceLocation location) {
        super(state -> state.getBlockState().getBlock() instanceof BlockComponent && ((BlockComponent) state.getBlockState().getBlock()).definition.location.equals(location),
                () -> new BlockInfo[]{new BlockInfo(MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(location))});
        this.location = location;
    }

    @Override
    public SimplePredicate buildObjectFromJson() {
        predicate = state -> state.getBlockState().getBlock() instanceof BlockComponent && ((BlockComponent) state.getBlockState().getBlock()).definition.location.equals(location);
        candidates = () -> new BlockInfo[]{new BlockInfo(MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(location))};
        return this;
    }
}
