package io.github.cleanroommc.multiblocked.api.pattern;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateStates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class Predicates {

    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        return new TraceabilityPredicate(new PredicateStates(allowedStates));
    }

    public static TraceabilityPredicate blocks(Block... blocks) {
        return new TraceabilityPredicate(new PredicateBlocks(blocks));
    }

    /**
     * Use it when you require that a position must have a specific capability.
     */
    public static TraceabilityPredicate anyCapability(IO io, MultiblockCapability<?> capability) {
        return new TraceabilityPredicate(new PredicateAnyCapability(io, capability));
    }

    public static TraceabilityPredicate component(ComponentDefinition definition) {
        TraceabilityPredicate predicate = new TraceabilityPredicate(new PredicateComponent(definition));
        if (definition instanceof ControllerDefinition) {
            predicate.setCenter();
        }
        return predicate;
    }

    public static TraceabilityPredicate any() {
        return TraceabilityPredicate.ANY;
    }

    public static TraceabilityPredicate air() {
        return TraceabilityPredicate.AIR;
    }
}
