package com.cleanroommc.multiblocked.api.pattern;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateStates;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
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
    public static TraceabilityPredicate anyCapability(MultiblockCapability<?> capability) {
        return new TraceabilityPredicate(new PredicateAnyCapability(capability));
    }

    public static TraceabilityPredicate component(ComponentDefinition definition) {
        TraceabilityPredicate predicate = new TraceabilityPredicate(new PredicateComponent(definition));
        return definition instanceof ControllerDefinition ? predicate.setCenter() : predicate;
    }

    public static TraceabilityPredicate any() {
        return new TraceabilityPredicate(SimplePredicate.ANY);
    }

    public static TraceabilityPredicate air() {
        return new TraceabilityPredicate(SimplePredicate.AIR);

    }
}
