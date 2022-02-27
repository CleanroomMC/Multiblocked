package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class PredicateStates extends SimplePredicate {
    public IBlockState[] states;

    public PredicateStates() {
        super("states");
    }
    
    public PredicateStates(IBlockState... states) {
        this();
        this.states = states;
        buildObjectFromJson();
    }

    @Override
    public SimplePredicate buildObjectFromJson() {
        predicate = state -> ArrayUtils.contains(states, state.getBlockState());
        candidates = () -> Arrays.stream(states).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
        return this;
    }
}
