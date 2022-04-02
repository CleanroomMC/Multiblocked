package com.cleanroommc.multiblocked.api.gui.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ResourceTexture implements IGuiTexture {

    public final ResourceLocation imageLocation;

    public final double offsetX;
    public final double offsetY;

    public final double imageWidth;
    public final double imageHeight;


    public ResourceTexture(ResourceLocation imageLocation, double offsetX, double offsetY, double width, double height) {
        this.imageLocation = imageLocation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public ResourceTexture(String imageLocation) {
        this(new ResourceLocation(imageLocation), 0.0, 0.0, 1.0, 1.0);
    }

    public ResourceTexture getSubTexture(double offsetX, double offsetY, double width, double height) {
        return new ResourceTexture(imageLocation,
                this.offsetX + (imageWidth * offsetX),
                this.offsetY + (imageHeight * offsetY),
                this.imageWidth * width,
                this.imageHeight * height);
    }

    @SideOnly(Side.CLIENT)
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        drawSubArea(x, y, width, height, 0.0, 0.0, 1.0, 1.0);
    }
    
    @SideOnly(Side.CLIENT)
    public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) {
        //sub area is just different width and height
        double imageU = this.offsetX + (this.imageWidth * drawnU);
        double imageV = this.offsetY + (this.imageHeight * drawnV);
        double imageWidth = this.imageWidth * drawnWidth;
        double imageHeight = this.imageHeight * drawnHeight;
        Minecraft.getMinecraft().renderEngine.bindTexture(imageLocation);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(imageU, imageV + imageHeight).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(imageU + imageWidth, imageV + imageHeight).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(imageU + imageWidth, imageV).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(imageU, imageV).endVertex();
        tessellator.draw();
    }

}
