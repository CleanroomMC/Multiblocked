package io.github.cleanroommc.multiblocked.client.renderer;

import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ComponentTESR extends TileEntitySpecialRenderer<ComponentTileEntity> {

    public ComponentTESR() {
        super();
    }

    @Override
    public void render(@Nonnull ComponentTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IRenderer renderer = te.getRenderer();
        if (renderer != null) {
            te.getRenderer().renderTESR(te, x, y, z, partialTicks, destroyStage, alpha);

        }
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull ComponentTileEntity te) {
        IRenderer renderer = te.getRenderer();
        if (renderer != null) {
            return te.getRenderer().isGlobalRenderer(te);
        }
        return false;
    }

}
