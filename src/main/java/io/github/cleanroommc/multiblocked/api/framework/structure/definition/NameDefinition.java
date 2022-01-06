package io.github.cleanroommc.multiblocked.api.framework.structure.definition;

import net.minecraft.block.state.IBlockState;

/**
 * Marker Definition
 */
public class NameDefinition implements IDefinition {

    public static final NameDefinition INSTANCE = new NameDefinition();

    @Override
    public boolean test(IBlockState state) {
        return true; // isAir?
    }

}
