package io.github.cleanroommc.multiblocked.api.framework.structure.definition;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.Objects;

public class BlockMetaDefinition extends BlockDefinition {

    private final int meta;

    public BlockMetaDefinition(Block block, int meta, boolean isController) {
        super(block, isController);
        this.meta = meta;
    }

    @Override
    public boolean test(IBlockState state) {
        return super.test(state) && state.getBlock().getMetaFromState(state) == meta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, isController, meta);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockMetaDefinition) {
            BlockMetaDefinition o = (BlockMetaDefinition) obj;
            return o.block.equals(this.block) && o.isController == this.isController && o.meta == this.meta;
        }
        return false;
    }
}
