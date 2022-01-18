package io.github.cleanroommc.multiblocked.gui.texture;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ResourceTexture implements IGuiTexture {

    public final ResourceLocation imageLocation;

    public ResourceTexture(ResourceLocation imageLocation) {
        this.imageLocation = imageLocation;
    }

    public ResourceTexture(String imageLocation) {
        this(new ResourceLocation(Multiblocked.MODID, imageLocation));
    }

    @SideOnly(Side.CLIENT)
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        Minecraft.getMinecraft().renderEngine.bindTexture(imageLocation);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(0, 1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0, 0).endVertex();
        tessellator.draw();
    }

}
