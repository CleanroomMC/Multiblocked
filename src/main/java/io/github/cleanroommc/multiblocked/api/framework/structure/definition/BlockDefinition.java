package io.github.cleanroommc.multiblocked.api.framework.structure.definition;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Objects;

public class BlockDefinition implements IDefinition {

    private static BlockDefinition AIR;

    public static BlockDefinition getAir() {
        if (AIR == null) {
            AIR = new BlockDefinition(Blocks.AIR, false);
        }
        return AIR;
    }

    protected final Block block;
    protected final boolean isController;

    public BlockDefinition(Block block, boolean isController) {
        this.block = block;
        this.isController = isController;
    }

    @Override
    public boolean isController() {
        return isController;
    }

    @Override
    public boolean test(IBlockState state) {
        return state.getBlock() == this.block;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block.hashCode(), isController);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockDefinition) {
            BlockDefinition o = (BlockDefinition) obj;
            return o.block.equals(this.block) && o.isController == this.isController;
        }
        return false;
    }
}
