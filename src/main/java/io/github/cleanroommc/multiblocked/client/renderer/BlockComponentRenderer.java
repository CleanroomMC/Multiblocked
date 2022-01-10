package io.github.cleanroommc.multiblocked.client.renderer;

import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockComponentRenderer {

    public static List<BakedQuad> renderComponent(ComponentTileEntity component, EnumFacing side, BlockRenderLayer layer) {
        return Collections.emptyList();
    }

}
